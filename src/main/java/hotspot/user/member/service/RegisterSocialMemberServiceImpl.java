package hotspot.user.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.auth.controller.response.LoginResponse;
import hotspot.user.auth.domain.mapper.LoginResponseMapper;
import hotspot.user.member.controller.port.RegisterSocialMemberService;
import hotspot.user.member.controller.request.CreateSocialAccountRequest;
import hotspot.user.member.domain.Member;
import hotspot.user.member.domain.SocialAccount;
import hotspot.user.member.domain.mapper.MemberMapper;
import hotspot.user.member.domain.mapper.SocialAccountMapper;
import hotspot.user.member.service.port.MemberRepository;
import hotspot.user.member.service.port.SocialAccountRepository;
import lombok.RequiredArgsConstructor;

/**
 * RegisterSocialMemberService 구현체
 */

@Service
@RequiredArgsConstructor
@Transactional
public class RegisterSocialMemberServiceImpl implements RegisterSocialMemberService {

    private final MemberRepository memberRepository;
    private final SocialAccountRepository socialAccountRepository;

    @Override
    public LoginResponse register(CreateSocialAccountRequest request) {
        // 1. Member 생성 및 저장
        Member member = memberRepository.save(MemberMapper.toMember(request));

        // 2. 소셜 계정 정보 생성 (Mapper 사용)
        SocialAccount socialAccount = SocialAccountMapper.toSocialAccount(request, member.getId());

        // 3. 소셜 계정 저장
        socialAccountRepository.save(socialAccount);

        // 4. LoginResponseMapper를 사용하여 DTO 반환
        return LoginResponseMapper.from(member, request, null);
    }
}
