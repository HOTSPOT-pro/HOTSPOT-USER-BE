package hotspot.user.dispatch.sms.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import hotspot.user.dispatch.sms.config.SmsProperties;
import hotspot.user.dispatch.sms.dto.SmsDispatchCommand;
import hotspot.user.kafka.domain.NotificationType;

class SmsMessageBuilderTest {

    @Test
    @DisplayName("builds usage guide format using title and usage fields")
    void buildMessage() {
        SmsMessageBuilder builder = new SmsMessageBuilder(
                new SmsProperties(
                        true, "noop", "HOTSPOT",
                        "api-key", "api-secret", "https://api.solapi.com", "/messages/v4/send-many/detail"
                )
        );
        SmsDispatchCommand command = command("SINGLE_USAGE_THRESHOLD_50");

        String message = builder.build(command, NotificationType.SINGLE_USAGE_THRESHOLD_50);

        assertThat(message)
                .contains("[HOTSPOT]\n이번 달 데이터 사용량 안내")
                .contains("「유쓰 5G 데이터 플러스」")
                .contains("- 제공량: 110GB")
                .contains("- 사용량: 80% 88.02GB")
                .contains("▶ 요금제 사용량 확인하기\n☞ https://hotspot.pics")
                .contains("https://hotspot.pics");
    }

    @Test
    @DisplayName("builds family usage guide without plan placeholder")
    void buildFamilyUsageMessage() {
        SmsMessageBuilder builder = new SmsMessageBuilder(
                new SmsProperties(
                        true, "noop", "HOTSPOT",
                        "api-key", "api-secret", "https://api.solapi.com", "/messages/v4/send-many/detail"
                )
        );
        SmsDispatchCommand command = command("FAMILY_USAGE_THRESHOLD_30");

        String message = builder.build(command, NotificationType.FAMILY_USAGE_THRESHOLD_30);

        assertThat(message)
                .contains("고객님, 가족 공유 데이터 사용량을 안내해 드립니다.")
                .doesNotContain("「");
    }

    @Test
    @DisplayName("builds present usage guide with sender name")
    void buildPresentUsageMessage() {
        SmsMessageBuilder builder = new SmsMessageBuilder(
                new SmsProperties(
                        true, "noop", "HOTSPOT",
                        "api-key", "api-secret", "https://api.solapi.com", "/messages/v4/send-many/detail"
                )
        );
        SmsDispatchCommand command = new SmsDispatchCommand(
                1L,
                1L,
                "evt-1",
                "PRESENT_USAGE_THRESHOLD_10",
                "이번 달 데이터 사용량 안내",
                "content",
                LocalDateTime.of(2026, 2, 23, 10, 15, 30),
                null,
                "10GB",
                "90%",
                "9.0GB",
                "민수"
        );

        String message = builder.build(command, NotificationType.PRESENT_USAGE_THRESHOLD_10);

        assertThat(message).contains("민수님께 선물 받은 데이터 사용량을 안내해 드립니다.");
    }

    @Test
    @DisplayName("builds family create sms template")
    void buildFamilyCreateMessage() {
        SmsMessageBuilder builder = new SmsMessageBuilder(
                new SmsProperties(
                        true, "noop", "HOTSPOT",
                        "api-key", "api-secret", "https://api.solapi.com", "/messages/v4/send-many/detail"
                )
        );
        SmsDispatchCommand command = command("FAMILY_CREATE_APPROVED");

        String message = builder.build(command, NotificationType.FAMILY_CREATE_APPROVED);

        assertThat(message)
                .startsWith("[HOTSPOT]\n")
                .contains("가족 생성 승인 안내")
                .contains("▶")
                .contains("☞ https://hotspot.pics")
                .doesNotContain("▶ 자세히 확인하기\n\n☞");
    }

    private SmsDispatchCommand command(String type) {
        return new SmsDispatchCommand(
                1L,
                1L,
                "evt-1",
                type,
                "이번 달 데이터 사용량 안내",
                "content",
                LocalDateTime.of(2026, 2, 23, 10, 15, 30),
                "유쓰 5G 데이터 플러스",
                "110GB",
                "80%",
                "88.02GB",
                null
        );
    }
}
