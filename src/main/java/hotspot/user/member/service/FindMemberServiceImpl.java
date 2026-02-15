package hotspot.user.member.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.member.controller.port.FindMemberService;
import hotspot.user.member.domain.Member;
import hotspot.user.member.service.port.MemberRepository;
import lombok.RequiredArgsConstructor;

/**
 * FindMemberService 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindMemberServiceImpl implements FindMemberService {

    private final MemberRepository memberRepository;

    @Override
    public Optional<Member> findByEmail(String email) {
        return memberRepository.findByEmail(email);
    }
}
