package io.github.jelilio.jwtauthotp.service.impl;

import io.github.jelilio.jwtauthotp.entity.User;
import io.github.jelilio.jwtauthotp.service.MailerService;
import io.github.jelilio.jwtauthotp.util.LocaleMessageUtil;
import io.quarkus.mailer.MailTemplate;
import io.quarkus.qute.Location;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class MailerServiceImpl implements MailerService {
  @Inject
  @Location("templates/mails/verification")
  MailTemplate otpVerification;;

  @Inject
  @Location("templates/mails/activation")
  MailTemplate activationMail;

  @Override
  public Uni<Void> sendOtpMail(User user, String otpKey, Long otpKeyDuration) {
    return otpVerification
        .to(user.email)
        .subject(LocaleMessageUtil.getDefaultMessage("mail.subject.otp-verification", otpKey))
        .data("otpKey", otpKey)
        .data("expireIn", TimeUnit.SECONDS.toMinutes(otpKeyDuration))
        .send();
  }

  @Override
  public Uni<Void> sendActivationMail(User user) {
    return activationMail
        .to(user.email)
        .subject(LocaleMessageUtil.getDefaultMessage("mail.subject.activation"))
        .data("user", user)
        .send();
  }
}
