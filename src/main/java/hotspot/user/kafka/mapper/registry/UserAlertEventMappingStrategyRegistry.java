package hotspot.user.kafka.mapper.registry;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.KafkaErrorCode;
import hotspot.user.kafka.domain.KafkaEventType;
import hotspot.user.kafka.mapper.strategy.UserAlertEventMappingStrategy;

@Component
public class UserAlertEventMappingStrategyRegistry {

    private final Map<KafkaEventType, UserAlertEventMappingStrategy> strategiesByEventType;

    // 모든 KafkaEventType에 대해 매핑 전략을 하나씩 매칭해 레지스트리에 등록한다.
    public UserAlertEventMappingStrategyRegistry(List<UserAlertEventMappingStrategy> mappingStrategies) {
        this.strategiesByEventType = new EnumMap<>(KafkaEventType.class);
        for (KafkaEventType eventType : KafkaEventType.values()) {
            this.strategiesByEventType.put(eventType, resolveSingleStrategy(eventType, mappingStrategies));
        }
    }

    // 요청된 이벤트 타입에 해당하는 매핑 전략을 찾아 반환하고 없으면 예외를 발생시킨다.
    public UserAlertEventMappingStrategy resolve(KafkaEventType eventType) {
        UserAlertEventMappingStrategy strategy = strategiesByEventType.get(eventType);
        if (strategy == null) {
            throw new ApplicationException(KafkaErrorCode.UNSUPPORTED_KAFKA_EVENT_TYPE);
        }
        return strategy;
    }

    // 주어진 이벤트 타입을 지원하는 전략을 필터링해 정확히 1개인지 검증하고 그 전략을 반환한다.
    private UserAlertEventMappingStrategy resolveSingleStrategy(
            KafkaEventType eventType,
            List<UserAlertEventMappingStrategy> mappingStrategies
    ) {
        List<UserAlertEventMappingStrategy> matchedStrategies = mappingStrategies.stream()
                .filter(strategy -> strategy.supports(eventType))
                .toList();

        if (matchedStrategies.isEmpty()) {
            throw new ApplicationException(KafkaErrorCode.UNSUPPORTED_KAFKA_EVENT_TYPE);
        }
        if (matchedStrategies.size() > 1) {
            throw new ApplicationException(KafkaErrorCode.KAFKA_MAPPING_STRATEGY_DUPLICATED);
        }
        return matchedStrategies.get(0);
    }
}
