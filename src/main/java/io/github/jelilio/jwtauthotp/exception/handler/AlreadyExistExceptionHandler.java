package io.github.jelilio.jwtauthotp.exception.handler;

import io.github.jelilio.jwtauthotp.exception.AlreadyExistException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.Map;

@Provider
public class AlreadyExistExceptionHandler implements ExceptionMapper<AlreadyExistException> {
  @Override
  public Response toResponse(AlreadyExistException ex) {
    return Response.status(Response.Status.BAD_REQUEST).
        entity(Map.of("message", ex.getMessage())).build();
  }
}
