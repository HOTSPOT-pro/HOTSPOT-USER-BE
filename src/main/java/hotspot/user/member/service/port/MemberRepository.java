package hotspot.user.member.service.port;

import java.util.Optional;

import hotspot.user.member.domain.Member;
import hotspot.user.member.domain.MemberDetailInfo;

public interface MemberRepository {
    Member save(Member member);
    Optional<Member> findById(Long id);
    Optional<MemberDetailInfo> findDetailByIdAndEmail(Long id, String email); // 소셜 계정 여러 개인걸 감안해서 email도 전달
     // 멤버 상세 정보 조회할 떄 JOIN으로 가져오기
    void delete(Member member);
    void deleteById(Long id);
}
