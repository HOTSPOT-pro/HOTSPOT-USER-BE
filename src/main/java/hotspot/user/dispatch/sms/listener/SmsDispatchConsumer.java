package hotspot.user.dispatch.sms.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.BaseErrorCode;
import hotspot.user.common.exception.code.SmsErrorCode;
import hotspot.user.dispatch.sms.dto.SmsDispatchCommand;
import hotspot.user.dispatch.sms.service.SmsDispatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SmsDispatchConsumer {

    private final SmsDispatchService smsDispatchService;

    @KafkaListener(
            topics = "${app.topics.sms-dispatch}",
            containerFactory = "smsDispatchKafkaListenerContainerFactory"
    )
    // SMS 디스패치 커맨드를 소비해 실제 발송 서비스를 호출한다.
    public void consume(SmsDispatchCommand command, Acknowledgment acknowledgment) {
        try {
            smsDispatchService.dispatch(command.toNotification());
            acknowledgment.acknowledge();
        } catch (ApplicationException ex) {
            SmsErrorCode errorCode = resolveErrorCode(ex);
            if (isRetryable(errorCode)) {
                throw ex;
            }

            log.warn(
                    "Skip SMS dispatch consume. notificationId={}, subId={}, errorCode={}, reason={}",
                    command.notificationId(),
                    command.subId(),
                    errorCode.getCustomCode(),
                    ex.getMessage()
            );
            acknowledgment.acknowledge();
        }
    }

    // 예외 코드에서 SMS 에러 코드를 추출하고 기본값을 보정한다.
    private SmsErrorCode resolveErrorCode(ApplicationException ex) {
        BaseErrorCode errorCode = ex.getCode();
        if (errorCode instanceof SmsErrorCode smsErrorCode) {
            return smsErrorCode;
        }
        return SmsErrorCode.SMS_DISPATCH_FAILED;
    }

    // 재시도 대상 에러인지 판별한다.
    private boolean isRetryable(SmsErrorCode errorCode) {
        return errorCode == SmsErrorCode.SMS_DISPATCH_FAILED;
    }
}
