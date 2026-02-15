package hotspot.user.common.util;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * Cookie Util 클래스 (refreshToken)
 */
@Component
public class CookieUtil {

    public static ResponseCookie createCookie(String name, String value, long maxAge) {
        return ResponseCookie.from(name, value)
                .path("/")
                .sameSite("None")
                .httpOnly(true)
                .secure(true)
                .maxAge(maxAge / 1000)
                .build();
    }

    public static ResponseCookie deleteCookie(String name) {
        return ResponseCookie.from(name, "")
                .path("/")
                .maxAge(0)
                .build();
    }
}
