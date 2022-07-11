package io.github.jelilio.jwtauthotp.service;

import io.github.jelilio.jwtauthotp.entity.User;
import io.smallrye.mutiny.Uni;

import java.util.Optional;

public interface MailerService {
  Uni<Void> sendOtpMail(User user, String otpKey, Long otpKeyDuration);

  Uni<Void> sendActivationMail(User user);
}
