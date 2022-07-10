package io.github.jelilio.jwtauthotp.entity;


import io.github.jelilio.jwtauthotp.entity.base.AbstractAuditingEntity;
import io.github.jelilio.jwtauthotp.entity.listener.AuditingEntityListener;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "permissions")
@EntityListeners(AuditingEntityListener.class)
public class Permission extends AbstractAuditingEntity {
  @Id
  @Column(name = "name", length = 100)
  public String name;

  public Permission() {}

  public Permission(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Permission permission = (Permission) o;
    return name.equals(permission.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }
}
