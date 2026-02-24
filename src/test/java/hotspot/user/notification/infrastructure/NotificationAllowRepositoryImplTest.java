package hotspot.user.notification.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.notification.domain.NotificationAllow;
import hotspot.user.notification.domain.NotificationCategory;
import hotspot.user.notification.infrastructure.entity.NotificationAllowEntity;
import hotspot.user.subscription.infrastructure.entity.SubscriptionEntity;

@ExtendWith(MockitoExtension.class)
class NotificationAllowRepositoryImplTest {

    @Mock
    private NotificationAllowJpaRepository notificationAllowJpaRepository;

    @InjectMocks
    private NotificationAllowRepositoryImpl notificationAllowRepository;

    @Test
    @DisplayName("findAllBySubId returns mapped allows")
    void findAllBySubIdSuccess() {
        Long subId = 100L;
        NotificationAllowEntity dataEntity = NotificationAllowEntity.builder()
                .notificationAllowId(1L)
                .subscription(SubscriptionEntity.builder().subId(subId).build())
                .notificationCategory(NotificationCategory.DATA)
                .notificationAllow(true)
                .isDeleted(false)
                .build();
        NotificationAllowEntity policyEntity = NotificationAllowEntity.builder()
                .notificationAllowId(2L)
                .subscription(SubscriptionEntity.builder().subId(subId).build())
                .notificationCategory(NotificationCategory.POLICY)
                .notificationAllow(false)
                .isDeleted(false)
                .build();
        given(notificationAllowJpaRepository.findBySubscriptionSubIdAndIsDeletedFalse(subId))
                .willReturn(List.of(dataEntity, policyEntity));

        List<NotificationAllow> result = notificationAllowRepository.findAllBySubId(subId);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getNotificationCategory()).isEqualTo(NotificationCategory.DATA);
        assertThat(result.get(0).getNotificationAllow()).isTrue();
        assertThat(result.get(1).getNotificationCategory()).isEqualTo(NotificationCategory.POLICY);
        assertThat(result.get(1).getNotificationAllow()).isFalse();
    }

    @Test
    @DisplayName("findBySubIdAndCategory returns mapped allow")
    void findBySubIdAndCategorySuccess() {
        Long subId = 100L;
        NotificationAllowEntity entity = NotificationAllowEntity.builder()
                .notificationAllowId(1L)
                .subscription(SubscriptionEntity.builder().subId(subId).build())
                .notificationCategory(NotificationCategory.APP_SERVICE)
                .notificationAllow(true)
                .isDeleted(false)
                .build();
        given(notificationAllowJpaRepository
                .findBySubscriptionSubIdAndNotificationCategoryAndIsDeletedFalse(
                        subId, NotificationCategory.APP_SERVICE))
                .willReturn(Optional.of(entity));

        Optional<NotificationAllow> result = notificationAllowRepository
                .findBySubIdAndCategory(subId, NotificationCategory.APP_SERVICE);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getNotificationAllow()).isTrue();
    }

    @Test
    @DisplayName("save returns mapped saved allow")
    void saveSuccess() {
        Long subId = 100L;
        NotificationAllow target = NotificationAllow.builder()
                .subId(subId)
                .notificationCategory(NotificationCategory.PRESENT)
                .notificationAllow(true)
                .isDeleted(false)
                .build();
        NotificationAllowEntity savedEntity = NotificationAllowEntity.builder()
                .notificationAllowId(10L)
                .subscription(SubscriptionEntity.builder().subId(subId).build())
                .notificationCategory(NotificationCategory.PRESENT)
                .notificationAllow(true)
                .isDeleted(false)
                .build();
        given(notificationAllowJpaRepository.save(any(NotificationAllowEntity.class)))
                .willReturn(savedEntity);

        NotificationAllow saved = notificationAllowRepository.save(target);

        assertThat(saved.getId()).isEqualTo(10L);
        assertThat(saved.getNotificationCategory()).isEqualTo(NotificationCategory.PRESENT);
        assertThat(saved.getNotificationAllow()).isTrue();
        then(notificationAllowJpaRepository).should().save(any(NotificationAllowEntity.class));
    }
}
