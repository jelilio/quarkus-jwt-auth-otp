package io.github.jelilio.jwtauthotp.service;

import io.github.jelilio.jwtauthotp.dto.RoleDto;
import io.github.jelilio.jwtauthotp.entity.Role;
import io.github.jelilio.jwtauthotp.util.Paged;
import io.smallrye.mutiny.Uni;

import java.util.List;
import java.util.Set;

public interface RoleService {
  Uni<Role> findById(String name);

  Uni<Paged<Role>> findAll(int size, int index);

  Uni<Role> create(RoleDto dto);

  Uni<Role> getOrCreate(String name);

  Uni<List<Role>> getOrCreate(Set<String> names);

  Uni<Role> update(String name, RoleDto roleDto);
}
