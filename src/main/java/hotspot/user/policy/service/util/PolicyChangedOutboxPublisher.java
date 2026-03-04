package hotspot.user.policy.service.util;

import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import hotspot.user.outbox.consistencyOutbox.domain.PolicyPayload;
import hotspot.user.outbox.consistencyOutbox.domain.event.family.policyChanged.FamilyPolicyChangedEvent;
import hotspot.user.policy.domain.BlockPolicy;
import hotspot.user.policy.service.port.PolicySubRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PolicyChangedOutboxPublisher {

    private final PolicySubRepository policySubRepository;
    private final ApplicationEventPublisher publisher;

    public void publish(BlockPolicy policy, Long familyId) {

        List<Long> subIds =
                policySubRepository.findActiveSubIdsByBlockPolicyId(policy.getId());

        if (subIds.isEmpty()) {
            return;
        }

        PolicyPayload payload =
                PolicySnapshotUtil.map(
                        policy.getId(),
                        policy.getPolicyType(),
                        policy.getPolicySnapshot()
                );

        FamilyPolicyChangedEvent event =
                new FamilyPolicyChangedEvent(
                        UUID.randomUUID().toString(),
                        familyId,
                        "POLICY_CHANGED",
                        subIds,
                        payload
                );

        publisher.publishEvent(event);
    }
}
