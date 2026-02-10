package com.sisferrete.auth;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Service;

@Service
public class TotpService {
  private static final long TIME_STEP_SECONDS = 30L;
  private static final int CODE_DIGITS = 6;
  private final Base32 base32 = new Base32();

  public boolean isValid(String base32Secret, String totp) {
    if (base32Secret == null || base32Secret.isBlank() || totp == null || totp.isBlank()) {
      return false;
    }
    String trimmed = totp.trim();
    long currentCounter = System.currentTimeMillis() / 1000 / TIME_STEP_SECONDS;
    for (long offset = -1; offset <= 1; offset++) {
      String candidate = generateCode(base32Secret, currentCounter + offset);
      if (candidate.equals(trimmed)) {
        return true;
      }
    }
    return false;
  }

  private String generateCode(String base32Secret, long counter) {
    try {
      byte[] key = base32.decode(base32Secret.getBytes(StandardCharsets.UTF_8));
      Mac mac = Mac.getInstance("HmacSHA1");
      mac.init(new SecretKeySpec(key, "HmacSHA1"));
      byte[] data = ByteBuffer.allocate(8).putLong(counter).array();
      byte[] hash = mac.doFinal(data);
      int offset = hash[hash.length - 1] & 0x0F;
      int binary = ((hash[offset] & 0x7f) << 24)
          | ((hash[offset + 1] & 0xff) << 16)
          | ((hash[offset + 2] & 0xff) << 8)
          | (hash[offset + 3] & 0xff);
      int otp = binary % (int) Math.pow(10, CODE_DIGITS);
      return String.format("%0" + CODE_DIGITS + "d", otp);
    } catch (Exception ex) {
      return "";
    }
  }
}