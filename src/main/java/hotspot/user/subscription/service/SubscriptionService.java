package hotspot.user.subscription.service;

import org.springframework.stereotype.Service;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.SubscriptionErrorCode;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.subscription.service.port.SubscriptionRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    public Subscription findByMemberId(Long memberId) {
        return subscriptionRepository.findByMemberId(memberId)
                .orElseThrow(() -> new ApplicationException(SubscriptionErrorCode.SUBSCRIPTION_NOT_FOUND));
    }
}
