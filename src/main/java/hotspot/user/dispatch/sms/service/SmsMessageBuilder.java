package hotspot.user.dispatch.sms.service;

import java.util.Locale;

import org.springframework.stereotype.Component;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.SmsErrorCode;
import hotspot.user.common.util.redis.RedisUsageCalculator;
import hotspot.user.dispatch.sms.config.SmsProperties;
import hotspot.user.dispatch.sms.dto.SmsDispatchCommand;
import hotspot.user.kafka.domain.NotificationType;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SmsMessageBuilder {

    private static final String DEFAULT_LINK = "https://hotspot.pics";
    private static final String BRAND_LABEL = "HOTSPOT";

    private final SmsProperties smsProperties;

    // 발신 헤더와 타입별 본문을 조합해 최종 SMS 문자열을 만든다.
    public String build(SmsDispatchCommand command, NotificationType notificationType) {
        return "[" + BRAND_LABEL + "]\n" + resolveBody(command, notificationType);
    }

    // 알림 타입에 따라 사용량 안내 또는 가족 생성 안내 본문으로 분기한다.
    private String resolveBody(SmsDispatchCommand command, NotificationType notificationType) {
        return switch (notificationType) {
            case SINGLE_USAGE_THRESHOLD_50,
                    SINGLE_USAGE_THRESHOLD_30,
                    SINGLE_USAGE_THRESHOLD_10,
                    SINGLE_USAGE_EXHAUSTED,
                    FAMILY_USAGE_THRESHOLD_50,
                    FAMILY_USAGE_THRESHOLD_30,
                    FAMILY_USAGE_THRESHOLD_10,
                    FAMILY_USAGE_EXHAUSTED,
                    PRESENT_USAGE_THRESHOLD_50,
                    PRESENT_USAGE_THRESHOLD_30,
                    PRESENT_USAGE_THRESHOLD_10,
                    PRESENT_USAGE_EXHAUSTED -> buildUsageGuideBody(command, notificationType);
            case FAMILY_CREATE_APPROVED -> buildFamilyCreateBody(
                    "가족 생성 승인 안내",
                    "고객님, 신규 가족 생성 요청이 승인되었습니다.",
                    "HOTSPOT을 바로 이용해 보세요."
            );
            case FAMILY_CREATE_REJECTED -> buildFamilyCreateBody(
                    "가족 생성 반려 안내",
                    "고객님, 신규 가족 생성 요청이 반려되었습니다.",
                    "신청 정보를 확인한 뒤 다시 요청해 주세요."
            );
            default -> throw new ApplicationException(SmsErrorCode.SMS_TEMPLATE_NOT_FOUND);
        };
    }

    // 사용량 안내 공통 포맷(제목/안내문/제공량/사용량/링크) 본문을 생성한다.
    private String buildUsageGuideBody(SmsDispatchCommand command, NotificationType notificationType) {
        String title = defaultIfBlank(command.title(), "이번 달 데이터 사용 안내");

        String providedAmountRaw = defaultIfBlank(command.providedAmount(), "0").trim().replace(",", "");
        double providedGb = RedisUsageCalculator.kbToGb(Double.parseDouble(providedAmountRaw));
        String providedAmount = (Math.floor(providedGb) == providedGb)
                ? String.valueOf((long) providedGb)
                : String.valueOf(providedGb);

        String usedPercent = defaultIfBlank(command.usedPercent(), "0").trim().replace("%", "");

        String usedAmountRaw = defaultIfBlank(command.usedAmount(), "0").trim().replace(",", "");
        double usedGb = RedisUsageCalculator.kbToGb(Double.parseDouble(usedAmountRaw));
        String usedAmount = (Math.floor(usedGb) == usedGb)
                ? String.valueOf((long) usedGb)
                : String.valueOf(usedGb);
        String intro = resolveUsageIntro(command, notificationType);

        return String.format(Locale.KOREA, """
                        %s

                        %s


                        ▶ 데이터 사용량 안내
                         - 제공량: %sGB
                         - 사용량: %s%% %sGB


                        ▶ 요금제 사용량 확인하기
                        ☞ %s
                        """,
                title,
                intro,
                providedAmount,
                usedPercent,
                usedAmount,
                DEFAULT_LINK
        ).trim();
    }

    // 타입별로 사용량 안내 소개 문장을 만든다(개인/가족/선물).
    private String resolveUsageIntro(SmsDispatchCommand command, NotificationType notificationType) {
        return switch (notificationType) {
            case SINGLE_USAGE_THRESHOLD_50,
                    SINGLE_USAGE_THRESHOLD_30,
                    SINGLE_USAGE_THRESHOLD_10,
                    SINGLE_USAGE_EXHAUSTED -> {
                String planName = defaultIfBlank(command.planName(), "요금제");
                yield "고객님, 「" + planName + "」 요금제의 기본 데이터 사용량을 안내해 드립니다.";
            }
            case FAMILY_USAGE_THRESHOLD_50,
                    FAMILY_USAGE_THRESHOLD_30,
                    FAMILY_USAGE_THRESHOLD_10,
                    FAMILY_USAGE_EXHAUSTED -> "고객님, 가족 공유 데이터 사용량을 안내해 드립니다.";
            case PRESENT_USAGE_THRESHOLD_50,
                    PRESENT_USAGE_THRESHOLD_30,
                    PRESENT_USAGE_THRESHOLD_10,
                    PRESENT_USAGE_EXHAUSTED -> {
                String senderName = defaultIfBlank(command.presentSenderName(), "지인");
                yield "고객님, " + senderName + "님께 선물 받은 데이터 사용량을 안내해 드립니다.";
            }
            default -> throw new ApplicationException(SmsErrorCode.SMS_TEMPLATE_NOT_FOUND);
        };
    }

    // 가족 생성 승인/반려에 사용하는 전용 본문 포맷을 생성한다.
    private String buildFamilyCreateBody(
            String familyTitle,
            String headline,
            String guide
    ) {
        String title = defaultIfBlank(familyTitle, "신규 가족 생성 안내");
        return String.format(Locale.KOREA, """
                        %s

                        %s
                        %s

                        ▶ 자세히 확인하기
                        ☞ %s
                        """,
                title,
                headline,
                guide,
                DEFAULT_LINK
        ).trim();
    }

    // null/blank 값을 기본 문자열로 치환한다.
    private String defaultIfBlank(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }
}
