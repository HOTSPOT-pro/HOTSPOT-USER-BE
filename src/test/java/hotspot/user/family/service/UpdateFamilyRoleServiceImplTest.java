package hotspot.user.family.service;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.AuthErrorCode;
import hotspot.user.common.exception.code.FamilyErrorCode;
import hotspot.user.family.controller.request.UpdateFamilyRoleRequest;
import hotspot.user.family.controller.response.UpdateFamilyRoleResponse;
import hotspot.user.family.domain.Family;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.subscription.service.port.SubscriptionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateFamilyRoleServiceImplTest {

    @InjectMocks
    private UpdateFamilyRoleServiceImpl updateFamilyRoleService;

    @Mock
    private FamilySubscriptionRepository familySubscriptionRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Test
    @DisplayName("성공: 타 구성원의 역할을 정상적으로 수정한다")
    void updateFamilyRoleSuccess() {
        // given
        Long requesterMemberId = 1L;
        Long requesterFamilyId = 100L;
        Long targetSubId = 2L;
        UpdateFamilyRoleRequest request = new UpdateFamilyRoleRequest(FamilyRole.PARENT);

        Subscription requesterSub = Subscription.builder().id(10L).build();
        given(subscriptionRepository.findByMemberId(requesterMemberId)).willReturn(Optional.of(requesterSub));

        Family family = Family.builder().id(requesterFamilyId).build();
        FamilySubscription targetFamilySub = FamilySubscription.builder()
                .family(family)
                .subscription(Subscription.builder().id(targetSubId).build())
                .familyRole(FamilyRole.CHILD)
                .build();
        given(familySubscriptionRepository.findBySubId(targetSubId)).willReturn(Optional.of(targetFamilySub));

        // when
        UpdateFamilyRoleResponse response = updateFamilyRoleService.update(
                requesterMemberId, requesterFamilyId, FamilyRole.OWNER, targetSubId, request);

        // then
        assertThat(response.familyRole()).isEqualTo(FamilyRole.PARENT);
        verify(familySubscriptionRepository, times(1)).save(any(FamilySubscription.class));
    }

    @Test
    @DisplayName("실패: OWNER 권한이 아닌 사용자가 수정을 시도하면 예외가 발생한다")
    void updateFamilyRoleFailByRole() {
        // given
        UpdateFamilyRoleRequest request = new UpdateFamilyRoleRequest(FamilyRole.PARENT);

        // when & then
        assertThatThrownBy(() -> updateFamilyRoleService.update(1L, 100L, FamilyRole.PARENT, 2L, request))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(AuthErrorCode.ACCESS_DENIED.getMessage());
    }

    @Test
    @DisplayName("실패: OWNER 본인의 역할을 수정하려 하면 예외가 발생한다")
    void updateFamilyRoleFailBySelfChange() {
        // given
        Long requesterMemberId = 1L;
        Long targetSubId = 10L;
        UpdateFamilyRoleRequest request = new UpdateFamilyRoleRequest(FamilyRole.PARENT);

        Subscription requesterSub = Subscription.builder().id(targetSubId).build();
        given(subscriptionRepository.findByMemberId(requesterMemberId)).willReturn(Optional.of(requesterSub));

        // when & then
        assertThatThrownBy(() -> updateFamilyRoleService.update(requesterMemberId, 100L, FamilyRole.OWNER, targetSubId, request))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(FamilyErrorCode.CANNOT_CHANGE_OWNER_ROLE.getMessage());
    }

    @Test
    @DisplayName("실패: 타인을 OWNER로 변경하려 하면 예외가 발생한다")
    void updateFamilyRoleFailByAssignOwner() {
        // given
        Long requesterMemberId = 1L;
        Long requesterFamilyId = 100L;
        Long targetSubId = 2L;
        UpdateFamilyRoleRequest request = new UpdateFamilyRoleRequest(FamilyRole.OWNER);

        Subscription requesterSub = Subscription.builder().id(10L).build();
        given(subscriptionRepository.findByMemberId(requesterMemberId)).willReturn(Optional.of(requesterSub));

        Family family = Family.builder().id(requesterFamilyId).build();
        FamilySubscription targetFamilySub = FamilySubscription.builder()
                .family(family)
                .subscription(Subscription.builder().id(targetSubId).build())
                .familyRole(FamilyRole.CHILD)
                .build();
        given(familySubscriptionRepository.findBySubId(targetSubId)).willReturn(Optional.of(targetFamilySub));

        // when & then
        assertThatThrownBy(() -> updateFamilyRoleService.update(requesterMemberId, requesterFamilyId, FamilyRole.OWNER, targetSubId, request))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(FamilyErrorCode.CANNOT_ASSIGN_OWNER_ROLE.getMessage());
    }

    @Test
    @DisplayName("실패: 다른 가족 구성원의 역할을 수정하려 하면 예외가 발생한다")
    void updateFamilyRoleFailByDifferentFamily() {
        // given
        Long requesterMemberId = 1L;
        Long requesterFamilyId = 100L;
        Long targetSubId = 2L;
        UpdateFamilyRoleRequest request = new UpdateFamilyRoleRequest(FamilyRole.PARENT);

        Subscription requesterSub = Subscription.builder().id(10L).build();
        given(subscriptionRepository.findByMemberId(requesterMemberId)).willReturn(Optional.of(requesterSub));

        Family otherFamily = Family.builder().id(200L).build();
        FamilySubscription targetFamilySub = FamilySubscription.builder()
                .family(otherFamily)
                .subscription(Subscription.builder().id(targetSubId).build())
                .build();
        given(familySubscriptionRepository.findBySubId(targetSubId)).willReturn(Optional.of(targetFamilySub));

        // when & then
        assertThatThrownBy(() -> updateFamilyRoleService.update(requesterMemberId, requesterFamilyId, FamilyRole.OWNER, targetSubId, request))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(FamilyErrorCode.NOT_FAMILY_MEMBER.getMessage());
    }
}
