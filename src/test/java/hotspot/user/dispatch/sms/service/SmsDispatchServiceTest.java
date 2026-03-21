package hotspot.user.dispatch.sms.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.SmsErrorCode;
import hotspot.user.dispatch.sms.domain.SmsRecipientResolution;
import hotspot.user.dispatch.sms.dto.SmsDispatchCommand;
import hotspot.user.dispatch.sms.infrastructure.SmsSenderPort;
import hotspot.user.kafka.domain.NotificationType;
import hotspot.user.notification.domain.NotificationAllow;
import hotspot.user.notification.domain.NotificationCategory;
import hotspot.user.notification.service.port.NotificationAllowRepository;

@ExtendWith(MockitoExtension.class)
class SmsDispatchServiceTest {

    @Mock
    private SmsSenderPort smsSenderPort;

    @Mock
    private SmsRecipientResolver smsRecipientResolver;

    @Mock
    private SmsMessageBuilder smsMessageBuilder;

    @Mock
    private NotificationAllowRepository notificationAllowRepository;

    @InjectMocks
    private SmsDispatchService smsDispatchService;

    @Test
    @DisplayName("sends sms for target type with resolved recipient")
    void sendsSmsForTargetType() {
        SmsDispatchCommand command = command("SINGLE_USAGE_THRESHOLD_50");
        given(notificationAllowRepository.findBySubIdAndCategory(1L, NotificationCategory.DATA))
                .willReturn(Optional.of(NotificationAllow.builder()
                        .subId(1L)
                        .notificationCategory(NotificationCategory.DATA)
                        .notificationAllow(true)
                        .build()));
        given(smsRecipientResolver.resolve(1L)).willReturn(SmsRecipientResolution.found("010-1234-5678"));
        given(smsMessageBuilder.build(command, NotificationType.SINGLE_USAGE_THRESHOLD_50))
                .willReturn("[HOTSPOT] sms-content");

        smsDispatchService.dispatch(command);

        then(smsSenderPort).should().send("010-1234-5678", "[HOTSPOT] sms-content");
    }

    @Test
    @DisplayName("skips sms for non-target type")
    void skipsForNonTargetType() {
        SmsDispatchCommand command = command("IMMEDIATE_BLOCK_APPLIED");

        assertThatThrownBy(() -> smsDispatchService.dispatch(command))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(SmsErrorCode.SMS_NOTIFICATION_TYPE_NOT_TARGET.getMessage());

        then(smsSenderPort).should(never()).send(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    @DisplayName("skips sms when recipient resolution fails")
    void skipsWhenResolverFails() {
        SmsDispatchCommand command = command("FAMILY_USAGE_THRESHOLD_10");
        given(notificationAllowRepository.findBySubIdAndCategory(1L, NotificationCategory.DATA))
                .willReturn(Optional.of(NotificationAllow.builder()
                        .subId(1L)
                        .notificationCategory(NotificationCategory.DATA)
                        .notificationAllow(true)
                        .build()));
        given(smsRecipientResolver.resolve(1L)).willReturn(SmsRecipientResolution.decryptFailed());

        assertThatThrownBy(() -> smsDispatchService.dispatch(command))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(SmsErrorCode.SMS_RECIPIENT_DECRYPT_FAILED.getMessage());

        then(smsSenderPort).should(never()).send(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    @DisplayName("skips data threshold sms when notification allow is disabled")
    void skipsDataThresholdWhenAllowDisabled() {
        SmsDispatchCommand command = command("SINGLE_USAGE_THRESHOLD_30");
        given(notificationAllowRepository.findBySubIdAndCategory(1L, NotificationCategory.DATA))
                .willReturn(Optional.of(NotificationAllow.builder()
                        .subId(1L)
                        .notificationCategory(NotificationCategory.DATA)
                        .notificationAllow(false)
                        .build()));

        assertThatThrownBy(() -> smsDispatchService.dispatch(command))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(SmsErrorCode.SMS_NOTIFICATION_ALLOW_DISABLED.getMessage());

        then(smsRecipientResolver).should(never()).resolve(org.mockito.ArgumentMatchers.anyLong());
        then(smsSenderPort).should(never()).send(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    @DisplayName("sends family create sms regardless of data notification allow")
    void sendsFamilyCreateWithoutDataAllowValidation() {
        SmsDispatchCommand command = command("FAMILY_CREATE_APPROVED");
        given(smsRecipientResolver.resolve(1L)).willReturn(SmsRecipientResolution.found("010-1234-5678"));
        given(smsMessageBuilder.build(command, NotificationType.FAMILY_CREATE_APPROVED))
                .willReturn("[HOTSPOT] sms-content");

        smsDispatchService.dispatch(command);

        then(notificationAllowRepository).should(never())
                .findBySubIdAndCategory(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.any());
        then(smsSenderPort).should().send("010-1234-5678", "[HOTSPOT] sms-content");
    }

    @Test
    @DisplayName("sends present usage sms when data notification allow is enabled")
    void sendsPresentUsageWhenDataAllowEnabled() {
        SmsDispatchCommand command = command("PRESENT_USAGE_THRESHOLD_10");
        given(notificationAllowRepository.findBySubIdAndCategory(1L, NotificationCategory.DATA))
                .willReturn(Optional.of(NotificationAllow.builder()
                        .subId(1L)
                        .notificationCategory(NotificationCategory.DATA)
                        .notificationAllow(true)
                        .build()));
        given(smsRecipientResolver.resolve(1L)).willReturn(SmsRecipientResolution.found("010-1234-5678"));
        given(smsMessageBuilder.build(command, NotificationType.PRESENT_USAGE_THRESHOLD_10))
                .willReturn("[HOTSPOT] sms-content");

        smsDispatchService.dispatch(command);

        then(smsSenderPort).should().send("010-1234-5678", "[HOTSPOT] sms-content");
    }

    @Test
    @DisplayName("skips present usage sms when data notification allow is disabled")
    void skipsPresentUsageWhenDataAllowDisabled() {
        SmsDispatchCommand command = command("PRESENT_USAGE_EXHAUSTED");
        given(notificationAllowRepository.findBySubIdAndCategory(1L, NotificationCategory.DATA))
                .willReturn(Optional.of(NotificationAllow.builder()
                        .subId(1L)
                        .notificationCategory(NotificationCategory.DATA)
                        .notificationAllow(false)
                        .build()));

        assertThatThrownBy(() -> smsDispatchService.dispatch(command))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(SmsErrorCode.SMS_NOTIFICATION_ALLOW_DISABLED.getMessage());

        then(smsRecipientResolver).should(never()).resolve(org.mockito.ArgumentMatchers.anyLong());
        then(smsSenderPort).should(never()).send(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    @DisplayName("throws when notification type is unsupported")
    void throwsWhenUnsupportedNotificationType() {
        SmsDispatchCommand command = command("UNKNOWN_TYPE");

        assertThatThrownBy(() -> smsDispatchService.dispatch(command))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(SmsErrorCode.UNSUPPORTED_SMS_NOTIFICATION_TYPE.getMessage());
    }

    private SmsDispatchCommand command(String type) {
        return new SmsDispatchCommand(
                1L,
                1L,
                "evt-1",
                type,
                "이번 달 데이터 사용량 안내",
                "content-1",
                LocalDateTime.of(2026, 2, 23, 10, 15, 30),
                "유쓰 5G 데이터 플러스",
                "110GB",
                "80%",
                "88.02GB",
                null
        );
    }
}
