package hotspot.user.auth.domain.mapper;

import java.util.Optional;

import hotspot.user.auth.controller.response.MemberInfoResponse;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.member.domain.Member;
import hotspot.user.member.domain.SocialAccount;
import hotspot.user.subscription.domain.Subscription;

/**
 * MemberInfo 응답 변환 매퍼
 */
public class MemberInfoMapper {

    public static MemberInfoResponse toMemberInfoResponse(
            Member member,
            SocialAccount socialAccount,
            Subscription subscription,
            FamilySubscription familySub,
            String decryptedPhone
    ) {
        return MemberInfoResponse.builder()
                .subId(subscription.getId())
                .familyId(Optional.ofNullable(familySub)
                        .map(fs -> fs.getFamily().getId())
                        .orElse(null))
                .name(member.getName())
                .email(socialAccount.getEmail())
                .phone(decryptedPhone)
                .familyRole(Optional.ofNullable(familySub)
                        .map(FamilySubscription::getFamilyRole)
                        .orElse(FamilyRole.NONE))
                .build();
    }
}
