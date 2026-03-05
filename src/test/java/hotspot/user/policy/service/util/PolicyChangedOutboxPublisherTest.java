package hotspot.user.policy.service.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.DayOfWeek;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import hotspot.user.outbox.consistencyOutbox.domain.event.family.policyChanged.FamilyPolicyChangedEvent;
import hotspot.user.policy.domain.BlockPolicy;
import hotspot.user.policy.domain.PolicySnapshot;
import hotspot.user.policy.domain.PolicyType;
import hotspot.user.policy.service.port.PolicySubRepository;

class PolicyChangedOutboxPublisherTest {

    private final PolicySubRepository policySubRepository = mock(PolicySubRepository.class);
    private final ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);

    private final PolicyChangedOutboxPublisher publisherService =
            new PolicyChangedOutboxPublisher(policySubRepository, publisher);

    @Test
    @DisplayName("성공: 정책 변경 이벤트 발행")
    void publishSuccess() {

        Long policyId = 1L;
        Long familyId = 10L;

        PolicySnapshot snapshot =
                PolicySnapshot.builder()
                        .days(List.of(DayOfWeek.MONDAY))
                        .startTime("09:00")
                        .endTime("14:00")
                        .build();

        BlockPolicy policy =
                BlockPolicy.builder()
                        .id(policyId)
                        .policyType(PolicyType.SCHEDULED)
                        .policySnapshot(snapshot)
                        .build();

        when(policySubRepository.findActiveSubIdsByBlockPolicyId(anyLong()))
                .thenReturn(List.of(1L, 2L));

        publisherService.publish(policy, familyId);

        ArgumentCaptor<FamilyPolicyChangedEvent> captor =
                ArgumentCaptor.forClass(FamilyPolicyChangedEvent.class);

        verify(publisher).publishEvent(captor.capture());

        FamilyPolicyChangedEvent event = captor.getValue();

        assertThat(event.familyId()).isEqualTo(familyId);
        assertThat(event.subIds()).hasSize(2);
        assertThat(event.subIds()).containsExactly(1L, 2L);
    }

    @Test
    @DisplayName("subIds가 없으면 이벤트 발행하지 않는다")
    void publishSkipWhenNoSubs() {

        PolicySnapshot snapshot =
                PolicySnapshot.builder()
                        .days(List.of(DayOfWeek.MONDAY))
                        .startTime("09:00")
                        .endTime("14:00")
                        .build();

        BlockPolicy policy =
                BlockPolicy.builder()
                        .id(1L)
                        .policyType(PolicyType.SCHEDULED)
                        .policySnapshot(snapshot)
                        .build();

        when(policySubRepository.findActiveSubIdsByBlockPolicyId(anyLong()))
                .thenReturn(List.of());

        publisherService.publish(policy, 10L);

        verify(publisher, never()).publishEvent(any());
    }
}
