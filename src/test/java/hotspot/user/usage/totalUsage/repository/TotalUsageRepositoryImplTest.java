package hotspot.user.usage.totalUsage.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.plan.domain.DataPeriod;
import hotspot.user.usage.totalUsage.repository.schema.TotalUsage;

@ExtendWith(MockitoExtension.class)
class TotalUsageRepositoryImplTest {

    @Mock
    TotalUsageRedisRepository redisRepository;

    @InjectMocks
    TotalUsageRepositoryImpl repository;

    @Test
    @DisplayName("RedisRepository 호출 후 결과 반환")
    void shouldReturnTotalUsage() {

        TotalUsage totalUsage =
                new TotalUsage(
                        38.0,
                        31.0,
                        82,
                        17.0,
                        6.0,
                        8.0
                );

        when(redisRepository.findTotalUsage(7L, 2L, DataPeriod.MONTH))
                .thenReturn(totalUsage);

        TotalUsage result =
                repository.findTotalUsage(7L, 2L, DataPeriod.MONTH);

        assertEquals(38.0, result.totalDataAmount());
        assertEquals(31.0, result.totalDataRemainAmount());
        assertEquals(82, result.totalDataRemainPercent());
        assertEquals(17.0, result.subDataRemainAmount());
        assertEquals(6.0, result.giftDataRemainAmount());
        assertEquals(8.0, result.familyDataRemainAmount());

        verify(redisRepository)
                .findTotalUsage(7L, 2L, DataPeriod.MONTH);
    }
}
