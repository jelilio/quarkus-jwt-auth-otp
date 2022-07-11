package io.github.jelilio.jwtauthotp.exception.handler;

import io.github.jelilio.jwtauthotp.exception.AuthenticationException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.Map;

@Provider
public class AuthenticationExceptionHandler implements ExceptionMapper<AuthenticationException> {
  @Override
  public Response toResponse(AuthenticationException ex) {
    return Response.status(Response.Status.FORBIDDEN).
        entity(Map.of("message", ex.getMessage(), "code", ex.getCode())).build();
  }
}
