package hotspot.user.auth.controller.port;

import hotspot.user.auth.controller.request.OnboardingRequest;
import hotspot.user.auth.controller.response.TokenResponse;

/**
 * 온보딩 서비스
 */
public interface OnboardingService {
    TokenResponse onboarding(OnboardingRequest request);
}
