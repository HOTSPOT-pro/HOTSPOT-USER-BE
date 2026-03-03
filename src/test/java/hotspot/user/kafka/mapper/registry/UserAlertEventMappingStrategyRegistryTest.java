package hotspot.user.kafka.mapper.registry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.KafkaErrorCode;
import hotspot.user.kafka.domain.KafkaEventType;
import hotspot.user.kafka.dto.UserAlertEvent;
import hotspot.user.kafka.mapper.strategy.UserAlertEventMappingStrategy;
import hotspot.user.kafka.mapper.strategy.appservice.AppServiceAlertEventMappingStrategy;
import hotspot.user.kafka.mapper.strategy.family.FamilyMemberApplyAlertEventMappingStrategy;
import hotspot.user.kafka.mapper.strategy.policy.PolicyAlertEventMappingStrategy;
import hotspot.user.kafka.mapper.strategy.present.PresentDataAlertEventMappingStrategy;
import hotspot.user.kafka.mapper.strategy.usage.UsageThresholdAlertEventMappingStrategy;
import hotspot.user.kafka.model.AlertNotificationMappingResult;

class UserAlertEventMappingStrategyRegistryTest {

    @Test
    @DisplayName("resolves single strategy by event type")
    void resolveSingleStrategy() {
        UserAlertEventMappingStrategyRegistry registry = new UserAlertEventMappingStrategyRegistry(defaultStrategies());

        UserAlertEventMappingStrategy strategy = registry.resolve(KafkaEventType.USAGE_THRESHOLD);

        assertThat(strategy).isInstanceOf(UsageThresholdAlertEventMappingStrategy.class);
    }

    @Test
    @DisplayName("throws when strategy is duplicated for same event type")
    void duplicatedStrategy() {
        UserAlertEventMappingStrategy duplicated = new UserAlertEventMappingStrategy() {
            @Override
            public boolean supports(KafkaEventType eventType) {
                return eventType == KafkaEventType.USAGE_THRESHOLD;
            }

            @Override
            public AlertNotificationMappingResult map(UserAlertEvent event) {
                throw new UnsupportedOperationException();
            }
        };

        assertThatThrownBy(() -> new UserAlertEventMappingStrategyRegistry(
                List.of(
                        new UsageThresholdAlertEventMappingStrategy(),
                        duplicated,
                        new PolicyAlertEventMappingStrategy(),
                        new AppServiceAlertEventMappingStrategy(),
                        new PresentDataAlertEventMappingStrategy(),
                        new FamilyMemberApplyAlertEventMappingStrategy()
                )
        ))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(KafkaErrorCode.KAFKA_MAPPING_STRATEGY_DUPLICATED.getMessage());
    }

    @Test
    @DisplayName("throws when event type has no strategy")
    void unsupportedEventType() {
        assertThatThrownBy(() -> new UserAlertEventMappingStrategyRegistry(
                List.of(
                        new UsageThresholdAlertEventMappingStrategy(),
                        new AppServiceAlertEventMappingStrategy(),
                        new PresentDataAlertEventMappingStrategy(),
                        new FamilyMemberApplyAlertEventMappingStrategy()
                )
        ))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(KafkaErrorCode.UNSUPPORTED_KAFKA_EVENT_TYPE.getMessage());
    }

    private List<UserAlertEventMappingStrategy> defaultStrategies() {
        return List.of(
                new UsageThresholdAlertEventMappingStrategy(),
                new PolicyAlertEventMappingStrategy(),
                new AppServiceAlertEventMappingStrategy(),
                new PresentDataAlertEventMappingStrategy(),
                new FamilyMemberApplyAlertEventMappingStrategy()
        );
    }
}
