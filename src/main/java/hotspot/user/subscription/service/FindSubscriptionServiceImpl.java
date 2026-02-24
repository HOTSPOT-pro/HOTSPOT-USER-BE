package hotspot.user.subscription.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.common.crpyto.PhoneDecryptor;
import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.SubscriptionErrorCode;
import hotspot.user.subscription.controller.port.FindSubscriptionService;
import hotspot.user.subscription.controller.response.SubscriptionResponse;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.subscription.domain.mapper.SubscriptionMapper;
import hotspot.user.subscription.service.port.SubscriptionRepository;
import lombok.RequiredArgsConstructor;

/**
 * 회선 조회 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindSubscriptionServiceImpl implements FindSubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final PhoneDecryptor phoneDecryptor;

    @Override
    public SubscriptionResponse findById(Long id) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(SubscriptionErrorCode.SUBSCRIPTION_NOT_FOUND));

        String decryptedPhone = phoneDecryptor.decrypt(subscription.getPhoneEnc());
        return SubscriptionMapper.toSubscriptionResponse(subscription, decryptedPhone);
    }

    @Override
    public SubscriptionResponse findByMemberId(Long memberId) {
        Subscription subscription = subscriptionRepository.findByMemberId(memberId)
                .orElseThrow(() -> new ApplicationException(SubscriptionErrorCode.SUBSCRIPTION_NOT_FOUND));

        String decryptedPhone = phoneDecryptor.decrypt(subscription.getPhoneEnc());
        return SubscriptionMapper.toSubscriptionResponse(subscription, decryptedPhone);
    }
}
