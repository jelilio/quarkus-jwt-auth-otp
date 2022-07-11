package io.github.jelilio.jwtauthotp.service.impl;

import io.github.jelilio.jwtauthotp.dto.BasicRegisterDto;
import io.github.jelilio.jwtauthotp.entity.Role;
import io.github.jelilio.jwtauthotp.entity.User;
import io.github.jelilio.jwtauthotp.exception.AlreadyExistException;
import io.github.jelilio.jwtauthotp.exception.AuthenticationException;
import io.github.jelilio.jwtauthotp.model.AuthResponse;
import io.github.jelilio.jwtauthotp.service.MailerService;
import io.github.jelilio.jwtauthotp.service.RoleService;
import io.github.jelilio.jwtauthotp.service.UserService;
import io.github.jelilio.jwtauthotp.util.*;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Page;
import io.quarkus.redis.client.reactive.ReactiveRedisClient;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import io.vertx.mutiny.redis.client.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.github.jelilio.jwtauthotp.exception.AuthenticationException.*;

@ApplicationScoped
public class UserServiceImpl implements UserService {
  private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
  
  @Inject
  PBKDF2Encoder passwordEncoder;

  @Inject
  RandomUtil randomUtil;
  
  @Inject
  ReactiveRedisClient reactiveRedisClient;

  @Inject
  RoleService roleService;

  @Inject
  MailerService mailerService;

  @ConfigProperty(name = "com.japharr.ajo-app.jwt.duration")
  Long duration;
  
  @ConfigProperty(name = "mp.jwt.verify.issuer")
  String issuer;

  @ConfigProperty(name = "com.japharr.ajo-app.otp.duration")
  Long otpKeyDuration; // in seconds

  @Override
  public Uni<User> findById(String id) {
    return User.findById(id).onItem().ifNull()
        .failWith(() -> new NotFoundException("Not found"));
  }
  
  @Override
  public Uni<Paged<User>> findAll(int size, int index) {
    Page page = Page.of(index, size);

    return PaginationUtil.paginate(page, User.findAll().page(page));
  }
  
  @Override
  public Uni<Boolean> checkIfEmailAvailable(String email) {
    return User.countByEmailAvailable(email)
        .onItem().transform(count -> count > 0);
  }

  public Uni<Tuple2<User, Long>> register(BasicRegisterDto dto) {
    return create(dto);
  }

  @Override
  public Uni<Tuple2<User, Long>> authenticate(String usernameOrEmail, String password) {
    logger.debug("usernameOrEmail: {}, password: {}", usernameOrEmail, password);

    var invalidAuthentication = new AuthenticationException("Invalid username or newPassword", AUTH_LOGIN_INVALID);

    Uni<User> userUni = User.findByUsernameOrEmail(usernameOrEmail)
        .onItem().ifNull().failWith(() -> invalidAuthentication);

    return userUni.flatMap(user -> {
      if(!user.enabled) {
        return Uni.createFrom().failure(() -> new AuthenticationException("Your account has been disabled, contact your administrator", AUTH_LOGIN_DISABLED));
      }

      if(user.email.equalsIgnoreCase(usernameOrEmail) && !user.isActivated()) {
        return Uni.createFrom().failure(() -> new AuthenticationException("Please, verify your email address", AUTH_VERIFY_EMAIL));
      }

      if(user.password.equals(passwordEncoder.encode(password))) {
        return createOtp(usernameOrEmail, user);
      }

      return Uni.createFrom().failure(() -> invalidAuthentication);
    });
  }

  @Override
  public Uni<AuthResponse> authenticateOtp(String usernameOrEmail, String otpKey) {
    return validateOtp(usernameOrEmail, otpKey).flatMap(user -> {
      user.lastLoginDate = Instant.now();
      return Panache.<User>withTransaction(user::persist);
    }).flatMap(this::createToken);
  }

  @Override
  public Uni<AuthResponse> verifyEmail(String email, String otpKey) {
    return validateOtp(email, otpKey).flatMap(user -> {
      user.activate();
      user.lastLoginDate = Instant.now();
      return Panache.<User>withTransaction(user::persist);
    }).flatMap(user -> mailerService.sendActivationMail(user)
        .flatMap(__ -> createToken(user)));
  }

