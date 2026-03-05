package hotspot.user.policy.service.util;

import java.util.List;
import java.util.Map;
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

    public void publishAll(List<Long> policyIds, Long familyId) {

        Map<Long, List<Long>> policySubMap =
                policySubRepository.findActiveSubIdsByBlockPolicyIds(policyIds);

        if (policySubMap.isEmpty()) {
            return;
        }

        List<FamilyPolicyDeletedEvent.PolicyTarget> policies =
                policySubMap.entrySet().stream()
                        .map(e -> new FamilyPolicyDeletedEvent.PolicyTarget(
                                e.getKey(),
                                e.getValue()
                        ))
                        .toList();

        FamilyPolicyDeletedEvent event =
                new FamilyPolicyDeletedEvent(
                        UUID.randomUUID().toString(),
                        familyId,
                        "POLICY_DELETED",
                        policies
                );

        publisher.publishEvent(event);
    }
}
