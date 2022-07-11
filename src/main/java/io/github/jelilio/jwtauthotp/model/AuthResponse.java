package io.github.jelilio.jwtauthotp.model;

public record AuthResponse(
    String token
) {
  public AuthResponse(String token) {
    this.token = "Bearer " + token;
  }
}
