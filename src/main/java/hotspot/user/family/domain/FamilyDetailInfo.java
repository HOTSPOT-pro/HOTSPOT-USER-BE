package hotspot.user.family.domain;

import java.util.List;

import hotspot.user.member.domain.MemberDetailInfo;
import lombok.Builder;
import lombok.Getter;

/**
 * 가족 전체 상세 정보를 담는 통합 도메인 모델 (Read Model)
 */
@Getter
@Builder
public class FamilyDetailInfo {
    private final Long familyId;
    private final int familyNum;
    private final List<MemberDetailInfo> memberDetailInfoList;
}
