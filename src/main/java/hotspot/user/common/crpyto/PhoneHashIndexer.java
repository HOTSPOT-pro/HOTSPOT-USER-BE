package hotspot.user.common.crpyto;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 전화번호로 검색 (해시 검색)
 * - 사용 키: hash_key
 * - 대상 컬럼: phone_hash
 * - Python과 동일하게 HMAC-SHA256 -> Base64로 생성해야 함
 */

@Component
public class PhoneHashIndexer {
    private final byte[] hashKey;

    public PhoneHashIndexer(@Value("${app.crypto.hash-key}") String hashKeyBase64) {
        this.hashKey = Base64.getDecoder().decode(hashKeyBase64);
        if (this.hashKey.length != 32) throw new IllegalArgumentException("hash-key must be 32 bytes");
    }

    public String toHash(String normalizedPhone) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(hashKey, "HmacSHA256"));
            byte[] digest = mac.doFinal(normalizedPhone.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest); // Python과 동일
        } catch (Exception e) {
            throw new IllegalStateException("hash failed", e);
        }
    }
}
