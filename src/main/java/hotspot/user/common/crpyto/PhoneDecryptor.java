package hotspot.user.common.crpyto;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
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
    private final byte[] secretKey;

    public PhoneDecryptor(@Value("${app.crypto.secret-key}") String secretKeyBase64) {
        this.secretKey = Base64.getDecoder().decode(secretKeyBase64);
        if (this.secretKey.length != 32) throw new IllegalArgumentException("secret-key must be 32 bytes");
    }

    public String decrypt(String phoneEnc) {
        try {
            byte[] raw = Base64.getDecoder().decode(phoneEnc); // IV(16) + ciphertext
            byte[] iv = Arrays.copyOfRange(raw, 0, 16);
            byte[] cipherText = Arrays.copyOfRange(raw, 16, raw.length);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(secretKey, "AES"), new IvParameterSpec(iv));
            return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("decrypt failed", e);
        }
    }
}
