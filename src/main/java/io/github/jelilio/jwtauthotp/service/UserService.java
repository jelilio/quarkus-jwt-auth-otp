package io.github.jelilio.jwtauthotp.service;

import io.github.jelilio.jwtauthotp.dto.BasicRegisterDto;
import io.github.jelilio.jwtauthotp.entity.User;
import io.github.jelilio.jwtauthotp.model.AuthResponse;
import io.github.jelilio.jwtauthotp.util.Paged;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;

public interface UserService {
  Uni<User> findById(String id);

  Uni<Paged<User>> findAll(int size, int index);

  Uni<Boolean> checkIfEmailAvailable(String email);

  Uni<Tuple2<User, Long>> register(BasicRegisterDto dto);

  Uni<Tuple2<User, Long>> authenticate(String usernameOrEmail, String password);

  Uni<AuthResponse> authenticateOtp(String usernameOrEmail, String otpKey);

  Uni<AuthResponse> verifyEmail(String email, String otpKey);

  Uni<User> validateOtp(String email, String otpKey);

  Uni<Tuple2<User, Long>> requestOtp(String email);
}
