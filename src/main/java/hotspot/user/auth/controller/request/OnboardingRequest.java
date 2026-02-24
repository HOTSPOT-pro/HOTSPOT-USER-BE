package hotspot.user.auth.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 온보딩 request Dto
 */
public record OnboardingRequest(
        @NotBlank(message = "전화번호는 필수입니다.")
        @Pattern(regexp = "^01(?:0|1|[6-9])(?:\\d{3}|\\d{4})\\d{4}$", message = "올바른 전화번호 형식이 아닙니다.")
        String phoneNumber,

        @NotBlank(message = "생년월일은 필수입니다.")
        @Pattern(regexp = "^\\d{6}$", message = "생년월일은 6자리 숫자여야 합니다. (YYMMDD)")
        String birthDate
) {
}
