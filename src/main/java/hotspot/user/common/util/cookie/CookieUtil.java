package hotspot.user.common.util.cookie;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * Cookie Util 클래스 (refreshToken)
 */
@Component
public class CookieUtil {

    public static ResponseCookie createCookie(String name, String value, long maxAge) {
    return ResponseCookie.from(name, value)
            // 로컬에서는 주석처리
            //.domain(".hotspot.pics")
            .path("/")
            .sameSite("None")
            .httpOnly(true)
            .secure(true)
            .maxAge(maxAge / 1000)
            .build();
}

    public static ResponseCookie deleteCookie(String name) {
        return ResponseCookie.from(name, "")
                // 로컬에서는 주석처리
                //.domain(".hotspot.pics")
                .path("/")
                .sameSite("None")
                .httpOnly(true)
                .secure(true)
                .maxAge(0)
                .build();
    }
}
