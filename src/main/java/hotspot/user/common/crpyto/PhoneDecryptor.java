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

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.CryptoErrorCode;
import software.amazon.awssdk.core.SdkBytes;
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
    private static final String PROVIDER_LOCAL = "local";
    private static final String PROVIDER_KMS = "kms";
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
            KmsClient kmsClient,
            SubscriptionKeyLookup subscriptionKeyLookup
    ) {
        this.encryptionProvider = Optional.ofNullable(encryptionProvider).orElse(PROVIDER_LOCAL).toLowerCase();
        this.defaultKmsKeyId = defaultKmsKeyId;
        this.subscriptionKeyLookup = subscriptionKeyLookup;
        this.kmsClient = kmsClient;

        if (secretKeyBase64 == null || secretKeyBase64.isBlank()) {
            this.secretKey = null;
        } else {
            this.secretKey = decodeSecretKey(secretKeyBase64);
        }
    }

    public String decrypt(String phoneEnc, Long subId) {
        byte[] dek = resolveDek(subId);
        return decryptPhoneNumber(phoneEnc, dek);
    }

    public String decrypt(String phoneEnc, SubscriptionKeyInfo keyInfo) {
        byte[] dek = resolveDek(keyInfo);
        return decryptPhoneNumber(phoneEnc, dek);
    }

    // Legacy fallback path for older call-sites.
    public String decrypt(String phoneEnc) {
        if (secretKey == null) {
            throw new ApplicationException(CryptoErrorCode.SECRET_KEY_REQUIRED);
        }

        return decryptPhoneNumber(phoneEnc, secretKey);
    }

    private byte[] resolveDek(Long subId) {
        SubscriptionKeyInfo keyInfo = subscriptionKeyLookup.findKeyInfoBySubId(subId)
                .orElseThrow(() -> new ApplicationException(CryptoErrorCode.SUBSCRIPTION_KEY_NOT_FOUND));

        return resolveDek(keyInfo);
    }

    private byte[] resolveDek(SubscriptionKeyInfo keyInfo) {
        if (keyInfo == null) {
            throw new ApplicationException(CryptoErrorCode.SUBSCRIPTION_KEY_NOT_FOUND);
        }

        if (PROVIDER_KMS.equals(encryptionProvider)) {
            String keyId = (keyInfo.kekKeyId() == null || keyInfo.kekKeyId().isBlank())
                    ? defaultKmsKeyId
                    : keyInfo.kekKeyId();

            try {
                DecryptRequest.Builder requestBuilder = DecryptRequest.builder()
                        .ciphertextBlob(SdkBytes.fromByteArray(Base64.getDecoder().decode(keyInfo.encryptedDek())));
                if (keyId != null && !keyId.isBlank()) {
                    requestBuilder.keyId(keyId);
                }

                return kmsClient.decrypt(requestBuilder.build()).plaintext().asByteArray();
            } catch (IllegalArgumentException e) {
                throw new ApplicationException(CryptoErrorCode.INVALID_ENCRYPTED_PAYLOAD, e);
            } catch (Exception e) {
                throw new ApplicationException(CryptoErrorCode.DECRYPTION_FAILED, e);
            }
        }

        if (secretKey == null) {
            throw new ApplicationException(CryptoErrorCode.SECRET_KEY_REQUIRED);
        }

        return decryptPayload(keyInfo.encryptedDek(), secretKey);
    }

    private byte[] decryptPayload(String payload, byte[] key) {
        if (payload == null || payload.isBlank()) {
            throw new ApplicationException(CryptoErrorCode.INVALID_ENCRYPTED_PAYLOAD);
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
        } catch (IllegalArgumentException e) {
            throw new ApplicationException(CryptoErrorCode.INVALID_ENCRYPTED_PAYLOAD, e);
        } catch (Exception e) {
            throw new ApplicationException(CryptoErrorCode.DECRYPTION_FAILED, e);
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
        } catch (IllegalArgumentException e) {
            throw new ApplicationException(CryptoErrorCode.INVALID_ENCRYPTED_PAYLOAD, e);
        } catch (Exception e) {
            throw new ApplicationException(CryptoErrorCode.DECRYPTION_FAILED, e);
        }
    }

    /**
     * 전화번호 정규화 (01012345678 -> 010-1234-5678)
     */
    public String normalizePhone(String rawPhoneNumber) {
        if (rawPhoneNumber == null || rawPhoneNumber.isBlank()) {
            throw new ApplicationException(CryptoErrorCode.DECRYPTION_FAILED);
        }

        String digits = rawPhoneNumber.replaceAll("\\D", "");
        if (!MOBILE_PATTERN.matcher(digits).matches()) {
            throw new ApplicationException(CryptoErrorCode.DECRYPTION_FAILED);
        }

        if (digits.length() == 11) {
            return digits.substring(0, 3) + "-" + digits.substring(3, 7) + "-" + digits.substring(7);
        }

        return digits.substring(0, 3) + "-" + digits.substring(3, 6) + "-" + digits.substring(6);
    }

    private byte[] decodeSecretKey(String secretKeyBase64) {
        try {
            byte[] decodedKey = Base64.getDecoder().decode(secretKeyBase64);
            if (decodedKey.length != 32) {
                throw new ApplicationException(CryptoErrorCode.INVALID_SECRET_KEY);
            }
            return decodedKey;
        } catch (IllegalArgumentException e) {
            throw new ApplicationException(CryptoErrorCode.INVALID_SECRET_KEY, e);
        }
    }

    private String decryptPhoneNumber(String phoneEnc, byte[] dek) {
        byte[] decrypted = decryptPayload(phoneEnc, dek);
        return normalizePhone(new String(decrypted, StandardCharsets.UTF_8));
    }
}
