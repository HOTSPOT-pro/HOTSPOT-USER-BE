package hotspot.user.member.service.port;

import java.util.Optional;

import hotspot.user.member.domain.Member;
import hotspot.user.member.domain.MemberDetailInfo;

public interface MemberRepository {
    Member save(Member member);
    Optional<Member> findById(Long id);
    Optional<MemberDetailInfo> findDetailById(Long id); // 멤버 상세 정보 조회할 떄 JOIN으로 가져오기
    void delete(Member member);
    void deleteById(Long id);
}
