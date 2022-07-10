package io.github.jelilio.jwtauthotp.entity;


import io.github.jelilio.jwtauthotp.entity.base.AbstractAuditingEntity;
import io.github.jelilio.jwtauthotp.entity.listener.AuditingEntityListener;
import io.smallrye.mutiny.Uni;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static javax.persistence.FetchType.EAGER;

@Entity
@Table(name = "roles")
@EntityListeners(AuditingEntityListener.class)
public class Role extends AbstractAuditingEntity {
  public static final String ROLE_NAME_PROPERTY = "name";

  @Id
  @Column(name = "name", length = 100)
  public String name;

  public static Uni<Long> countByName(String name) {
    return Role.count(ROLE_NAME_PROPERTY, name);
  }

  @ManyToMany(fetch = EAGER)
  @JoinTable(name = "roles_perms",
      joinColumns = @JoinColumn(name = "role_name"),
      inverseJoinColumns = @JoinColumn(name = "permission_name")
  )
  public Set<Permission> permissions = new HashSet<>();

  public Role() {}

  public Role(String name) {
    this.name = name;
  }

  public Role(String name, Set<String> permissions) {
    this.name = name;
    this.permissions = permissions.stream()
        .map(Permission::new)
        .collect(Collectors.toSet());
  }

  public static Uni<List<Role>> findAllByIds(Set<String> names) {
    return find("from Role r where r.name IN (?1)", names).list();
  }

  public Set<String> getPermissions(boolean withRole) {
    if(!withRole) return getPermissions();

    Set<String> perms = new HashSet<>(Set.of(this.name));
    perms.addAll(getPermissions());
    return perms;
  }

  public Set<String> getPermissions() {
    return permissions.stream()
        .map(Permission::getName)
        .collect(Collectors.toSet());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Role role = (Role) o;
    return name.equals(role.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  @Override
  public String toString() {
    return "Role{" +
        "name='" + name + '\'' +
        '}';
  }
}
