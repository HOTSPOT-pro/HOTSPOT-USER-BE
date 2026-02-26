package hotspot.user.usage.subscriptionUsage.infrastructure.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.plan.domain.DataPeriod;
import hotspot.user.usage.subscriptionUsage.domain.SubscriptionUsage;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class SubscriptionUsageRepositoryImplTest {

    @Mock
    private SubscriptionUsageRedisRepository redisRepository;

    @InjectMocks
    private SubscriptionUsageRepositoryImpl repository;

    @Test
    @DisplayName("findSubscriptionUsage: redisRepository를 그대로 위임 호출한다")
    void shouldCallRedisRepository_findSubscriptionUsage() {

        Long subId = 1L;
        DataPeriod dataPeriod = DataPeriod.MONTH;

        SubscriptionUsage expected = mock(SubscriptionUsage.class);

        when(redisRepository.findSubscriptionUsage(subId, dataPeriod))
                .thenReturn(expected);

        SubscriptionUsage result =
                repository.findSubscriptionUsage(subId, dataPeriod);

        assertEquals(expected, result);

        verify(redisRepository, times(1))
                .findSubscriptionUsage(subId, dataPeriod);
    }

    @Test
    @DisplayName("findRemainingPlanKb: redisRepository를 그대로 위임 호출한다")
    void shouldCallRedisRepository_findRemainingPlanKb() {

        Long subId = 1L;
        DataPeriod dataPeriod = DataPeriod.DAY;

        when(redisRepository.findRemainingPlanKb(subId, dataPeriod))
                .thenReturn(1234L);

        long result =
                repository.findRemainingPlanKb(subId, dataPeriod);

        assertEquals(1234L, result);

        verify(redisRepository, times(1))
                .findRemainingPlanKb(subId, dataPeriod);
    }
}