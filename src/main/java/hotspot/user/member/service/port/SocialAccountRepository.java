package hotspot.user.member.service.port;

import java.util.Optional;

import hotspot.user.member.domain.SocialAccount;

public interface SocialAccountRepository {
    SocialAccount save(SocialAccount socialAccount);
    Optional<SocialAccount> findByMemberId(Long memberId);
    Optional<SocialAccount> findByEmail(String email);
    Optional<SocialAccount> findByMemberIdAndEmail(Long memberId, String email);
    void delete(SocialAccount socialAccount);
    void deleteByMemberId(Long memberId);
}
