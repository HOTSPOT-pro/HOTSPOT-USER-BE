package hotspot.user.member.controller.request;


import hotspot.user.member.domain.Provider;

/**
 * SocialAccount 요청 Dto
 * 소셜 로그인 정보를 담는 객체 (Member 정보 포함)
 */
public record CreateSocialAccountRequest(
    String name,
    String email,
    String socialId,
    Provider provider,
    Long memberId
) {
}
