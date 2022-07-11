package io.github.jelilio.jwtauthotp.service.impl;


import com.google.common.collect.Sets;
import io.github.jelilio.jwtauthotp.dto.RoleDto;
import io.github.jelilio.jwtauthotp.entity.Permission;
import io.github.jelilio.jwtauthotp.entity.Role;
import io.github.jelilio.jwtauthotp.exception.AlreadyExistException;
import io.github.jelilio.jwtauthotp.service.RoleService;
import io.github.jelilio.jwtauthotp.util.Paged;
import io.github.jelilio.jwtauthotp.util.PaginationUtil;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Page;
import io.smallrye.mutiny.Uni;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.NotFoundException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class RoleServiceImpl implements RoleService {
  private static final Logger LOGGER = LoggerFactory.getLogger(RoleServiceImpl.class);

  @Override
  public Uni<Role> findById(String name) {
    return Role.<Role>findById(name)
        .onItem().ifNull()
        .failWith(() -> new NotFoundException("Role with the name: " + name + ", not found"));
  }

  @Override
  public Uni<Paged<Role>> findAll(int size, int index) {
    Page page = Page.of(index, size);

    return PaginationUtil.paginate(page, Role.findAll().page(page));
  }

  @Override
  public Uni<Role> create(RoleDto dto) {
    return checkIfExist(dto.name()).flatMap(check -> {
      if(check) {
        throw new AlreadyExistException(String.format("Role with this name: %s, already exist", dto.name()));
      }

      Role role = new Role(dto.name(), dto.permissions());
      return Panache.withTransaction(role::persist);
    });
  }

  public Uni<Boolean> checkIfExist(String name) {
    return Role.countByName(name)
        .onItem().transform(count -> count > 0);
  }

  @Override
  public Uni<Role> getOrCreate(String name) {
    return Role.<Role>findById(name)
        .onItem().ifNull()
        .switchTo(() -> {
          Role role = new Role(name);
          return Panache.withTransaction(role::persist);
        });
  }

  @Override
  public Uni<List<Role>> getOrCreate(Set<String> names) {
    Uni<List<Role>> uniRoles = Role.findAllByIds(names);

    return uniRoles.flatMap(roles -> {
      Set<String> existing = roles.stream().map(item -> item.name).collect(Collectors.toSet());

      Set<Role> whatToAdd = Sets.difference(names, existing).stream().map(Role::new).collect(Collectors.toSet());

      return Panache.withTransaction(() -> Role.persist(whatToAdd))
          .map(__ -> names.stream().map(Role::new).collect(Collectors.toList()));
    });
  }

  @Override
  public Uni<Role> update(String name, RoleDto roleDto) {
    return findById(name)
        .flatMap(ext -> {
          ext.permissions = roleDto.permissions().stream()
              .map(Permission::new)
              .collect(Collectors.toSet());
          return Panache.withTransaction(ext::persist);
        });
  }
}
