package hotspot.user.member.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

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

@ExtendWith(MockitoExtension.class)
class SocialAccountRepositoryImplTest {

    @Mock
    private SocialAccountJpaRepository socialAccountJpaRepository;

    @Mock
    private MemberJpaRepository memberJpaRepository;

    @InjectMocks
    private SocialAccountRepositoryImpl socialAccountRepository;

    @Test
    @DisplayName("소셜 계정 저장 성공")
    void saveSuccess() {
        // given
        SocialAccount socialAccount = SocialAccount.builder()
                .memberId(1L)
                .email("test@test.com")
                .socialId("12345")
                .provider(Provider.KAKAO)
                .build();

        MemberEntity memberEntity = MemberEntity.builder().id(1L).build();
        SocialAccountEntity entity = SocialAccountEntity.domainToEntity(socialAccount, memberEntity);

        given(memberJpaRepository.findById(1L)).willReturn(Optional.of(memberEntity));
        given(socialAccountJpaRepository.save(any(SocialAccountEntity.class))).willReturn(entity);

        // when
        SocialAccount result = socialAccountRepository.save(socialAccount);

        // then
        assertThat(result.getEmail()).isEqualTo("test@test.com");
        verify(socialAccountJpaRepository).save(any(SocialAccountEntity.class));
    }

    @Test
    @DisplayName("memberId로 소셜 계정 조회 성공")
    void findByMemberIdSuccess() {
        // given
        Long memberId = 1L;
        MemberEntity memberEntity = MemberEntity.builder().id(memberId).build();
        SocialAccountEntity entity = SocialAccountEntity.builder()
                .id(10L)
                .member(memberEntity)
                .email("test@test.com")
                .socialId("social123")
                .provider(Provider.GOOGLE)
                .build();

        given(socialAccountJpaRepository.findByMemberId(memberId)).willReturn(Optional.of(entity));

        // when
        Optional<SocialAccount> result = socialAccountRepository.findByMemberId(memberId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@test.com");
    }

    @Test
    @DisplayName("이메일로 소셜 계정 조회 성공")
    void findByEmailSuccess() {
        // given
        String email = "test@test.com";
        MemberEntity memberEntity = MemberEntity.builder().id(1L).build();
        SocialAccountEntity entity = SocialAccountEntity.builder()
                .id(10L)
                .member(memberEntity)
                .email(email)
                .socialId("social123")
                .provider(Provider.GOOGLE)
                .build();

        given(socialAccountJpaRepository.findByEmail(email)).willReturn(Optional.of(entity));

        // when
        Optional<SocialAccount> result = socialAccountRepository.findByEmail(email);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getSocialId()).isEqualTo("social123");
    }

    @Test
    @DisplayName("소셜 계정 객체로 삭제 호출 확인")
    void deleteSuccess() {
        // given
        SocialAccount socialAccount = SocialAccount.builder()
                .memberId(1L)
                .build();
        MemberEntity memberEntity = MemberEntity.builder().id(1L).build();

        given(memberJpaRepository.findById(1L)).willReturn(Optional.of(memberEntity));

        // when
        socialAccountRepository.delete(socialAccount);

        // then
        verify(socialAccountJpaRepository).delete(any(SocialAccountEntity.class));
    }

    @Test
    @DisplayName("memberId로 소셜 계정 삭제(Soft Delete) 호출 확인")
    void deleteByMemberIdSuccess() {
        // given
        Long memberId = 1L;

        // when
        socialAccountRepository.deleteByMemberId(memberId);

        // then
        verify(socialAccountJpaRepository).deleteByMemberId(memberId);
    }
}
