package hotspot.user.common.crpyto;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 유저 조회 후 전화번호 표시 (복호화)
 * - 사용 키: secret_key
 * - 대상 컬럼: phone_enc
 */
@Component
public class PhoneDecryptor {
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^01\\d{8,9}$");
    private final byte[] secretKey;

    public PhoneDecryptor(@Value("${app.crypto.secret-key}") String secretKeyBase64) {
        this.secretKey = Base64.getDecoder().decode(secretKeyBase64);
        if (this.secretKey.length != 32) {
            throw new IllegalArgumentException("secret-key must be 32 bytes");
        }
    }

    public String decrypt(String phoneEnc) {
        try {
            byte[] raw = Base64.getDecoder().decode(phoneEnc); // IV(16) + ciphertext
            byte[] iv = Arrays.copyOfRange(raw, 0, 16);
            byte[] cipherText = Arrays.copyOfRange(raw, 16, raw.length);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(secretKey, "AES"), new IvParameterSpec(iv));

            String decrypted = new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
            return normalizePhone(decrypted); // 복호화 후 정규화
        } catch (Exception e) {
            throw new IllegalStateException("decrypt failed", e);
        }
    }

    /**
     * 전화번호 정규화 (01012345678 -> 010-1234-5678)
     */
    public String normalizePhone(String rawPhoneNumber) {
        if (rawPhoneNumber == null || rawPhoneNumber.isBlank()) {
            throw new IllegalArgumentException("Phone number is blank.");
        }

        String digits = rawPhoneNumber.replaceAll("\\D", "");
        if (!MOBILE_PATTERN.matcher(digits).matches()) {
            throw new IllegalArgumentException("Phone number format is invalid.");
        }

        if (digits.length() == 11) {
            return digits.substring(0, 3) + "-" + digits.substring(3, 7) + "-" + digits.substring(7);
        }

        return digits.substring(0, 3) + "-" + digits.substring(3, 6) + "-" + digits.substring(6);
    }
}
