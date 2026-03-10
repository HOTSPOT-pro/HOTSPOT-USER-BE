package hotspot.user.common.crpyto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.CryptoErrorCode;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.DecryptRequest;
import software.amazon.awssdk.services.kms.model.DecryptResponse;

@ExtendWith(MockitoExtension.class)
class PhoneDecryptorTest {

    private static final byte[] SECRET_KEY = sequentialBytes(1);
    private static final byte[] DEK = sequentialBytes(2);
    private static final String PHONE = "01012345678";
    private static final String NORMALIZED_PHONE = "010-1234-5678";

    @Mock
    private SubscriptionKeyLookup subscriptionKeyLookup;

    @Mock
    private KmsClient kmsClient;

    @Test
    @DisplayName("local provider에서 GCM 전화번호 복호화에 성공한다")
    void decryptLocalGcmSuccess() throws Exception {
        PhoneDecryptor decryptor = new PhoneDecryptor(
                "local",
                "",
                encode(SECRET_KEY),
                kmsClient,
                subscriptionKeyLookup
        );
        String encryptedDek = encryptGcm(DEK, SECRET_KEY);
        String encryptedPhone = encryptGcm(PHONE.getBytes(StandardCharsets.UTF_8), DEK);

        given(subscriptionKeyLookup.findKeyInfoBySubId(1L))
                .willReturn(Optional.of(new SubscriptionKeyInfo(encryptedDek, "")));

        assertThat(decryptor.decrypt(encryptedPhone, 1L)).isEqualTo(NORMALIZED_PHONE);
    }

    @Test
    @DisplayName("local provider에서 CBC 레거시 전화번호 복호화에 성공한다")
    void decryptLocalCbcSuccess() throws Exception {
        PhoneDecryptor decryptor = new PhoneDecryptor(
                "local",
                "",
                encode(SECRET_KEY),
                kmsClient,
                subscriptionKeyLookup
        );
        String encryptedDek = encryptGcm(DEK, SECRET_KEY);
        String encryptedPhone = encryptCbc(PHONE.getBytes(StandardCharsets.UTF_8), DEK);

        given(subscriptionKeyLookup.findKeyInfoBySubId(2L))
                .willReturn(Optional.of(new SubscriptionKeyInfo(encryptedDek, "")));

        assertThat(decryptor.decrypt(encryptedPhone, 2L)).isEqualTo(NORMALIZED_PHONE);
    }

    @Test
    @DisplayName("kms provider에서 KMS로 DEK를 복원해 전화번호 복호화에 성공한다")
    void decryptKmsSuccess() throws Exception {
        PhoneDecryptor decryptor = new PhoneDecryptor(
                "kms",
                "default-kek",
                "",
                kmsClient,
                subscriptionKeyLookup
        );
        String encryptedPhone = encryptGcm(PHONE.getBytes(StandardCharsets.UTF_8), DEK);
        String encryptedDek = Base64.getEncoder().encodeToString("kms-cipher".getBytes(StandardCharsets.UTF_8));

        given(subscriptionKeyLookup.findKeyInfoBySubId(3L))
                .willReturn(Optional.of(new SubscriptionKeyInfo(encryptedDek, "kek-arn")));
        given(kmsClient.decrypt(any(DecryptRequest.class)))
                .willReturn(DecryptResponse.builder().plaintext(SdkBytes.fromByteArray(DEK)).build());

        assertThat(decryptor.decrypt(encryptedPhone, 3L)).isEqualTo(NORMALIZED_PHONE);
    }

    @Test
    @DisplayName("subscription key가 없으면 예외가 발생한다")
    void decryptFailsWhenSubscriptionKeyMissing() {
        PhoneDecryptor decryptor = new PhoneDecryptor(
                "local",
                "",
                encode(SECRET_KEY),
                kmsClient,
                subscriptionKeyLookup
        );

        given(subscriptionKeyLookup.findKeyInfoBySubId(4L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> decryptor.decrypt("gcm:abcd", 4L))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(CryptoErrorCode.SUBSCRIPTION_KEY_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("local provider에서 SECRET_KEY가 없으면 예외가 발생한다")
    void decryptFailsWhenLocalSecretKeyMissing() {
        PhoneDecryptor decryptor = new PhoneDecryptor(
                "local",
                "",
                "",
                kmsClient,
                subscriptionKeyLookup
        );

        given(subscriptionKeyLookup.findKeyInfoBySubId(5L))
                .willReturn(Optional.of(new SubscriptionKeyInfo("gcm:abcd", "")));

        assertThatThrownBy(() -> decryptor.decrypt("gcm:abcd", 5L))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(CryptoErrorCode.SECRET_KEY_REQUIRED.getMessage());
    }

    @Test
    @DisplayName("키가 일치하지 않으면 복호화 실패 예외가 발생한다")
    void decryptFailsWhenKeyMismatched() throws Exception {
        PhoneDecryptor decryptor = new PhoneDecryptor(
                "local",
                "",
                encode(SECRET_KEY),
                kmsClient,
                subscriptionKeyLookup
        );
        byte[] wrongDek = sequentialBytes(9);
        String encryptedDek = encryptGcm(DEK, SECRET_KEY);
        String encryptedPhone = encryptGcm(PHONE.getBytes(StandardCharsets.UTF_8), wrongDek);

        given(subscriptionKeyLookup.findKeyInfoBySubId(6L))
                .willReturn(Optional.of(new SubscriptionKeyInfo(encryptedDek, "")));

        assertThatThrownBy(() -> decryptor.decrypt(encryptedPhone, 6L))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(CryptoErrorCode.DECRYPTION_FAILED.getMessage());
    }

    private static String encryptGcm(byte[] plainBytes, byte[] key) throws Exception {
        byte[] nonce = new byte[12];
        new SecureRandom().nextBytes(nonce);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, nonce));
        byte[] encrypted = cipher.doFinal(plainBytes);

        byte[] payload = new byte[nonce.length + encrypted.length];
        System.arraycopy(nonce, 0, payload, 0, nonce.length);
        System.arraycopy(encrypted, 0, payload, nonce.length, encrypted.length);
        return "gcm:" + Base64.getEncoder().encodeToString(payload);
    }

    private static String encryptCbc(byte[] plainBytes, byte[] key) throws Exception {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
        byte[] encrypted = cipher.doFinal(plainBytes);

        byte[] payload = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, payload, 0, iv.length);
        System.arraycopy(encrypted, 0, payload, iv.length, encrypted.length);
        return Base64.getEncoder().encodeToString(payload);
    }

    private static String encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    private static byte[] sequentialBytes(int seed) {
        byte[] bytes = new byte[32];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) (seed + i);
        }
        return bytes;
    }
}
