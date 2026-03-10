package hotspot.user.usage.giftUsage.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import hotspot.user.presentData.service.port.PresentDataRepository;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.subscription.service.SubscriptionService;
import hotspot.user.usage.giftUsage.controller.response.GiftUsageListResponse;
import hotspot.user.usage.giftUsage.domain.GiftUsage;
import hotspot.user.usage.giftUsage.service.port.GiftUsageRepository;

class FindGiftUsageServiceImplTest {

    @Mock
    GiftUsageRepository giftUsageRepository;

    @Mock
    PresentDataRepository presentDataRepository;

    @Mock
    SubscriptionService subscriptionService;

    FindGiftUsageServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new FindGiftUsageServiceImpl(
                giftUsageRepository,
                presentDataRepository,
                subscriptionService
        );
    }

    @Test
    @DisplayName("선물 데이터 조회 성공")
    void shouldReturnGiftUsageListSuccessfully() {

        Subscription subscription =
                Subscription.builder()
                        .id(1L)
                        .build();

        when(subscriptionService.findByMemberId(1L))
                .thenReturn(subscription);

        List<GiftUsage> giftUsages =
                List.of(
                        new GiftUsage(101L, 5242880, 1048576),
                        new GiftUsage(102L, 3145728, 1048576)
                );

        when(giftUsageRepository.findGiftUsageList(1L))
                .thenReturn(giftUsages);

        when(presentDataRepository.findGiftGiverNames(List.of(101L, 102L)))
                .thenReturn(
                        Map.of(
                                101L, "김태연",
                                102L, "아이유"
                        )
                );

        GiftUsageListResponse result =
                service.findGiftUsages(1L);

        assertEquals(2, result.giftUsages().size());
        assertEquals("김태연", result.giftUsages().get(0).giftUserName());
        assertEquals("아이유", result.giftUsages().get(1).giftUserName());
    }
}
