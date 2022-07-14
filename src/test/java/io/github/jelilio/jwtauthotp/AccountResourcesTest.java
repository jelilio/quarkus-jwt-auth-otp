package io.github.jelilio.jwtauthotp;

import io.github.jelilio.jwtauthotp.dto.AuthRequestDto;
import io.github.jelilio.jwtauthotp.dto.BasicRegisterDto;
import io.github.jelilio.jwtauthotp.dto.ValidateOtpDto;
import io.github.jelilio.jwtauthotp.model.OtpResponseDto;
import io.quarkus.test.junit.QuarkusTest;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static io.github.jelilio.jwtauthotp.exception.AuthenticationException.AUTH_VERIFY_EMAIL;
import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.HttpHeaders.*;
import static javax.ws.rs.core.Response.Status.*;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AccountResourcesTest {
  private static final String JSON = "application/json;charset=UTF-8";
  private static final String TEXT = "text/plain";
  private static final String DEFAULT_NAME = "John Doe";
  private static final String DEFAULT_EMAIL = "johnd@mail.com";
  private static final String DEFAULT_USERNAME = "johnd";
  private static final String DEFAULT_PASSWORD = "password";
  private static final String DEFAULT_RIGHT_OTP = "1234";
  private static final String DEFAULT_WRONG_OTP = "34538";

  private static String token;

  @Test
  @Order(1)
  void shouldUserRegister() {
    BasicRegisterDto registerDto = new BasicRegisterDto(DEFAULT_NAME, DEFAULT_EMAIL, DEFAULT_PASSWORD);

    var response = given()
        .body(registerDto)
        .header(CONTENT_TYPE, JSON)
        .header(ACCEPT, JSON)
        .when()
        .post("/api/account/register")
        .then()
        .statusCode(CREATED.getStatusCode());

    String location = response.extract().header("Location");
    OtpResponseDto body = response.extract().body().as(OtpResponseDto.class);

    assertTrue(location.contains("/api/account/register"));
    assertNotNull(body);
    assertInstanceOf(Long.class, body.expireIn());

    // Stores the id
    String[] segments = location.split("/");
    String userId = segments[segments.length - 1];
    assertNotNull(userId);
  }

  @Test
  @Order(2)
  void shouldUserAuthenticateWithEmail() {
    AuthRequestDto authRequestDto = new AuthRequestDto(DEFAULT_EMAIL, DEFAULT_PASSWORD);

    given()
        .body(authRequestDto)
        .header(CONTENT_TYPE, JSON)
        .header(ACCEPT, JSON)
        .when()
        .post("/api/account/authenticate")
        .then()
        .statusCode(FORBIDDEN.getStatusCode())
        .header(CONTENT_TYPE, JSON)
        .body("code", Is.is(AUTH_VERIFY_EMAIL));
  }

  @Test
  @Order(3)
  void shouldUserVerifyEmailUsingOtp() {
    ValidateOtpDto validateOtpDto = new ValidateOtpDto(DEFAULT_EMAIL, DEFAULT_RIGHT_OTP);

    token = given()
        .body(validateOtpDto)
        .header(CONTENT_TYPE, JSON)
        .header(ACCEPT, JSON)
        .when()
        .post("/api/account/verify-email-otp")
        .then()
        .statusCode(OK.getStatusCode())
        .header(CONTENT_TYPE, JSON)
        .extract().body().jsonPath().getString("token");

    assertNotNull(token);
    assertTrue(token.startsWith("Bearer"));
  }

  @Test
  @Order(4)
  void shouldUserGetProfile() {
    given()
        .header(AUTHORIZATION, token)
        .header(CONTENT_TYPE, JSON)
        .when()
        .get("/api/account/profile")
        .then()
        .statusCode(OK.getStatusCode())
        .header(CONTENT_TYPE, JSON)
        .body("name", Is.is(DEFAULT_NAME))
        .body("email", Is.is(DEFAULT_EMAIL));
  }
}
