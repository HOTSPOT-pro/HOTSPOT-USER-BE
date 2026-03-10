package hotspot.user.subscription.domain;

import hotspot.user.member.domain.Member;
import hotspot.user.plan.domain.Plan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 회선 도메인
 */

@Getter
@Builder
@AllArgsConstructor
public class Subscription {
    private Long id;
    private Plan plan;
    private Member member;
    private String phoneEnc;
    private String phoneHash;
    private Integer phoneKeyBucketId;
    private Integer phoneKeyVersion;
    private Boolean isLocked;

    public Subscription updateMember(Member member) {
        return Subscription.builder()
                .id(this.id)
                .plan(this.plan)
                .member(member)
                .phoneEnc(this.phoneEnc)
                .phoneHash(this.phoneHash)
                .phoneKeyBucketId(this.phoneKeyBucketId)
                .phoneKeyVersion(this.phoneKeyVersion)
                .isLocked(this.isLocked)
                .build();
    }
}
