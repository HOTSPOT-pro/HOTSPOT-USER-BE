package hotspot.user.family.domain;

import hotspot.user.member.domain.FamilyRole;
import hotspot.user.subscription.domain.Subscription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 가족-회선 매핑 도메인
 */
@Getter
@Builder
@AllArgsConstructor
public class FamilySubscription {
    private Long id;
    private Subscription subscription;
    private Family family;
    private FamilyRole familyRole;
    private int priority;
    private int dataLimit;
}