  @Override
  public Uni<User> validateOtp(String usernameOrEmail, String otpKey) {
    Uni<User> userUni = User.findByUsernameOrEmail(usernameOrEmail)
        .onItem().ifNull().failWith(() -> new AuthenticationException("No user with this email/username found", AUTH_LOGIN_INVALID));

    return userUni.flatMap(login -> {
      if(!login.enabled) {
        return Uni.createFrom().failure(() -> new AuthenticationException("Your account has been disabled, contact your administrator", AUTH_LOGIN_DISABLED));
      }

      if(login.isActivated()) {
        return Uni.createFrom().failure(() -> new AuthenticationException("Already activated", AUTH_LOGIN_ACTIVATED));
      }

      return reactiveRedisClient.get(usernameOrEmail).flatMap(response -> {
        if(response == null) {
          return Uni.createFrom().failure(() -> new AuthenticationException("Expired OTP", AUTH_OTP_EXPIRED));
        }

        if(!response.toString().equalsIgnoreCase(otpKey)) {
          return Uni.createFrom().failure(() -> new AuthenticationException("Invalid OTP", AUTH_OTP_INVALID));
        }

        return Uni.createFrom().item(login);
      });
    });
  }

  @Override
  public Uni<Tuple2<User, Long>> requestOtp(String usernameOrEmail) {
    Uni<User> loginUni = User.findByUsernameOrEmail(usernameOrEmail)
        .onItem().ifNull().failWith(() -> new AuthenticationException("Email or username not registered", AUTH_LOGIN_INVALID));

    return loginUni.flatMap(login -> {
      if (!login.enabled) {
        return Uni.createFrom().failure(() -> new AuthenticationException("Your account has been disabled, contact your administrator", AUTH_LOGIN_DISABLED));
      }

      if(login.isActivated()) {
        return Uni.createFrom().failure(() -> new AuthenticationException("Already activated", AUTH_LOGIN_ACTIVATED));
      }

      return createOtp(usernameOrEmail, login);
    });
  }

  private Uni<Tuple2<User, Long>> create(BasicRegisterDto dto) {
    logger.debug("creating: email: {}, name: {}", dto.email(), dto.name());
    // check if email is already registered and activated
    Uni<Boolean> uniEmailCount = checkIfEmailAvailable(dto.email());
    // fetch User if email is already registered but not activated
    Uni<User> extLoginUni = User.findByEmailAndNotActivated(dto.email());
    Uni<List<Role>> uniRole = roleService.getOrCreate(Set.of("ROLE_USER"));

    return uniRole.flatMap(role -> Uni.combine().all()
        .unis(uniEmailCount, extLoginUni).asTuple().flatMap(item -> {
          var emailCount = item.getItem1();

          if(emailCount) {
            return Uni.createFrom().failure(() -> new AlreadyExistException("Email already in used"));
          }

          final User user = item.getItem2() != null? item.getItem2() : new User();
          user.name = dto.name();
          user.email = dto.email();
          user.password = passwordEncoder.encode(dto.password());
          user.roles = new HashSet<>(role);
          logger.debug("creating: User: {}", user);

          return Panache.<User>withTransaction(user::persist)
              .flatMap(updated -> createOtp(dto.email(), updated));
        }));
  }

  private Uni<Tuple2<User, Long>> createOtp(String usernameOrEmail, User user) {
    var otpKey = randomUtil.generateOtp();
    Uni<Response> responseUni = reactiveRedisClient.setex(
        usernameOrEmail, otpKey, String.valueOf(otpKeyDuration));

    return responseUni
        .flatMap(__ -> mailerService.sendOtpMail(user, otpKey, otpKeyDuration)
            .map(it -> Tuple2.of(user, otpKeyDuration)));
  }

  private Uni<AuthResponse> createToken(User login) {
    try {
      Uni<AuthResponse> response = Uni.createFrom().item(new AuthResponse(TokenUtil.generateToken(login.id.toString(), login.email,
          login.getRoles(), duration, issuer)));

      login.lastLoginDate = Instant.now();
      return Panache.withTransaction(login::persist).flatMap(__ -> response);
    } catch (Exception e) {
      return Uni.createFrom().failure(() -> new AuthenticationException("Unable to generate token, try again later", AUTH_BAD_TOKEN));
    }
  }
}
