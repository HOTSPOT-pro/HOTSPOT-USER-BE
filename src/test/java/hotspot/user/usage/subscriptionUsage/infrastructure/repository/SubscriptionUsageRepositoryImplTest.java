package hotspot.user.usage.subscriptionUsage.infrastructure.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.usage.subscriptionUsage.domain.SubscriptionUsage;

@ExtendWith(MockitoExtension.class)
class SubscriptionUsageRepositoryImplTest {

    @Mock
    private SubscriptionUsageRedisRepository redisRepository;

    @InjectMocks
    private SubscriptionUsageRepositoryImpl repository;

    @Test
    @DisplayName("redisRepository를 그대로 위임 호출한다")
    void shouldCallRedisRepository() {

        Long subId = 1L;

        SubscriptionUsage expected = mock(SubscriptionUsage.class);

        when(redisRepository.findSubscriptionUsage(subId))
                .thenReturn(expected);

        SubscriptionUsage result =
                repository.findSubscriptionUsage(subId);

        assertEquals(expected, result);

        verify(redisRepository, times(1))
                .findSubscriptionUsage(subId);
    }
}
