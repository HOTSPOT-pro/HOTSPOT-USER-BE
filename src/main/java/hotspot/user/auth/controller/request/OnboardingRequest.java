package hotspot.user.auth.controller.request;

/**
 * 온보딩 request Dto
 */
public record OnboardingRequest(
        Long memberId,
        String email,
        String phoneNumber,
        String birthDate
) {

}
