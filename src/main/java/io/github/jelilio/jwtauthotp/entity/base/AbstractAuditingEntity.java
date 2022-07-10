package io.github.jelilio.jwtauthotp.entity.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.time.Instant;

@MappedSuperclass
public abstract class AbstractAuditingEntity extends PanacheEntityBase {

  @JsonIgnore
  @Column(name = "created_by", updatable = false, nullable = false)
  public String createdBy;

  @JsonIgnore
  @Column(name = "created_date", updatable  = false, nullable = false)
  public Instant createdDate;

  @JsonIgnore
  @Column(name = "last_modified_by", nullable = false)
  public String lastModifiedBy;

  @JsonIgnore
  @Column(name = "last_modified_date", nullable = false)
  public Instant lastModifiedDate;
}
