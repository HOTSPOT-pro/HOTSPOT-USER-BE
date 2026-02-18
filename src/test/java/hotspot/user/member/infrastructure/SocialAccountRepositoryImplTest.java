package hotspot.user.member.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.member.domain.Provider;
import hotspot.user.member.domain.SocialAccount;
import hotspot.user.member.infrastructure.entity.MemberEntity;
import hotspot.user.member.infrastructure.entity.SocialAccountEntity;

/**
 * 소셜 계정 Repository 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class SocialAccountRepositoryImplTest {

    @Mock
    private SocialAccountJpaRepository socialAccountJpaRepository;
    @Mock
    private MemberJpaRepository memberJpaRepository;

    @InjectMocks
    private SocialAccountRepositoryImpl socialAccountRepository;

    @Test
    @DisplayName("소셜 계정 저장 성공: 연관된 멤버 엔티티를 찾아 함께 저장한다")
    void saveSocialAccountSuccess() {
        // given
        Long memberId = 1L;
        SocialAccount socialAccount = SocialAccount.builder()
                .memberId(memberId)
                .email("test@test.com")
                .provider(Provider.KAKAO)
                .socialId("abc")
                .build();

        MemberEntity memberEntity = MemberEntity.builder().id(memberId).build();
        SocialAccountEntity entity = SocialAccountEntity.builder()
                .id(10L)
                .member(memberEntity)
                .email("test@test.com")
                .build();

        given(memberJpaRepository.findById(memberId)).willReturn(Optional.of(memberEntity));
        given(socialAccountJpaRepository.save(any(SocialAccountEntity.class))).willReturn(entity);

        // when
        SocialAccount result = socialAccountRepository.save(socialAccount);

        // then
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getEmail()).isEqualTo("test@test.com");
    }

    @Test
    @DisplayName("이메일로 소셜 계정 조회 성공")
    void findByEmailSuccess() {
        // given
        String email = "test@test.com";
        SocialAccountEntity entity = SocialAccountEntity.builder()
                .id(10L)
                .email(email)
                .member(MemberEntity.builder().id(1L).build())
                .build();
        given(socialAccountJpaRepository.findByEmail(email)).willReturn(Optional.of(entity));

        // when
        Optional<SocialAccount> result = socialAccountRepository.findByEmail(email);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo(email);
    }

    @Test
    @DisplayName("멤버 ID로 소셜 계정 조회 성공")
    void findByMemberIdSuccess() {
        // given
        Long memberId = 1L;
        SocialAccountEntity entity = SocialAccountEntity.builder()
                .id(10L)
                .member(MemberEntity.builder().id(memberId).build())
                .build();
        given(socialAccountJpaRepository.findByMemberId(memberId)).willReturn(Optional.of(entity));

        // when
        Optional<SocialAccount> result = socialAccountRepository.findByMemberId(memberId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getMemberId()).isEqualTo(memberId);
    }
}
