package hotspot.user.common.crpyto;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.DecryptRequest;

/**
 * 유저 조회 후 전화번호 표시 (복호화)
 * - 대상 컬럼: phone_enc
 */
@Component
public class PhoneDecryptor {
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^01(?:0|1|[6-9])(?:\\d{3}|\\d{4})\\d{4}$");
    private static final String GCM_PREFIX = "gcm:";
    private static final int GCM_NONCE_SIZE = 12;
    private static final int GCM_TAG_SIZE = 16;
    private static final int CBC_IV_SIZE = 16;

    private final String encryptionProvider;
    private final String defaultKmsKeyId;
    private final byte[] secretKey;
    private final SubscriptionKeyLookup subscriptionKeyLookup;
    private final KmsClient kmsClient;

    public PhoneDecryptor(
            @Value("${app.crypto.encryption-provider:local}") String encryptionProvider,
            @Value("${app.crypto.kms-key-id:}") String defaultKmsKeyId,
            @Value("${app.crypto.secret-key:}") String secretKeyBase64,
            @Value("${AWS_REGION:${aws.region:ap-northeast-2}}") String awsRegion,
            SubscriptionKeyLookup subscriptionKeyLookup
    ) {
        this.encryptionProvider = Optional.ofNullable(encryptionProvider).orElse("local").toLowerCase();
        this.defaultKmsKeyId = defaultKmsKeyId;
        this.subscriptionKeyLookup = subscriptionKeyLookup;
        this.kmsClient = KmsClient.builder().region(Region.of(awsRegion)).build();

        if (secretKeyBase64 == null || secretKeyBase64.isBlank()) {
            this.secretKey = null;
        } else {
            this.secretKey = Base64.getDecoder().decode(secretKeyBase64);
            if (this.secretKey.length != 32) {
                throw new IllegalArgumentException("secret-key must be 32 bytes");
            }
        }
    }

    public String decrypt(String phoneEnc, Long subId) {
        try {
            byte[] dek = resolveDek(subId);
            String decrypted = new String(decryptPayload(phoneEnc, dek), StandardCharsets.UTF_8);
            return normalizePhone(decrypted);
        } catch (Exception e) {
            throw new IllegalStateException("decrypt failed", e);
        }
    }

    public String decrypt(String phoneEnc, SubscriptionKeyInfo keyInfo) {
        try {
            byte[] dek = resolveDek(keyInfo);
            String decrypted = new String(decryptPayload(phoneEnc, dek), StandardCharsets.UTF_8);
            return normalizePhone(decrypted);
        } catch (Exception e) {
            throw new IllegalStateException("decrypt failed", e);
        }
    }

    // Legacy fallback path for older call-sites.
    public String decrypt(String phoneEnc) {
        if (secretKey == null) {
            throw new IllegalStateException("secret-key is required for legacy decrypt");
        }

        try {
            String decrypted = new String(decryptPayload(phoneEnc, secretKey), StandardCharsets.UTF_8);
            return normalizePhone(decrypted);
        } catch (Exception e) {
            throw new IllegalStateException("decrypt failed", e);
        }
    }

    private byte[] resolveDek(Long subId) {
        SubscriptionKeyInfo keyInfo = subscriptionKeyLookup.findKeyInfoBySubId(subId)
                .orElseThrow(() -> new IllegalStateException("subscription key not found"));

        return resolveDek(keyInfo);
    }

    private byte[] resolveDek(SubscriptionKeyInfo keyInfo) {
        if (keyInfo == null) {
            throw new IllegalStateException("subscription key not found");
        }

        if ("kms".equals(encryptionProvider)) {
            String keyId = (keyInfo.kekKeyId() == null || keyInfo.kekKeyId().isBlank())
                    ? defaultKmsKeyId
                    : keyInfo.kekKeyId();

            DecryptRequest.Builder requestBuilder = DecryptRequest.builder()
                    .ciphertextBlob(SdkBytes.fromByteArray(Base64.getDecoder().decode(keyInfo.encryptedDek())));
            if (keyId != null && !keyId.isBlank()) {
                requestBuilder.keyId(keyId);
            }

            return kmsClient.decrypt(requestBuilder.build()).plaintext().asByteArray();
        }

        if (secretKey == null) {
            throw new IllegalStateException("secret-key is required for local decrypt");
        }

        return decryptPayload(keyInfo.encryptedDek(), secretKey);
    }

    private byte[] decryptPayload(String payload, byte[] key) {
        if (payload == null || payload.isBlank()) {
            throw new IllegalArgumentException("encrypted payload is blank");
        }

        if (payload.startsWith(GCM_PREFIX)) {
            return decryptGcm(payload.substring(GCM_PREFIX.length()), key);
        }

        return decryptCbc(payload, key);
    }

    private byte[] decryptGcm(String encoded, byte[] key) {
        try {
            byte[] decoded = Base64.getDecoder().decode(encoded);
            byte[] nonce = Arrays.copyOfRange(decoded, 0, GCM_NONCE_SIZE);
            byte[] ciphertextAndTag = Arrays.copyOfRange(decoded, GCM_NONCE_SIZE, decoded.length);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_SIZE * 8, nonce);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), spec);
            return cipher.doFinal(ciphertextAndTag);
        } catch (Exception e) {
            throw new IllegalStateException("gcm decrypt failed", e);
        }
    }

    private byte[] decryptCbc(String encoded, byte[] key) {
        try {
            byte[] raw = Base64.getDecoder().decode(encoded); // IV(16) + ciphertext
            byte[] iv = Arrays.copyOfRange(raw, 0, CBC_IV_SIZE);
            byte[] cipherText = Arrays.copyOfRange(raw, CBC_IV_SIZE, raw.length);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
            return cipher.doFinal(cipherText);
        } catch (Exception e) {
            throw new IllegalStateException("cbc decrypt failed", e);
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
