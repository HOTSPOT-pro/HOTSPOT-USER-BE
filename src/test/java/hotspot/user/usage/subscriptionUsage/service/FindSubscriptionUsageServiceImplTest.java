package hotspot.user.usage.subscriptionUsage.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.presentData.service.port.PresentDataRepository;
import hotspot.user.usage.subscriptionUsage.controller.response.SubscriptionUsageResponse;
import hotspot.user.usage.subscriptionUsage.domain.GiftUsage;
import hotspot.user.usage.subscriptionUsage.domain.SubscriptionUsage;
import hotspot.user.usage.subscriptionUsage.service.port.SubscriptionUsageRepository;

@ExtendWith(MockitoExtension.class)
class FindSubscriptionUsageServiceImplTest {

    @Mock
    SubscriptionUsageRepository subscriptionUsageRepository;

    @Mock
    PresentDataRepository presentDataRepository;

    @InjectMocks
    FindSubscriptionUsageServiceImpl service;

    @Test
    @DisplayName("개인 데이터 사용량 서비스 정상 동작")
    void shouldReturnSubscriptionUsageSuccessfully() {

        Long subId = 1L;

        SubscriptionUsage mockUsage =
                new SubscriptionUsage(
                        subId,
                        24 * 1024 * 1024, // 24GB KB 단위
                        0,
                        List.of(
                                new GiftUsage(
                                        69395L,
                                        1 * 1024 * 1024,
                                        512 * 1024 // 0.5GB
                                )
                        )
                );

        when(subscriptionUsageRepository.findSubscriptionUsage(subId))
                .thenReturn(mockUsage);

        when(presentDataRepository.findGiftGiverNames(eq(List.of(69395L))))
                .thenReturn(Map.of(69395L, "김태연"));

        SubscriptionUsageResponse response =
                service.findSubscriptionUsage(subId);

        assertNotNull(response);
        assertEquals(subId, response.subId());

        assertEquals(1, response.giftUsages().size());
        assertEquals("김태연",
                response.giftUsages().get(0).giftUserName());

        verify(subscriptionUsageRepository)
                .findSubscriptionUsage(subId);

        verify(presentDataRepository)
                .findGiftGiverNames(anyList());
    }
}
