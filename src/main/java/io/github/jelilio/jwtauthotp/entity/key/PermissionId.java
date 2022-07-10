package io.github.jelilio.jwtauthotp.entity.key;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class PermissionId implements Serializable {
  @Column(name = "name", length = 100)
  public String name;

  @Column(name = "role_name")
  public String roleName;

  public PermissionId() {}

  public PermissionId(String name, String roleName) {
    this.name = name;
    this.roleName = roleName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PermissionId that = (PermissionId) o;
    return name.equals(that.name) && roleName.equals(that.roleName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, roleName);
  }
}
