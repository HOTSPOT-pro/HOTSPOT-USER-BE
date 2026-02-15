package hotspot.user.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.member.controller.port.RegisterSocialMemberService;
import hotspot.user.member.controller.request.CreateSocialAccountRequest;
import hotspot.user.member.domain.Member;
import hotspot.user.member.domain.SocialAccount;
import hotspot.user.member.domain.Status;
import hotspot.user.member.service.port.MemberRepository;
import lombok.RequiredArgsConstructor;

/**
 * RegisterSocialMemberService 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional
public class RegisterSocialMemberServiceImpl implements RegisterSocialMemberService {

    private final MemberRepository memberRepository;

    @Override
    public Member register(CreateSocialAccountRequest request) {
        // 1. 소셜 계정 정보 생성
        SocialAccount socialAccount = SocialAccount.create(request);

        // 2. 소셜 계정을 포함한 Member 객체 생성 후 저장
        Member member = Member.builder()
                .name(request.name())
                .status(Status.PENDING)
                .socialAccount(socialAccount)
                .build();

        return memberRepository.save(member);
    }
}
