package hotspot.user.dispatch.sms.service;

import org.springframework.stereotype.Service;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.SmsErrorCode;
import hotspot.user.dispatch.sms.domain.SmsRecipientResolution;
import hotspot.user.dispatch.sms.port.SmsSenderPort;
import hotspot.user.kafka.domain.NotificationType;
import hotspot.user.notification.domain.Notification;
import hotspot.user.notification.domain.NotificationCategory;
import hotspot.user.notification.service.port.NotificationAllowRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SmsDispatchService {

    private final SmsSenderPort smsSenderPort;
    private final SmsRecipientResolver smsRecipientResolver;
    private final SmsMessageBuilder smsMessageBuilder;
    private final NotificationAllowRepository notificationAllowRepository;

    // 알림 정보를 검증하고 수신자/메시지를 구성해 SMS를 발송한다.
    public void dispatch(Notification notification) {
        try {
            NotificationType notificationType = parseNotificationType(notification);
            validateSmsTargetType(notificationType);
            validateDataAllow(notification, notificationType);

            SmsRecipientResolution resolution = smsRecipientResolver.resolve(notification.getSubId());
            String phoneNumber = resolvePhoneNumber(resolution);

            smsSenderPort.send(phoneNumber, smsMessageBuilder.build(notification));
        } catch (ApplicationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApplicationException(SmsErrorCode.SMS_DISPATCH_FAILED, ex);
        }
    }

    // DATA 카테고리 알림인지 확인해 수신 허용 검증 필요 여부를 반환한다.
    private boolean requiresDataAllowValidation(NotificationType notificationType) {
        return notificationType.category() == NotificationCategory.DATA;
    }

    // 구독자의 카테고리별 알림 허용 여부를 조회한다.
    private boolean isAllowed(Long subId, NotificationCategory category) {
        return notificationAllowRepository.findBySubIdAndCategory(subId, category)
                .map(notificationAllow -> Boolean.TRUE.equals(notificationAllow.getNotificationAllow()))
                .orElse(false);
    }

    // 알림 타입 문자열을 NotificationType으로 파싱한다.
    private NotificationType parseNotificationType(Notification notification) {
        try {
            return NotificationType.from(notification.getNotificationType());
        } catch (RuntimeException ex) {
            throw new ApplicationException(SmsErrorCode.UNSUPPORTED_SMS_NOTIFICATION_TYPE, ex);
        }
    }

    // SMS 발송 대상 타입인지 검증한다.
    private void validateSmsTargetType(NotificationType notificationType) {
        if (!NotificationType.isSmsAllowed(notificationType)) {
            throw new ApplicationException(SmsErrorCode.SMS_NOTIFICATION_TYPE_NOT_TARGET);
        }
    }

    // DATA 카테고리 알림의 수신 허용 상태를 검증한다.
    private void validateDataAllow(Notification notification, NotificationType notificationType) {
        if (requiresDataAllowValidation(notificationType)
                && !isAllowed(notification.getSubId(), NotificationCategory.DATA)) {
            throw new ApplicationException(SmsErrorCode.SMS_NOTIFICATION_ALLOW_DISABLED);
        }
    }

    // 수신자 해석 결과를 실제 전화번호 또는 도메인 예외로 변환한다.
    private String resolvePhoneNumber(SmsRecipientResolution resolution) {
        return switch (resolution.status()) {
            case FOUND -> resolution.phoneNumber();
            case SUBSCRIPTION_NOT_FOUND ->
                    throw new ApplicationException(SmsErrorCode.SMS_RECIPIENT_SUBSCRIPTION_NOT_FOUND);
            case PHONE_MISSING ->
                    throw new ApplicationException(SmsErrorCode.SMS_RECIPIENT_PHONE_MISSING);
            case DECRYPT_FAILED ->
                    throw new ApplicationException(SmsErrorCode.SMS_RECIPIENT_DECRYPT_FAILED);
        };
    }
}
