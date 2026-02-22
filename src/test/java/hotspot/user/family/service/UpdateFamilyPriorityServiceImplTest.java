package hotspot.user.family.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.AuthErrorCode;
import hotspot.user.common.exception.code.FamilyErrorCode;
import hotspot.user.family.controller.request.MemberPriorityRequest;
import hotspot.user.family.controller.request.UpdateFamilyPriorityRequest;
import hotspot.user.family.controller.response.UpdateFamilyPriorityResponse;
import hotspot.user.family.domain.Family;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.domain.PriorityType;
import hotspot.user.family.service.port.FamilyRepository;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.subscription.domain.Subscription;

@ExtendWith(MockitoExtension.class)
class UpdateFamilyPriorityServiceImplTest {

    @Mock
    private FamilyRepository familyRepository;

    @Mock
    private FamilySubscriptionRepository familySubscriptionRepository;

    @InjectMocks
    private UpdateFamilyPriorityServiceImpl service;

    @Test
    @DisplayName("성공: OWNER가 가족의 우선순위 타입을 FIFO로 변경한다")
    void updateFamilyPriorityToFifoSuccess() {
        // given
        Long familyId = 1L;
        UpdateFamilyPriorityRequest request = new UpdateFamilyPriorityRequest(familyId, PriorityType.FIFO, null);

        Family family = Family.builder().id(familyId).priorityType(PriorityType.PRIORITY).build();
        FamilySubscription sub1 = createFamilySubscription(100L, 1);

        given(familyRepository.findById(familyId)).willReturn(Optional.of(family));
        given(familySubscriptionRepository.findByFamilyId(familyId)).willReturn(List.of(sub1));

        // when
        UpdateFamilyPriorityResponse response = service.updateFamilyPriority(request, familyId, FamilyRole.OWNER);

        // then
        assertThat(family.getPriorityType()).isEqualTo(PriorityType.FIFO);
        assertThat(sub1.getPriority()).isEqualTo(-1);
        verify(familyRepository, times(1)).save(family);
        verify(familySubscriptionRepository, times(1)).updatePriorities(anyList());
    }

    @Test
    @DisplayName("성공: OWNER가 가족의 우선순위 타입을 PRIORITY로 변경하고 개별 순위를 설정한다")
    void updateFamilyPriorityToPrioritySuccess() {
        // given
        Long familyId = 1L;
        List<MemberPriorityRequest> memberRequests = List.of(
                new MemberPriorityRequest(100L, 2),
                new MemberPriorityRequest(101L, 1)
        );
        UpdateFamilyPriorityRequest request = new UpdateFamilyPriorityRequest(familyId, PriorityType.PRIORITY, memberRequests);

        Family family = Family.builder().id(familyId).priorityType(PriorityType.FIFO).build();
        FamilySubscription sub1 = createFamilySubscription(100L, -1);
        FamilySubscription sub2 = createFamilySubscription(101L, -1);

        given(familyRepository.findById(familyId)).willReturn(Optional.of(family));
        given(familySubscriptionRepository.findByFamilyId(familyId)).willReturn(List.of(sub1, sub2));

        // when
        UpdateFamilyPriorityResponse response = service.updateFamilyPriority(request, familyId, FamilyRole.OWNER);

        // then
        assertThat(family.getPriorityType()).isEqualTo(PriorityType.PRIORITY);
        assertThat(sub1.getPriority()).isEqualTo(2);
        assertThat(sub2.getPriority()).isEqualTo(1);
        verify(familySubscriptionRepository, times(1)).updatePriorities(anyList());
    }

    @Test
    @DisplayName("실패: OWNER가 아니면 권한 예외가 발생한다")
    void updateFamilyPriorityFailNotOwner() {
        // given
        UpdateFamilyPriorityRequest request = new UpdateFamilyPriorityRequest(1L, PriorityType.FIFO, null);

        // when & then
        assertThatThrownBy(() -> service.updateFamilyPriority(request, 1L, FamilyRole.CHILD))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(AuthErrorCode.ACCESS_DENIED.getMessage());
    }

    @Test
    @DisplayName("실패: 본인의 가족이 아닌 ID로 요청하면 예외가 발생한다")
    void updateFamilyPriorityFailDifferentFamily() {
        // given
        UpdateFamilyPriorityRequest request = new UpdateFamilyPriorityRequest(2L, PriorityType.FIFO, null);

        // when & then
        assertThatThrownBy(() -> service.updateFamilyPriority(request, 1L, FamilyRole.OWNER))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(FamilyErrorCode.NOT_FAMILY_MEMBER.getMessage());
    }

    private FamilySubscription createFamilySubscription(Long subId, int priority) {
        return FamilySubscription.builder()
                .subscription(Subscription.builder().id(subId).build())
                .priority(priority)
                .build();
    }
}
