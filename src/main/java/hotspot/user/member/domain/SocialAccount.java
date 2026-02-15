package hotspot.user.member.domain;

import hotspot.user.member.controller.request.CreateSocialAccountRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SocialAccount {
    private final Long id;
    private final String email;
    private final String socialId;
    private final Provider provider;
    private final Long memberId; // 소속된 회원의 ID (참조용)

    public static SocialAccount create(CreateSocialAccountRequest request) {
        return SocialAccount.builder()
                .email(request.email())
                .socialId(request.socialId())
                .provider(request.provider())
                .memberId(request.memberId())
                .build();
    }
}
