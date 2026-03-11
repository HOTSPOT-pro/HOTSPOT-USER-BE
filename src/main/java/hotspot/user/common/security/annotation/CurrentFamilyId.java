package hotspot.user.common.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 컨트롤러 파라미터에서 현재 로그인한 사용자의 최신 familyId를 주입받기 위한 어노테이션
 * DB 조회를 통해 최신 정보를 가져오므로 SecurityContext 내의 최신화되지 않은 정보 방지
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentFamilyId {
}
