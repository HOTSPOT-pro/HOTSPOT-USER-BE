package hotspot.user.common.security.resolver;

import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.common.security.annotation.CurrentFamilyId;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import lombok.RequiredArgsConstructor;

/**
 * @CurrentFamilyId 어노테이션이 붙은 파라미터에 대해 DB 조회를 수행하여 최신 familyId를 주입
 */
@Component
@RequiredArgsConstructor
public class CurrentFamilyIdResolver implements HandlerMethodArgumentResolver {

    private final FamilySubscriptionRepository familySubscriptionRepository;

    /**
     * 지원하는 파라미터인지 확인
     * @param parameter 검사할 파라미터
     * @return @CurrentFamilyId 어노테이션이 붙어있고 Long 타입이면 true
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasAnnotation = parameter.hasParameterAnnotation(CurrentFamilyId.class);
        boolean isLongType = parameter.getParameterType().equals(Long.class);
        return hasAnnotation && isLongType;
    }

    /**
     * 파라미터에 주입할 값을 결정
     * SecurityContext에서 memberId를 추출한 뒤 DB에서 최신 familyId를 조회
     * @return DB에서 조회된 최신 familyId, 없거나 로그인 정보가 없으면 null
     */
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof PrincipalDetails principal)) {
            return null;
        }

        // DB에서 최신 familyId를 실시간으로 조회하여 반환
        return familySubscriptionRepository.findFamilyIdByMemberId(principal.getId())
                .orElse(null);
    }
}
