package hotspot.user.usage.giftUsage.infrastructure.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import hotspot.user.usage.giftUsage.domain.GiftUsage;

class GiftUsageRepositoryImplTest {

    @Mock
    GiftUsageRedisRepository redisRepository;

    @InjectMocks
    GiftUsageRepositoryImpl repository;

    public GiftUsageRepositoryImplTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("RedisRepository 호출 후 결과 반환")
    void shouldReturnGiftUsageList() {

        List<GiftUsage> mockResult =
                List.of(
                        new GiftUsage(101L, 5242880, 1048576)
                );

        when(redisRepository.findGiftUsageList(1L))
                .thenReturn(mockResult);

        List<GiftUsage> result =
                repository.findGiftUsageList(1L);

        assertEquals(1, result.size());
        assertEquals(101L, result.get(0).giftId());
    }
}
