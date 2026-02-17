package hotspot.user.member.domain.mapper;

import hotspot.user.member.controller.request.CreateSocialAccountRequest;
import hotspot.user.member.controller.response.MemberResponse;
import hotspot.user.member.domain.Member;
import hotspot.user.member.domain.SocialAccount;
import hotspot.user.member.domain.Status;
import hotspot.user.subscription.domain.Subscription;

/**
 * Member 도메인과 DTO 간의 변환을 담당하는 매퍼
 */
public class MemberMapper {

    // Request -> Domain (소셜 회원가입 시)
    public static Member toMember(CreateSocialAccountRequest request) {
        return Member.builder()
                .name(request.name())
                .status(Status.PENDING)
                .build();
    }

    // Domain -> Response (회원 정보 조회 시 조립)
    public static MemberResponse toResponse(Member member, SocialAccount socialAccount, Subscription subscription) {
        String email = (socialAccount != null) ? socialAccount.getEmail() : null;
        String phone = (subscription != null) ? subscription.getPhoneEnc() : null;

        return MemberResponse.of(member, email, phone);
    }
}
