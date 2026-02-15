package hotspot.user.member.service.port;

import java.util.Optional;

import hotspot.user.member.domain.Member;

public interface MemberRepository {
    Member save(Member member);
    Optional<Member> findByEmail(String email);
    Optional<Member> findById(Long id);
}
