package hotspot.user.family.controller.port;

import hotspot.user.family.domain.FamilySubscription;

/**
 * 가족 구성원(회선) 조회 서비스 포트
 */
public interface FindFamilySubscriptionService {
    FamilySubscription findBySubId(Long subId);
    FamilySubscription findByMemberId(Long memberId);
}
