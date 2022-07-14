package io.github.jelilio.jwtauthotp.entity.listener;

import io.github.jelilio.jwtauthotp.entity.base.AbstractAuditingEntity;
import org.eclipse.microprofile.jwt.Claim;

import javax.enterprise.context.RequestScoped;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.time.Instant;

import static io.github.jelilio.jwtauthotp.util.TokenUtil.USER_ID;

@RequestScoped
public class AuditingEntityListener {
  @Claim(USER_ID)
  public String userId;

  @PrePersist
  private void prePersist(AbstractAuditingEntity entity) {
    entity.createdBy = userId == null? "system" : userId;
    entity.createdDate = Instant.now();

    entity.lastModifiedBy = userId == null? "system" : userId;
    entity.lastModifiedDate = Instant.now();
  }

  @PreUpdate
  public void preUpdate(AbstractAuditingEntity entity) {
    entity.lastModifiedBy = userId == null? "system" : userId;
    entity.lastModifiedDate = Instant.now();
  }
}
