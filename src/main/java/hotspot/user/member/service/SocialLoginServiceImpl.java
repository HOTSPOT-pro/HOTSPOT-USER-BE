package hotspot.user.member.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.MemberErrorCode;
import hotspot.user.member.controller.port.RegisterSocialMemberService;
import hotspot.user.member.controller.port.SocialLoginService;
import hotspot.user.member.controller.request.CreateSocialAccountRequest;
import hotspot.user.member.domain.Member;
import hotspot.user.member.domain.SocialAccount;
import hotspot.user.member.service.port.MemberRepository;
import hotspot.user.member.service.port.SocialAccountRepository;
import lombok.RequiredArgsConstructor;

/**
 * SocialLoginService 구현체
 * 회원 조회 및 가입 흐름 제어
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SocialLoginServiceImpl implements SocialLoginService {

    private final SocialAccountRepository socialAccountRepository;
    private final MemberRepository memberRepository;
    private final RegisterSocialMemberService registerSocialMemberService;

    @Override
    public Member login(CreateSocialAccountRequest request) {
        // 1. 이메일로 소셜 계정 조회
        Optional<SocialAccount> socialAccount = socialAccountRepository.findByEmail(request.email());

        if (socialAccount.isPresent()) {
            // 2. 소셜 계정이 있으면 연결된 회원 정보 반환
            return memberRepository.findById(socialAccount.get().getMemberId())
                    .orElseThrow(() -> new ApplicationException(MemberErrorCode.MEMBER_NOT_FOUND));
        }

        // 3. 소셜 계정이 없으면 신규 회원 가입 진행
        return registerSocialMemberService.register(request);
    }
}
