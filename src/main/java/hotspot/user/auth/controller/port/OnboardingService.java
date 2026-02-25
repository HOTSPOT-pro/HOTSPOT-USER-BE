package hotspot.user.auth.controller.port;

import hotspot.user.auth.controller.request.OnboardingRequest;
import hotspot.user.auth.controller.response.OnboardingResponse;

/**
 * 온보딩 서비스
 */
public interface OnboardingService {
    OnboardingResponse onboarding(Long memberId, String email, OnboardingRequest request);
}
