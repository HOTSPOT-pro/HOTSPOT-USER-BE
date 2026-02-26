package hotspot.user.family.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.FamilyErrorCode;
import hotspot.user.family.controller.port.FindFamilySubscriptionService;
import hotspot.user.family.controller.response.FindDataLimitResponse;
import hotspot.user.family.domain.Family;
import hotspot.user.family.domain.FamilySubDataLimit;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.member.domain.FamilyRole;

@ExtendWith(MockitoExtension.class)
class FindDataLimitServiceImplTest {

    @Mock
    private FamilySubscriptionRepository familySubscriptionRepository;

    @Mock
    private FindFamilySubscriptionService familySubscriptionService;

    @InjectMocks
    private FindDataLimitServiceImpl findDataLimitService;

    @Test
    @DisplayName("성공: OWNER 권한으로 동일 가족 구성원의 데이터 한도를 조회한다")
    void findDataLimitSuccess() {
        // given
        Long targetSubId = 2L;
        Long familyId = 100L;
        FamilyRole role = FamilyRole.OWNER;

        Family family = Family.builder().id(familyId).build();
        FamilySubscription targetFs = FamilySubscription.builder()
                .family(family)
                .build();

        FamilySubDataLimit dataLimit = FamilySubDataLimit.builder()
                .name("김태연")
                .isLocked(false)
                .dataLimit(1048576L * 5) // 5GB
                .familyDataAmount(1048576L * 24) // 24GB
                .build();

        given(familySubscriptionService.findBySubId(targetSubId)).willReturn(targetFs);
        given(familySubscriptionRepository.findDataLimitBySubId(targetSubId)).willReturn(dataLimit);

        // when
        FindDataLimitResponse response = findDataLimitService.findDataLimit(targetSubId, familyId, role);

        // then
        assertThat(response.name()).isEqualTo("김태연");
        assertThat(response.isLocked()).isFalse();
        assertThat(response.dataLimit()).isEqualTo(5.0);
        assertThat(response.familyDataAmount()).isEqualTo(24.0);
    }

    @Test
    @DisplayName("실패: OWNER 권한이 아닌 사용자가 조회하려 하면 예외가 발생한다")
    void findDataLimitFailByRole() {
        // given
        Long targetSubId = 2L;
        Long familyId = 100L;

        // when & then
        assertThatThrownBy(() -> findDataLimitService.findDataLimit(targetSubId, familyId, FamilyRole.CHILD))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", FamilyErrorCode.ONLY_OWNER_CAN_MANAGE);

        assertThatThrownBy(() -> findDataLimitService.findDataLimit(targetSubId, familyId, FamilyRole.PARENT))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", FamilyErrorCode.ONLY_OWNER_CAN_MANAGE);
    }

    @Test
    @DisplayName("실패: 다른 가족 구성원의 정보를 조회하려 하면 예외가 발생한다")
    void findDataLimitFailByDifferentFamily() {
        // given
        Long targetSubId = 2L;
        Long requesterFamilyId = 100L;
        Long targetFamilyId = 200L;

        Family otherFamily = Family.builder().id(targetFamilyId).build();
        FamilySubscription targetFs = FamilySubscription.builder()
                .family(otherFamily)
                .build();

        given(familySubscriptionService.findBySubId(targetSubId)).willReturn(targetFs);

        // when & then
        assertThatThrownBy(() -> findDataLimitService.findDataLimit(targetSubId, requesterFamilyId, FamilyRole.OWNER))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", FamilyErrorCode.NOT_FAMILY_MEMBER);
    }
}
