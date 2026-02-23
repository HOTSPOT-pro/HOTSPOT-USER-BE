package hotspot.user.member.domain;

import lombok.Builder;
import lombok.Getter;

/**
 * 회원 상세 정보 조회를 위한 통합 도메인 모델
 */
@Getter
@Builder
public class MemberDetailInfo {
    private final Member member;
    private final String email;
    private final String phone;      // Subscription 정보
    private final Long subId;        // Subscription 식별자
    private final FamilyRole role;   // FamilySubscription 정보
    private final Long familyId;     // 소속 가족 식별자
}
