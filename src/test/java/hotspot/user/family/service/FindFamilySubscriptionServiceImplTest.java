package hotspot.user.family.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.FamilyErrorCode;
import hotspot.user.family.domain.Family;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.subscription.domain.Subscription;

/**
 * 가족-회선 매핑 테이블 관련한 서비스 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class FindFamilySubscriptionServiceImplTest {

    @Mock
    private FamilySubscriptionRepository repository;

    @InjectMocks
    private FindFamilySubscriptionServiceImpl service;

    @Test
    @DisplayName("회선 ID로 가족 가입 정보를 조회하면 FamilySubscription 도메인을 반환한다")
    void findBySubIdSuccess() {
        // given
        Long subId = 1L;
        Long familyId = 10L;
        Long familySubId = 100L;

        Subscription subscription = Subscription.builder()
                .id(subId)
                .build();

        Family family = Family.builder()
                .id(familyId)
                .build();

        FamilySubscription familySubscription = FamilySubscription.builder()
                .id(familySubId)
                .subscription(subscription)
                .family(family)
                .familyRole(FamilyRole.PARENT)
                .priority(-1)
                .dataLimit(10000)
                .build();

        given(repository.findBySubId(subId)).willReturn(Optional.of(familySubscription));

        // when
        FamilySubscription response = service.findBySubId(subId);

        // then
        assertThat(response.getId()).isEqualTo(familySubId);
        assertThat(response.getSubscription().getId()).isEqualTo(subId);
        assertThat(response.getFamily().getId()).isEqualTo(familyId);
        assertThat(response.getFamilyRole()).isEqualTo(FamilyRole.PARENT);
    }

    @Test
    @DisplayName("가족에 가입되지 않은 회선 ID로 조회 시 예외가 발생한다")
    void findBySubIdFail() {
        // given
        Long subId = 999L;
        given(repository.findBySubId(subId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> service.findBySubId(subId))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(FamilyErrorCode.FAMILY_SUBSCRIPTION_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("회원 ID로 가족 가입 정보를 조회하면 FamilySubscription 도메인을 반환한다")
    void findByMemberIdSuccess() {
        // given
        Long memberId = 1L;
        Long subId = 2L;
        Long familyId = 10L;
        Long familySubId = 100L;

        Subscription subscription = Subscription.builder()
                .id(subId)
                .build();

        Family family = Family.builder()
                .id(familyId)
                .build();

        FamilySubscription familySubscription = FamilySubscription.builder()
                .id(familySubId)
                .subscription(subscription)
                .family(family)
                .familyRole(FamilyRole.PARENT)
                .priority(-1)
                .dataLimit(10000)
                .build();

        given(repository.findByMemberId(memberId)).willReturn(Optional.of(familySubscription));

        // when
        FamilySubscription response = service.findByMemberId(memberId);

        // then
        assertThat(response.getId()).isEqualTo(familySubId);
        assertThat(response.getSubscription().getId()).isEqualTo(subId);
    }
}
