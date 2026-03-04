package hotspot.user.dispatch.sms.support;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.SmsErrorCode;

@Component
public class SolapiAuthHeaderFactory {

    // Solapi HMAC 인증 헤더를 생성한다.
    public String create(String apiKey, String apiSecret) {
        String date = Instant.now().truncatedTo(ChronoUnit.SECONDS).toString();
        String salt = UUID.randomUUID().toString().replace("-", "");
        return create(apiKey, apiSecret, date, salt);
    }

    // Solapi HMAC 인증 헤더를 지정한 date/salt로 생성한다.
    String create(String apiKey, String apiSecret, String date, String salt) {
        String signature = hmacSha256Hex(apiSecret, date + salt);
        return "HMAC-SHA256 apiKey=" + apiKey
                + ", date=" + date
                + ", salt=" + salt
                + ", signature=" + signature;
    }

    // HMAC-SHA256 서명을 hex 문자열로 생성한다.
    private String hmacSha256Hex(String secret, String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] raw = mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return toHex(raw);
        } catch (Exception ex) {
            throw new ApplicationException(SmsErrorCode.SMS_PROVIDER_AUTH_FAILED, ex);
        }
    }

    // byte 배열을 소문자 hex 문자열로 변환한다.
    private String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }
}
