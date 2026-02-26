package hotspot.user.family.domain;

import lombok.Builder;
import lombok.Getter;

/**
 * 가족 구성원의 공유 데이터 한도 및 즉시 차단 여부 도메인
 */
@Getter
@Builder
public class FamilySubDataLimit {
    String name;
    Boolean isLocked;
    Long dataLimit;
    Long familyDataAmount;


}
