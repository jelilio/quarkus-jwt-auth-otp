package io.github.jelilio.jwtauthotp.web.rest;

import io.github.jelilio.jwtauthotp.dto.AuthRequestDto;
import io.github.jelilio.jwtauthotp.dto.BasicRegisterDto;
import io.github.jelilio.jwtauthotp.dto.ValidateOtpDto;
import io.github.jelilio.jwtauthotp.entity.User;
import io.github.jelilio.jwtauthotp.model.AuthResponse;
import io.github.jelilio.jwtauthotp.model.OtpResponseDto;
import io.github.jelilio.jwtauthotp.service.UserService;
import io.github.jelilio.jwtauthotp.util.ResponseWrapper;
import io.github.jelilio.jwtauthotp.util.TokenUtil;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import javax.annotation.security.PermitAll;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

@PermitAll
@RequestScoped
@Path("/api/account")
public class AccountResource {
  @Claim(TokenUtil.USER_ID)
  String userId;

  @Inject
  UserService userService;

  @POST
  @Path("/authenticate")
  @Operation(summary = "Authenticate a user and sent an OTP key to registered email")
  @APIResponse(responseCode = "403", description = "Forbidden to access this resources")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = AuthResponse.class, required = true)))
  public Uni<Response> authenticate(@Valid AuthRequestDto dto) {
    return userService.authenticate(dto.username(), dto.password()).onItem()
        .transform(response -> Response.ok(new OtpResponseDto(response.getItem2())).build());
  }

  @POST
  @Path("/register")
  @Operation(summary = "Register a user")
  @APIResponse(responseCode = "201", description = "User's account successfully registered",
      content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = OtpResponseDto.class)))
  public Uni<Response> register(@Valid @RequestBody BasicRegisterDto dto) {
    return userService.register(dto)
        .map(inserted -> Response
            .created(URI.create("/api/account/register/" + inserted.getItem1().id))
            .entity(new OtpResponseDto(inserted.getItem2()))
            .build());
  }

  @POST
  @Path("/authenticate-otp")
  @Operation(summary = "Authenticate a user with an OTP")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = String.class)))
  public Uni<Response> authenticateOtp(@Valid @RequestBody ValidateOtpDto dto) {
    return userService.authenticateOtp(dto.email(), dto.otpKey()).onItem()
        .transform(response -> Response.ok(response).build());
  }

  @POST
  @Path("/verify-email-otp")
  @Operation(summary = "Verify a user's email address using OTP")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = String.class)))
  public Uni<Response> verifyEmail(@Valid @RequestBody ValidateOtpDto dto) {
    return userService.verifyEmail(dto.email(), dto.otpKey()).onItem()
        .transform(response -> Response.ok(response).build());
  }

  @POST
  @Path("/request-otp")
  @Operation(summary = "Request for OTP")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = OtpResponseDto.class)))
  public Uni<Response> requestOtp(@Valid @RequestBody String email) {
    return userService.requestOtp(email).onItem()
        .transform(userLongTuple -> Response.ok(ResponseWrapper.of("Kindly check your mail"))
            .entity(new OtpResponseDto(userLongTuple.getItem2()))
            .build());
  }

  @GET
  @Path("/check-email")
  @Operation(summary = "Verify if an email is in used or not")
  @APIResponse(responseCode = "200", content = @Content(mediaType = TEXT_PLAIN, schema = @Schema(implementation = Boolean.class)))
  public Uni<Response> checkEmail(@NotNull @QueryParam("email") String email) {
    return userService.checkIfEmailAvailable(email)
        .onItem().transform(item -> Response.ok().entity(item).build());
  }

  @GET
  @Path("/profile")
  @Authenticated
  @Operation(summary = "Get a logged-in user's profile")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = User.class)))
  public Uni<Response> accountProfile() {
    return userService.findById(userId)
        .onItem().transform(item -> Response.ok().entity(item).build());
  }
}
