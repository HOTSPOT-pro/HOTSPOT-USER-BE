package hotspot.user.family.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 가족 구성원 추가 / 신청 도메인
 */
@Getter
@Builder
@AllArgsConstructor
public class FamilyApply {
    private Long id;
    private Long requesterSubId;
    private Long familyId;
    private ApplyType applyType;
    private String docUrl;
    private ApplyStatus status;
}
