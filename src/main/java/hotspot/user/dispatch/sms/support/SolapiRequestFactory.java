package hotspot.user.dispatch.sms.support;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class SolapiRequestFactory {

    // Solapi SMS 발송 엔드포인트 URL을 생성한다.
    public String buildSendUrl(String baseUrl, String sendPath) {
        if (baseUrl.endsWith("/") && sendPath.startsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1) + sendPath;
        }
        if (!baseUrl.endsWith("/") && !sendPath.startsWith("/")) {
            return baseUrl + "/" + sendPath;
        }
        return baseUrl + sendPath;
    }

    // Solapi send-many/detail 요청 바디를 구성한다.
    public Map<String, Object> buildPayload(String from, String to, String message) {
        Map<String, String> msg = new LinkedHashMap<>();
        msg.put("to", digitsOnly(to));
        msg.put("from", digitsOnly(from));
        msg.put("text", message);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("messages", List.of(msg));
        return payload;
    }

    // 전화번호 문자열에서 숫자만 추출한다.
    private String digitsOnly(String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }
}
