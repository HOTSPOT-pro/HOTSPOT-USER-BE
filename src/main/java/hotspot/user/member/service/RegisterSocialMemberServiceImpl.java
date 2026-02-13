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
        // 1. 신규 회원 생성 (상태는 PENDING)
        Member member = Member.builder()
                .name(request.name())
                .status(Status.PENDING)
                .build();

        // 2. Member 저장하여 ID 확보
        Member savedMember = memberRepository.save(member);

        // 3. 소셜 계정 정보 생성 및 연결 (memberId 사용)
        CreateSocialAccountRequest accountRequest = new CreateSocialAccountRequest(
                request.name(),
                request.email(),
                request.socialId(),
                request.provider(),
                savedMember.getId()
        );

        SocialAccount socialAccount = SocialAccount.create(accountRequest);

        // 4. Member에 소셜 계정 추가 후 최종 저장
        Member finalMember = Member.builder()
                .id(savedMember.getId())
                .name(savedMember.getName())
                .status(savedMember.getStatus())
                .socialAccount(socialAccount)
                .build();

        return memberRepository.save(finalMember);
    }
}
