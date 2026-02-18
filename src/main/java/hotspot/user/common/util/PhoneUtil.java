package hotspot.user.common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 전화번호 관련 유틸리티 (해싱 등)
 */
public class PhoneUtil {

    // 입력받은 전화번호 hash로 변환 => 사용 형태 관리자 쪽이랑 상의해보기
    public static String hashPhoneNumber(String phoneNumber) {
        try {
            // 하이픈 제거 및 공백 제거
            String cleanNumber = phoneNumber.replaceAll("[^0-9]", "");

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(cleanNumber.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
}
