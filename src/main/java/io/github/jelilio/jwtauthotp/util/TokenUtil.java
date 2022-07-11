package io.github.jelilio.jwtauthotp.util;

import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.build.JwtClaimsBuilder;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Set;

public class TokenUtil {
  public static final String USER_ID = "user_id";

  public static String generateToken(
      String loginId, String username, Set<String> groups, Long duration, String issuer, String privateKeyLocation
  ) throws Exception {
    PrivateKey privateKey = readPrivateKey("/" + privateKeyLocation);

    JwtClaimsBuilder claimsBuilder = Jwt.claims();
    long currentTimeInSecs = currentTimeInSecs();

    claimsBuilder.issuer(issuer);
    claimsBuilder.subject(username);
    claimsBuilder.claim(USER_ID, loginId);
    claimsBuilder.issuedAt(currentTimeInSecs);
    claimsBuilder.expiresAt(currentTimeInSecs + duration);
    claimsBuilder.groups(groups);

    return claimsBuilder.jws().keyId(privateKeyLocation).sign(privateKey);
  }

  public static PrivateKey readPrivateKey(final String pemResName) throws Exception {
    try (InputStream contentIS = TokenUtil.class.getResourceAsStream(pemResName)) {
      byte[] tmp = new byte[4096];
      int length = contentIS.read(tmp);
      return decodePrivateKey(new String(tmp, 0, length, StandardCharsets.UTF_8));
    }
  }

  public static PrivateKey decodePrivateKey(final String pemEncoded) throws Exception {
    byte[] encodedBytes = toEncodedBytes(pemEncoded);

    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encodedBytes);
    KeyFactory kf = KeyFactory.getInstance("RSA");
    return kf.generatePrivate(keySpec);
  }

  public static byte[] toEncodedBytes(final String pemEncoded) {
    final String normalizedPem = removeBeginEnd(pemEncoded);
    return Base64.getDecoder().decode(normalizedPem);
  }

  public static String removeBeginEnd(String pem) {
    pem = pem.replaceAll("-----BEGIN (.*)-----", "");
    pem = pem.replaceAll("-----END (.*)----", "");
    pem = pem.replaceAll("\r\n", "");
    pem = pem.replaceAll("\n", "");
    return pem.trim();
  }

  public static int currentTimeInSecs() {
    long currentTimeMS = System.currentTimeMillis();
    return (int) (currentTimeMS / 1000);
  }
}
