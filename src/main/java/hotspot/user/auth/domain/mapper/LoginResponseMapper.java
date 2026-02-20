package hotspot.user.auth.domain.mapper;

import hotspot.user.auth.controller.response.LoginResponse;
import hotspot.user.member.controller.request.CreateSocialAccountRequest;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.member.domain.Member;
import hotspot.user.member.domain.SocialAccount;

/**
 * LoginResponse 변환을 담당하는 매퍼
 */
public class LoginResponseMapper {

    /**
     * 신규 회원 가입 시 LoginResponse 생성
     */
    public static LoginResponse from(Member member, CreateSocialAccountRequest request, Long familyId) {
        return new LoginResponse(
                member.getId(),
                request.email(),
                member.getStatus(),
                FamilyRole.CHILD, // 신규 회원의 기본 역할
                familyId
        );
    }

    /**
     * 기존 회원 로그인 시 LoginResponse 생성
     */
    public static LoginResponse from(Member member, SocialAccount socialAccount, FamilyRole familyRole, Long familyId) {
        return new LoginResponse(
                member.getId(),
                socialAccount.getEmail(),
                member.getStatus(),
                familyRole,
                familyId
        );
    }
}
