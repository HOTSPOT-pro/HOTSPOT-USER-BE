package hotspot.user.common.crpyto;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 전화번호로 검색 (해시 검색)
 * - 사용 키: hash_key
 * - 대상 컬럼: phone_hash
 */
@Component
public class PhoneHashIndexer {
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^01(?:0|1|[6-9])(?:\\d{3}|\\d{4})\\d{4}$");
    private final byte[] hashKey;

    public PhoneHashIndexer(@Value("${app.crypto.hash-key}") String hashKeyBase64) {
        this.hashKey = Base64.getDecoder().decode(hashKeyBase64);
        if (this.hashKey.length != 32) {
            throw new IllegalArgumentException("hash-key must be 32 bytes");
        }
    }

    public String toHash(String rawPhoneNumber) {
        try {
            String normalizedPhone = normalizePhone(rawPhoneNumber);
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(hashKey, "HmacSHA256"));
            byte[] digest = mac.doFinal(normalizedPhone.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest); // Python과 동일
        } catch (Exception e) {
            throw new IllegalStateException("hash failed", e);
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
