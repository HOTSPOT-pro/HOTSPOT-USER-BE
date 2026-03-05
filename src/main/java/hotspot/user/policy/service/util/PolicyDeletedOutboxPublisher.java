package hotspot.user.policy.service.util;

import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import hotspot.user.outbox.consistencyOutbox.domain.event.family.policyDeleted.FamilyPolicyDeletedEvent;
import hotspot.user.policy.service.port.PolicySubRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PolicyDeletedOutboxPublisher {

    private final PolicySubRepository policySubRepository;
    private final ApplicationEventPublisher publisher;

    public void publish(Long policyId, Long familyId) {

        List<Long> subIds =
                policySubRepository.findActiveSubIdsByBlockPolicyId(policyId);

        if (subIds.isEmpty()) {
            return;
        }

        FamilyPolicyDeletedEvent event =
                new FamilyPolicyDeletedEvent(
                        UUID.randomUUID().toString(),
                        familyId,
                        "POLICY_DELETED",
                        subIds,
                        policyId
                );

        publisher.publishEvent(event);
    }
}
