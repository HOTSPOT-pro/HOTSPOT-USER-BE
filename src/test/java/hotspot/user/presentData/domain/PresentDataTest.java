package hotspot.user.presentData.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import hotspot.user.subscription.domain.Subscription;

class PresentDataTest {

    @Test
    @DisplayName("PresentData 빌더 정상 생성 및 값 보존")
    void shouldCreatePresentDataSuccessfully() {

        // given
        Subscription targetSubscription = mock(Subscription.class);
        Subscription provideSubscription = mock(Subscription.class);

        LocalDateTime now = LocalDateTime.now();

        // when
        PresentData presentData = PresentData.builder()
                .presentDataId(1L)
                .targetSubscription(targetSubscription)
                .provideSubscription(provideSubscription)
                .dataAmount(1048576L)
                .createdTime(now)
                .build();

        // then
        assertNotNull(presentData);
        assertEquals(1L, presentData.getPresentDataId());
        assertEquals(targetSubscription, presentData.getTargetSubscription());
        assertEquals(provideSubscription, presentData.getProvideSubscription());
        assertEquals(1048576L, presentData.getDataAmount());
        assertEquals(now, presentData.getCreatedTime());
    }
}
