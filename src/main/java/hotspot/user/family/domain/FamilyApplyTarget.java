package hotspot.user.family.domain;

import hotspot.user.member.domain.FamilyRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 가족 생성 / 구성원 추가 신청 타겟 도메인
 */
@Getter
@Builder
@AllArgsConstructor
public class FamilyApplyTarget {
    private Long id;
    private Long familyApplyId;
    private Long targetSubId;
    private FamilyRole targetFamilyRole;
}
