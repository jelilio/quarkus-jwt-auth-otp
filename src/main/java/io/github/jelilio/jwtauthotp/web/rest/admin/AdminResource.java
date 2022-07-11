package io.github.jelilio.jwtauthotp.web.rest.admin;

import io.github.jelilio.jwtauthotp.entity.User;
import io.github.jelilio.jwtauthotp.service.UserService;
import io.github.jelilio.jwtauthotp.util.PageRequest;
import io.github.jelilio.jwtauthotp.util.Paged;
import io.smallrye.mutiny.Uni;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/api/admin")
@RolesAllowed("ROLE_ADMIN")
public class AdminResource {
  @Inject
  UserService userService;

  @GET
  @Path("/users")
  public Uni<Paged<User>> get(@BeanParam PageRequest pageRequest) {
    return userService.findAll(pageRequest.size, pageRequest.page);
  }
}
