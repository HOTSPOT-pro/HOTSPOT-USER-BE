package hotspot.user.common.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import hotspot.user.common.security.resolver.CurrentFamilyIdResolver;
import lombok.RequiredArgsConstructor;

/**
 * Web MVC 설정
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final CurrentFamilyIdResolver currentFamilyIdResolver;

    /**
     * 커스텀 ArgumentResolver 등록
     * @param resolvers 등록할 Resolver 리스트
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentFamilyIdResolver);
    }
}
