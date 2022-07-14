package io.github.jelilio.jwtauthotp.web.rest.admin;

import io.github.jelilio.jwtauthotp.entity.User;
import io.github.jelilio.jwtauthotp.service.UserService;
import io.github.jelilio.jwtauthotp.util.PageRequest;
import io.github.jelilio.jwtauthotp.util.Paged;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/api/admin")
@RolesAllowed("ROLE_ADMIN")
public class AdminResource {
  @Inject
  UserService userService;

  @GET
  @Path("/users")
  @Operation(summary = "Get a paginated list of all registered users")
  @APIResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = User.class)))
  public Uni<Paged<User>> get(@BeanParam PageRequest pageRequest) {
    return userService.findAll(pageRequest.size, pageRequest.page);
  }
}
