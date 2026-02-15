package hotspot.user.member.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.member.controller.port.FindMemberService;
import hotspot.user.member.controller.port.RegisterSocialMemberService;
import hotspot.user.member.controller.port.SocialLoginService;
import hotspot.user.member.controller.request.CreateSocialAccountRequest;
import hotspot.user.member.domain.Member;
import lombok.RequiredArgsConstructor;

/**
 * SocialLoginService 구현체
 * 회원 조회 및 가입 흐름 제어
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SocialLoginServiceImpl implements SocialLoginService {

    private final FindMemberService findMemberService;
    private final RegisterSocialMemberService registerSocialMemberService;

    @Override
    public Member login(CreateSocialAccountRequest request) {
        // 1. 이메일로 기존 회원 확인
        Optional<Member> member = findMemberService.findByEmail(request.email());

        // 2. 존재하면 반환, 없으면 가입
        return member.orElseGet(() -> registerSocialMemberService.register(request));
    }
}
