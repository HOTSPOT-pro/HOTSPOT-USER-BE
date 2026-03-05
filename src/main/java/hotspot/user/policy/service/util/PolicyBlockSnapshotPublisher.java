package hotspot.user.policy.service.util;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import hotspot.user.outbox.consistencyOutbox.domain.PolicyPayload;
import hotspot.user.outbox.consistencyOutbox.domain.event.subscription.policyBlock.PolicyBlockSnapshotEvent;
import hotspot.user.policy.domain.BlockPolicy;
import hotspot.user.policy.domain.PolicySub;
import hotspot.user.policy.service.port.BlockPolicyRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PolicyBlockSnapshotPublisher {

    private final BlockPolicyRepository blockPolicyRepository;
    private final ApplicationEventPublisher eventPublisher;

    public void publish(Long subId, List<PolicySub> activeSubs) {

        if (activeSubs.isEmpty()) {

            eventPublisher.publishEvent(
                    new PolicyBlockSnapshotEvent(
                            "POLICY_SNAPSHOT",
                            subId,
                            List.of(),
                            UUID.randomUUID().toString()
                    )
            );

            return;
        }

        List<Long> policyIds =
                activeSubs.stream()
                        .map(PolicySub::getBlockPolicyId)
                        .toList();

        Map<Long, BlockPolicy> policyMap =
                blockPolicyRepository.findAllById(policyIds)
                        .stream()
                        .collect(Collectors.toMap(
                                BlockPolicy::getId,
                                policy -> policy
                        ));

        List<PolicyPayload> payloads =
                activeSubs.stream()
                        .map(sub -> {

                            BlockPolicy policy =
                                    policyMap.get(sub.getBlockPolicyId());

                            return PolicySnapshotUtil.map(
                                    policy.getId(),
                                    policy.getPolicyType(),
                                    policy.getPolicySnapshot()
                            );
                        })
                        .toList();

        eventPublisher.publishEvent(
                new PolicyBlockSnapshotEvent(
                        "POLICY_SNAPSHOT",
                        subId,
                        payloads,
                        UUID.randomUUID().toString()
                )
        );
    }
}
