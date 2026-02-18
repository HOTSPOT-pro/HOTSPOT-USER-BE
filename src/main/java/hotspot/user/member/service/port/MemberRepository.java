package hotspot.user.member.service.port;

import java.util.Optional;

import hotspot.user.member.domain.Member;

public interface MemberRepository {
    Member save(Member member);
    Optional<Member> findById(Long id);
    void delete(Member member);
    void deleteById(Long id);
}
