package hotspot.user.policy.service.util;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;

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
    private static final Long POLICY_ID = 1L;

    @Test
    @DisplayName("성공: subIds가 존재하면 POLICY_DELETED 이벤트를 발행한다")
    void publishSuccess() {

        List<Long> subIds = List.of(10L, 11L);

        given(policySubRepository.findActiveSubIdsByBlockPolicyId(POLICY_ID))
                .willReturn(subIds);

        policyDeletedOutboxPublisher.publish(POLICY_ID, FAMILY_ID);

        verify(policySubRepository).findActiveSubIdsByBlockPolicyId(POLICY_ID);
        verify(publisher).publishEvent(any(FamilyPolicyDeletedEvent.class));
    }

    @Test
    @DisplayName("성공: subIds가 없으면 이벤트를 발행하지 않는다")
    void publishNoSubIds() {

        given(policySubRepository.findActiveSubIdsByBlockPolicyId(POLICY_ID))
                .willReturn(List.of());

        policyDeletedOutboxPublisher.publish(POLICY_ID, FAMILY_ID);

        verify(policySubRepository).findActiveSubIdsByBlockPolicyId(POLICY_ID);
        verify(publisher, never()).publishEvent(any());
    }
}
