package hotspot.user.member.service.port;

import java.util.Optional;

import hotspot.user.member.domain.Member;
import hotspot.user.member.domain.MemberDetailInfo;

public interface MemberRepository {
    Member save(Member member);
    Optional<Member> findById(Long id);
    Optional<MemberDetailInfo> findDetailByIdAndEmail(Long id, String email);
    Optional<MemberDetailInfo> findDetailById(Long id);
    void delete(Member member);
    void deleteById(Long id);
}
