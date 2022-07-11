package io.github.jelilio.jwtauthotp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.jelilio.jwtauthotp.entity.base.AbstractAuditingEntity;
import io.github.jelilio.jwtauthotp.entity.listener.AuditingEntityListener;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.persistence.*;
import javax.validation.constraints.Email;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class User extends AbstractAuditingEntity {
  public static final String USER_EMAIL_PROPERTY = "email";
  public static final String USER_USERNAME_PROPERTY = "username";

  @Id
  @JsonIgnore
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Schema(readOnly = true)
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  public UUID id;
  
  @Column(length = 100, nullable = false)
  public String name;
  
  @Email
  @Column(unique = true, nullable = false)
  public String email;
  
  @JsonIgnore
  @Column(nullable = false, length = 500)
  public String password;
  
  @Column(unique = true, length = 100)
  public String username;
  
  public boolean enabled = true;

  @JsonIgnore
  public Instant activatedDate;

  public Instant lastLoginDate;
  
  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(name = "users_roles",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_name")
  )
  public Set<Role> roles = new HashSet<>();

  public static Uni<User> findByEmail(String email) {
    return User.find("email = ?1", email).firstResult();
  }

  public static Uni<User> findByEmailAndNotActivated(String email) {
    return User.find("email = ?1 and activatedDate = null", email).firstResult();
  }

  public static Uni<User> findByUsernameOrEmail(String usernameOrEmail) {
    return User.find("email = ?1 or username = ?1", usernameOrEmail).firstResult();
  }

  public static Uni<Long> countByEmail(String email) {
    return User.count(USER_EMAIL_PROPERTY, email);
  }

  public static Uni<Long> countByEmailAvailable(String email) {
    return User.count("email = ?1 and activatedDate != null", email);
  }

  public static Uni<User> findById(String id) {
    return User.findById(UUID.fromString(id));
  }

  public User() {}

  public User(String email, String password) {
    this.email = email;
    this.password = password;
  }

  public boolean isActivated() {
    return activatedDate != null;
  }

  public void activate() {
    this.activatedDate = Instant.now();
  }

  public Set<String> getRoles() {
    return roles.stream()
        .map(item -> item.getPermissions(true))
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());
  }
}
