package hotspot.user.presentData.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.plan.domain.DataPeriod;
import hotspot.user.presentData.domain.SubUsage;
import hotspot.user.presentData.infrastructure.entity.PresentDataEntity;
import hotspot.user.subscription.infrastructure.entity.SubscriptionEntity;

@ExtendWith(MockitoExtension.class)
class PresentDataRepositoryImplTest {

    @Mock
    private PresentDataJpaRepository presentDataJpaRepository;

    @Mock
    private FamilySubUsageRedisRepository redisRepository;

    @InjectMocks
    private PresentDataRepositoryImpl repository;

    @Test
    @DisplayName("giftId → giverName 매핑 정상 변환")
    void shouldReturnGiftGiverNamesSuccessfully() {

        List<Long> giftIds = List.of(1L, 2L);

        PresentDataJpaRepository.GiftGiverRow row1 =
                new PresentDataJpaRepository.GiftGiverRow() {
                    public Long getGiftId() { return 1L; }
                    public String getGiverName() { return "김태연"; }
                };

        PresentDataJpaRepository.GiftGiverRow row2 =
                new PresentDataJpaRepository.GiftGiverRow() {
                    public Long getGiftId() { return 2L; }
                    public String getGiverName() { return "홍길동"; }
                };

        when(presentDataJpaRepository.findGiftGivers(giftIds))
                .thenReturn(List.of(row1, row2));

        Map<Long, String> result =
                repository.findGiftGiverNames(giftIds);

        assertEquals(2, result.size());
        assertEquals("김태연", result.get(1L));
        assertEquals("홍길동", result.get(2L));
    }

    @Test
    @DisplayName("giftIds가 비어있으면 빈 Map 반환")
    void shouldReturnEmptyMapWhenGiftIdsEmpty() {

        Map<Long, String> result =
                repository.findGiftGiverNames(List.of());

        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("targetSubId로 선물 받은 목록 조회 성공")
    void shouldReturnPresentReceiveSuccessfully() {

        Long subId = 10L;

        SubscriptionEntity targetSub =
                SubscriptionEntity.builder()
                        .subId(subId)
                        .build();

        SubscriptionEntity provideSub =
                SubscriptionEntity.builder()
                        .subId(20L)
                        .build();

        PresentDataEntity entity =
                PresentDataEntity.builder()
                        .presentDataId(1L)
                        .targetSubscription(targetSub)
                        .provideSubscription(provideSub)
                        .dataAmount(1000L)
                        .createdTime(LocalDateTime.now())
                        .build();

        when(presentDataJpaRepository.findAllByTargetSubId(subId))
                .thenReturn(List.of(entity));

        List<?> result = repository.findPresentReceive(subId);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("provideSubId로 선물 제공 목록 조회 성공")
    void shouldReturnPresentProvideSuccessfully() {

        Long subId = 10L;

        SubscriptionEntity targetSub =
                SubscriptionEntity.builder()
                        .subId(20L)
                        .build();

        SubscriptionEntity provideSub =
                SubscriptionEntity.builder()
                        .subId(subId)
                        .build();

        PresentDataEntity entity =
                PresentDataEntity.builder()
                        .presentDataId(1L)
                        .targetSubscription(targetSub)
                        .provideSubscription(provideSub)
                        .dataAmount(1000L)
                        .createdTime(LocalDateTime.now())
                        .build();

        when(presentDataJpaRepository.findAllByProviderSubId(subId))
                .thenReturn(List.of(entity));

        List<?> result = repository.findPresentProvide(subId);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("subUsage Redis 조회 위임 성공")
    void shouldReturnSubUsageSuccessfully() {

        Map<Long, DataPeriod> periodMap =
                Map.of(1L, DataPeriod.MONTH);

        Map<Long, SubUsage> expected =
                Map.of(1L, new SubUsage(1000, 2000));

        when(redisRepository.findUsageAndLimit(periodMap))
                .thenReturn(expected);

        Map<Long, SubUsage> result =
                repository.findSubUsage(periodMap);

        assertEquals(1, result.size());
        assertEquals(expected, result);
    }
}
