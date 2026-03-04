package hotspot.user.dispatch.sms.service;

import org.springframework.stereotype.Component;

import hotspot.user.common.crpyto.PhoneDecryptor;
import hotspot.user.dispatch.sms.domain.SmsRecipientResolution;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.subscription.service.port.SubscriptionRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SmsRecipientResolver {

    private final SubscriptionRepository subscriptionRepository;
    private final PhoneDecryptor phoneDecryptor;

    // 구독자 ID로 SMS 수신 전화번호를 조회하고 복호화 결과를 상태로 반환한다.
    public SmsRecipientResolution resolve(Long subId) {
        Subscription subscription = subscriptionRepository.findById(subId).orElse(null);
        if (subscription == null) {
            return SmsRecipientResolution.subscriptionNotFound();
        }

        String phoneEnc = subscription.getPhoneEnc();
        if (phoneEnc == null || phoneEnc.isBlank()) {
            return SmsRecipientResolution.phoneMissing();
        }

        try {
            return SmsRecipientResolution.found(phoneDecryptor.decrypt(phoneEnc));
        } catch (RuntimeException ex) {
            return SmsRecipientResolution.decryptFailed();
        }
    }
}
