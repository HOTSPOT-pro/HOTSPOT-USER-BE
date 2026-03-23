package hotspot.user.kafka.mapper.template;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.KafkaErrorCode;
import hotspot.user.kafka.domain.NotificationType;
import hotspot.user.kafka.mapper.support.AlertEventMappingSupport;
import hotspot.user.kafka.model.AlertNotificationMappingResult;

public final class AlertMessageTemplateRegistry {

    private static final Map<NotificationType, MessageTemplate> TEMPLATES = new EnumMap<>(NotificationType.class);

    static {
        // Usage threshold
        put(NotificationType.SINGLE_USAGE_THRESHOLD_50, "내 데이터 안내", "데이터가 50%% 이하로 남았어요. 필요한 곳에만 조금 아껴 써볼까요?");
        put(NotificationType.SINGLE_USAGE_THRESHOLD_30, "내 데이터 안내", "데이터가 30%% 이하로 남았어요. 영상/업데이트 사용을 잠시 줄이면 도움이 돼요.");
        put(NotificationType.SINGLE_USAGE_THRESHOLD_10, "내 데이터 안내", "데이터가 10%% 이하로 남았어요. 꼭 필요한 서비스부터 사용해 주세요.");
        put(NotificationType.SINGLE_USAGE_EXHAUSTED, "내 데이터 안내", "데이터를 모두 사용했어요. 잠시 Wi-Fi를 이용하거나 다음 달을 기다려 주세요.");

        put(NotificationType.FAMILY_USAGE_THRESHOLD_50, "가족 데이터 안내", "가족 공유 데이터가 50%% 이하로 남았어요. 함께 아껴 쓰면 좋아요.");
        put(NotificationType.FAMILY_USAGE_THRESHOLD_30, "가족 데이터 안내", "가족 공유 데이터가 30%% 이하로 남았어요. 필요한 사용부터 우선해 볼까요?");
        put(NotificationType.FAMILY_USAGE_THRESHOLD_10, "가족 데이터 안내", "가족 공유 데이터가 10%% 이하로 남았어요. 잠시 사용을 줄이면 안전해요.");
        put(NotificationType.FAMILY_USAGE_EXHAUSTED, "가족 데이터 안내", "가족 공유 데이터를 모두 사용했어요. Wi-Fi를 이용하거나 다음 달을 기다려 주세요.");

        put(
                NotificationType.PRESENT_USAGE_THRESHOLD_50,
                "선물 데이터 안내",
                "\"%s\"님께 선물 받은 데이터가 50%% 이하로 남았어요. 필요할 때 쓰도록 조금만 아껴볼까요?"
        );
        put(
                NotificationType.PRESENT_USAGE_THRESHOLD_30,
                "선물 데이터 안내",
                "\"%s\"님께 선물 받은 데이터가 30%% 이하로 남았어요. 영상/다운로드는 잠시만 조절해도 좋아요."
        );
        put(
                NotificationType.PRESENT_USAGE_THRESHOLD_10,
                "선물 데이터 안내",
                "\"%s\"님께 선물 받은 데이터가 10%% 이하로 남았어요. 꼭 필요한 용도로 먼저 사용해 주세요."
        );
        put(
                NotificationType.PRESENT_USAGE_EXHAUSTED,
                "선물 데이터 안내",
                "\"%s\"님께 선물 받은 데이터를 모두 사용했어요. Wi-Fi를 이용하거나 추가 선물을 기다려 주세요."
        );

        // Policy
        put(
                NotificationType.TIME_WINDOW_POLICY_APPLIED,
                "이용 시간 관리 안내",
                "\"%s\"가 설정되었어요. 해당 시간에는 데이터 사용이 잠시 제한될 수 있어요."
        );
        put(NotificationType.TIME_WINDOW_POLICY_RELEASED, "이용 시간 관리 안내", "\"%s\"가 해제되었어요. 데이터 사용을 다시 자유롭게 할 수 있어요.");
        put(NotificationType.IMMEDIATE_BLOCK_APPLIED, "데이터 사용 제한 안내", "데이터 사용이 잠시 제한되었어요. 필요하면 보호자에게 알려주세요.");
        put(NotificationType.IMMEDIATE_BLOCK_RELEASED, "데이터 사용 제한 안내", "데이터 사용 제한이 해제되었어요. 다시 사용할 수 있어요.");

        // App service
        put(NotificationType.SERVICE_ACCESS_BLOCKED, "서비스 이용 안내", "\"%s\" 이용이 잠시 제한되었어요.");
        put(NotificationType.SERVICE_ACCESS_RELEASED, "서비스 이용 안내", "\"%s\" 이용 제한이 해제되었어요. 다시 사용할 수 있어요.");

        // Present
        put(
                NotificationType.PRESENT_DATA,
                "데이터 선물 도착",
                "\"%s\"님이 데이터 %s를 보내줬어요. 고마운 마음을 전해볼까요?"
        );

        // Family
        put(NotificationType.FAMILY_CREATE_APPROVED, "가족 생성 완료", "\"%s\"님의 가족 생성이 승인되었어요. 이제 함께 이용할 수 있어요.");
        put(
                NotificationType.FAMILY_CREATE_REJECTED,
                "가족 생성 안내",
                "\"%s\"님의 가족 생성 요청이 반려되었어요. 내용을 확인하고 다시 신청해 주세요."
        );
        put(NotificationType.FAMILY_MEMBER_ADD_APPROVED, "가족 구성원 추가 완료", "\"%s\"님의 구성원 추가가 승인되었어요.");
        put(
                NotificationType.FAMILY_MEMBER_ADD_REJECTED,
                "가족 구성원 추가 안내",
                "\"%s\"님의 구성원 추가 요청이 반려되었어요. 내용을 확인하고 다시 신청해 주세요."
        );
        put(NotificationType.FAMILY_MEMBER_REMOVE_APPROVED, "가족 구성원 삭제 완료", "\"%s\"님의 구성원 삭제가 승인되었어요.");
        put(
                NotificationType.FAMILY_MEMBER_REMOVE_REJECTED,
                "가족 구성원 삭제 안내",
                "\"%s\"님의 구성원 삭제 요청이 반려되었어요. 내용을 확인하고 다시 신청해 주세요."
        );
    }

    private AlertMessageTemplateRegistry() {
    }

    // 알림 타입에 맞는 템플릿을 찾아 제목/본문이 채워진 매핑 결과를 만든다.
    public static AlertNotificationMappingResult create(NotificationType type, Object... bodyArgs) {
        MessageTemplate template = TEMPLATES.get(type);
        if (template == null) {
            throw new ApplicationException(KafkaErrorCode.UNSUPPORTED_KAFKA_EVENT_TYPE);
        }
        return AlertEventMappingSupport.create(type, template.title(), template.render(bodyArgs));
    }

    // 알림 타입별 제목/본문 템플릿을 레지스트리에 등록한다.
    private static void put(NotificationType type, String title, String bodyTemplate) {
        TEMPLATES.put(type, new MessageTemplate(title, bodyTemplate));
    }

    private record MessageTemplate(String title, String bodyTemplate) {
        // 본문 템플릿의 치환자(%s 등)에 인자를 적용해 최종 문구를 생성한다.
        private String render(Object... args) {
            return String.format(Locale.ROOT, bodyTemplate, args);
        }
    }
}
