package hotspot.user.policy.service.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.DayOfWeek;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import hotspot.user.outbox.consistencyOutbox.domain.event.subscription.policyBlock.PolicyBlockSnapshotEvent;
import hotspot.user.policy.domain.BlockPolicy;
import hotspot.user.policy.domain.PolicySnapshot;
import hotspot.user.policy.domain.PolicySub;
import hotspot.user.policy.domain.PolicyType;
import hotspot.user.policy.service.port.BlockPolicyRepository;

class PolicyBlockSnapshotPublisherTest {

    private final BlockPolicyRepository blockPolicyRepository = mock(BlockPolicyRepository.class);
    private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

    private final PolicyBlockSnapshotPublisher publisher =
            new PolicyBlockSnapshotPublisher(blockPolicyRepository, eventPublisher);

    @Test
    @DisplayName("activeSubs가 없으면 빈 snapshot 이벤트 발행")
    void publishEmptySnapshot() {

        Long subId = 1L;

        publisher.publish(subId, List.of());

        ArgumentCaptor<PolicyBlockSnapshotEvent> captor =
                ArgumentCaptor.forClass(PolicyBlockSnapshotEvent.class);

        verify(eventPublisher, times(1)).publishEvent(captor.capture());

        PolicyBlockSnapshotEvent event = captor.getValue();

        assertThat(event.subId()).isEqualTo(subId);
        assertThat(event.policies()).isEmpty();
    }

    @Test
    @DisplayName("activeSubs가 있으면 정책 조회 후 snapshot 이벤트 발행")
    void publishSnapshotSuccess() {

        Long subId = 1L;
        Long policyId = 10L;

        PolicySub policySub =
                PolicySub.builder()
                        .blockPolicyId(policyId)
                        .build();

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

        when(blockPolicyRepository.findAllById(anyList()))
                .thenReturn(List.of(policy));

        publisher.publish(subId, List.of(policySub));

        ArgumentCaptor<PolicyBlockSnapshotEvent> captor =
                ArgumentCaptor.forClass(PolicyBlockSnapshotEvent.class);

        verify(eventPublisher, times(1)).publishEvent(captor.capture());

        PolicyBlockSnapshotEvent event = captor.getValue();

        assertThat(event.subId()).isEqualTo(subId);
        assertThat(event.policies()).hasSize(1);
    }
}
