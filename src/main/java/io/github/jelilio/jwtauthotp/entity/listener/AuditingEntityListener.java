package io.github.jelilio.jwtauthotp.entity.listener;

import io.github.jelilio.jwtauthotp.entity.base.AbstractAuditingEntity;
import org.eclipse.microprofile.jwt.Claim;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.time.Instant;

@ApplicationScoped
public class AuditingEntityListener {
  @Claim("login_id")
  public String loginId;

  @PrePersist
  private void prePersist(AbstractAuditingEntity entity) {
    entity.createdBy = loginId == null? "system" : loginId;
    entity.createdDate = Instant.now();

    entity.lastModifiedBy = loginId == null? "system" : loginId;
    entity.lastModifiedDate = Instant.now();
  }

  @PreUpdate
  public void preUpdate(AbstractAuditingEntity entity) {
    entity.lastModifiedBy = loginId == null? "system" : loginId;
    entity.lastModifiedDate = Instant.now();
  }
}
