package hotspot.user.member.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.member.controller.port.LoadSocialUserService;
import hotspot.user.member.controller.request.CreateSocialAccountRequest;
import hotspot.user.member.domain.Member;
import hotspot.user.member.domain.SocialAccount;
import hotspot.user.member.domain.Status;
import hotspot.user.member.service.port.MemberRepository;
import lombok.RequiredArgsConstructor;

/**
 * LoadSocialUserService 구현체
 * 소셜 로그인 성공 후 이메일을 기반으로 기존 회원을 조회하고,
 * 존재하지 않을 경우 PENDING 상태의 신규 회원 및 소셜 계정 정보를 생성합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class LoadSocialUserServiceImpl implements LoadSocialUserService {

    private final MemberRepository memberRepository;

    @Override
    public Member loadOrCreateMember(String name, CreateSocialAccountRequest request) {
        // 1. 이메일로 기존 회원 확인
        return memberRepository.findByEmail(request.email())
                .orElseGet(() -> createNewMember(name, request));
    }

    private Member createNewMember(String name, CreateSocialAccountRequest request) {
        // 2. 신규 회원 생성 (상태는 PENDING)
        Member member = Member.builder()
                .name(name)
                .status(Status.PENDING)
                .socialAccountList(new ArrayList<>())
                .build();

        // 3. Member 저장하여 ID 확보
        Member savedMember = memberRepository.save(member);

        // 4. 소셜 계정 정보 생성 및 연결
        // record의 값을 사용하되, 저장된 memberId를 주입하여 새로 요청 생성 (불변성 고려)
        CreateSocialAccountRequest accountRequest = new CreateSocialAccountRequest(
                request.email(),
                request.socialId(),
                request.provider(),
                savedMember.getId()
        );

        SocialAccount socialAccount = SocialAccount.create(accountRequest);

        // 5. Member에 소셜 계정 추가 후 최종 저장
        List<SocialAccount> socialAccounts = new ArrayList<>();
        socialAccounts.add(socialAccount);

        Member finalMember = Member.builder()
                .id(savedMember.getId())
                .name(savedMember.getName())
                .status(savedMember.getStatus())
                .socialAccountList(socialAccounts)
                .build();

        return memberRepository.save(finalMember);
    }
}
