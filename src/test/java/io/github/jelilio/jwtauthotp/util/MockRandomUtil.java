package io.github.jelilio.jwtauthotp.util;

import io.quarkus.test.Mock;

import javax.enterprise.context.ApplicationScoped;

@Mock
@ApplicationScoped
public class MockRandomUtil extends RandomUtil {
  @Override
  public String generatePassword() {
    return "password";
  }
  @Override
  public String generateOtp() {
    return "1234";
  }
}
