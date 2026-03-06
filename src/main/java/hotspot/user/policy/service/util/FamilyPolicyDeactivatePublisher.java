package hotspot.user.policy.service.util;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import hotspot.user.outbox.consistencyOutbox.domain.event.family.deactivate.FamilyPolicyDeactivateEvent;
import hotspot.user.policy.service.port.PolicySubRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FamilyPolicyDeactivatePublisher {

    private final PolicySubRepository policySubRepository;
    private final ApplicationEventPublisher publisher;

    public void publish(List<Long> policyIds, Long familyId) {

        Map<Long, List<Long>> policySubMap =
                policySubRepository.findActiveSubIdsByBlockPolicyIds(policyIds);

        List<FamilyPolicyDeactivateEvent.PolicyTarget> policies =
                policySubMap.entrySet().stream()
                        .map(e -> new FamilyPolicyDeactivateEvent.PolicyTarget(
                                e.getKey(),
                                e.getValue()
                        ))
                        .toList();

        if (policies.isEmpty()) {
            return;
        }

        FamilyPolicyDeactivateEvent event =
                new FamilyPolicyDeactivateEvent(
                        UUID.randomUUID().toString(),
                        familyId,
                        "POLICY_DEACTIVATE",
                        policies
                );

        publisher.publishEvent(event);
    }
}
