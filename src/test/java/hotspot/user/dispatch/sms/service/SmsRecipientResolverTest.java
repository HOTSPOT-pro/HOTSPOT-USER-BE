package hotspot.user.dispatch.sms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.common.crpyto.PhoneDecryptor;
import hotspot.user.dispatch.sms.domain.SmsRecipientResolution;
import hotspot.user.dispatch.sms.domain.SmsRecipientResolutionStatus;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.subscription.service.port.SubscriptionRepository;

@ExtendWith(MockitoExtension.class)
class SmsRecipientResolverTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private PhoneDecryptor phoneDecryptor;

    @InjectMocks
    private SmsRecipientResolver smsRecipientResolver;

    @Test
    @DisplayName("returns subscription not found when sub does not exist")
    void resolveSubscriptionNotFound() {
        given(subscriptionRepository.findById(1L)).willReturn(java.util.Optional.empty());

        SmsRecipientResolution resolution = smsRecipientResolver.resolve(1L);

        assertThat(resolution.status()).isEqualTo(SmsRecipientResolutionStatus.SUBSCRIPTION_NOT_FOUND);
    }

    @Test
    @DisplayName("returns phone missing when encrypted phone is blank")
    void resolvePhoneMissing() {
        given(subscriptionRepository.findById(1L))
                .willReturn(java.util.Optional.of(Subscription.builder().id(1L).phoneEnc(" ").build()));

        SmsRecipientResolution resolution = smsRecipientResolver.resolve(1L);

        assertThat(resolution.status()).isEqualTo(SmsRecipientResolutionStatus.PHONE_MISSING);
    }

    @Test
    @DisplayName("returns decrypt failed when decryptor throws")
    void resolveDecryptFailed() {
        given(subscriptionRepository.findById(1L))
                .willReturn(java.util.Optional.of(Subscription.builder().id(1L).phoneEnc("enc").build()));
        given(phoneDecryptor.decrypt("enc", 1L)).willThrow(new IllegalStateException("decrypt fail"));

        SmsRecipientResolution resolution = smsRecipientResolver.resolve(1L);

        assertThat(resolution.status()).isEqualTo(SmsRecipientResolutionStatus.DECRYPT_FAILED);
    }

    @Test
    @DisplayName("returns found when decrypt succeeds")
    void resolveFound() {
        given(subscriptionRepository.findById(1L))
                .willReturn(java.util.Optional.of(Subscription.builder().id(1L).phoneEnc("enc").build()));
        given(phoneDecryptor.decrypt("enc", 1L)).willReturn("010-1234-5678");

        SmsRecipientResolution resolution = smsRecipientResolver.resolve(1L);

        assertThat(resolution.status()).isEqualTo(SmsRecipientResolutionStatus.FOUND);
        assertThat(resolution.phoneNumber()).isEqualTo("010-1234-5678");
    }
}
