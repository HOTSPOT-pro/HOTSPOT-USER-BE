package hotspot.user.policy.service.util;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import hotspot.user.outbox.consistencyOutbox.domain.event.family.policyDeleted.FamilyPolicyDeletedEvent;
import hotspot.user.policy.service.port.PolicySubRepository;

@ExtendWith(MockitoExtension.class)
class PolicyDeletedOutboxPublisherTest {

    @Mock
    private PolicySubRepository policySubRepository;

    @Mock
    private ApplicationEventPublisher publisher;

    @InjectMocks
    private PolicyDeletedOutboxPublisher policyDeletedOutboxPublisher;

    private static final Long FAMILY_ID = 100L;

    @Test
    @DisplayName("성공: policy-sub 매핑이 존재하면 POLICY_DELETED 이벤트를 발행한다")
    void publishAllSuccess() {

        List<Long> policyIds = List.of(1L, 2L);

        Map<Long, List<Long>> policySubMap = Map.of(
                1L, List.of(10L, 11L),
                2L, List.of(12L)
        );

        given(policySubRepository.findActiveSubIdsByBlockPolicyIds(policyIds))
                .willReturn(policySubMap);

        // when
        policyDeletedOutboxPublisher.publishAll(policyIds, FAMILY_ID);

        // then
        verify(policySubRepository).findActiveSubIdsByBlockPolicyIds(policyIds);
        verify(publisher).publishEvent(any(FamilyPolicyDeletedEvent.class));
    }

    @Test
    @DisplayName("성공: 매핑된 subIds가 없으면 이벤트를 발행하지 않는다")
    void publishAllNoSubIds() {

        List<Long> policyIds = List.of(1L, 2L);

        given(policySubRepository.findActiveSubIdsByBlockPolicyIds(policyIds))
                .willReturn(Map.of());

        // when
        policyDeletedOutboxPublisher.publishAll(policyIds, FAMILY_ID);

        // then
        verify(policySubRepository).findActiveSubIdsByBlockPolicyIds(policyIds);
        verify(publisher, never()).publishEvent(any());
    }
}
