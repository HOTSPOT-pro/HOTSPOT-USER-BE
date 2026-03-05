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

import hotspot.user.outbox.consistencyOutbox.domain.event.family.deactivate.FamilyPolicyDeactivateEvent;
import hotspot.user.policy.service.port.PolicySubRepository;

@ExtendWith(MockitoExtension.class)
class FamilyPolicyDeactivatePublisherTest {

    @Mock
    private PolicySubRepository policySubRepository;

    @Mock
    private ApplicationEventPublisher publisher;

    @InjectMocks
    private FamilyPolicyDeactivatePublisher familyPolicyDeactivatePublisher;

    private static final Long FAMILY_ID = 100L;

    @Test
    @DisplayName("성공: policySub가 존재하면 POLICY_DEACTIVATE 이벤트를 발행한다")
    void publishSuccess() {

        List<Long> policyIds = List.of(1L, 2L);

        Map<Long, List<Long>> policySubMap = Map.of(
                1L, List.of(10L, 11L),
                2L, List.of(20L)
        );

        given(policySubRepository.findActiveSubIdsByBlockPolicyIds(policyIds))
                .willReturn(policySubMap);

        familyPolicyDeactivatePublisher.publish(policyIds, FAMILY_ID);

        verify(policySubRepository).findActiveSubIdsByBlockPolicyIds(policyIds);
        verify(publisher).publishEvent(any(FamilyPolicyDeactivateEvent.class));
    }

    @Test
    @DisplayName("성공: 활성 sub가 없으면 이벤트를 발행하지 않는다")
    void publishNoSubIds() {

        List<Long> policyIds = List.of(1L);

        given(policySubRepository.findActiveSubIdsByBlockPolicyIds(policyIds))
                .willReturn(Map.of());

        familyPolicyDeactivatePublisher.publish(policyIds, FAMILY_ID);

        verify(policySubRepository).findActiveSubIdsByBlockPolicyIds(policyIds);
        verify(publisher, never()).publishEvent(any());
    }
}
