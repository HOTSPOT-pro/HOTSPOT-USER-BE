package hotspot.user.member.domain.mapper;

import hotspot.user.member.controller.request.CreateSocialAccountRequest;
import hotspot.user.member.domain.SocialAccount;

/**
 * SocialAccount 도메인 매퍼
 */
public class SocialAccountMapper {

    public static SocialAccount toSocialAccount(CreateSocialAccountRequest request, Long memberId) {
        return SocialAccount.builder()
                .email(request.email())
                .socialId(request.socialId())
                .provider(request.provider())
                .memberId(memberId)
                .build();
    }
}
