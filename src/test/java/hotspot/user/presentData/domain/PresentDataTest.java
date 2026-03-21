package hotspot.user.presentData.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.PresentDataErrorCode;
import hotspot.user.subscription.domain.Subscription;

class PresentDataTest {

    @Test
    @DisplayName("PresentData 빌더 정상 생성 및 값 보존")
    void shouldCreatePresentDataSuccessfully() {

        // given
        Subscription targetSubscription = mock(Subscription.class);
        Subscription provideSubscription = mock(Subscription.class);

        when(targetSubscription.getId()).thenReturn(1L);
        when(provideSubscription.getId()).thenReturn(2L);

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

    @Test
    @DisplayName("자가 선물 시 생성자에서 예외 발생")
    void shouldThrowExceptionWhenSelfGifting() {
        // given
        Subscription subscription = mock(Subscription.class);
        when(subscription.getId()).thenReturn(1L);

        // when & then
        assertThatThrownBy(() -> PresentData.builder()
                .presentDataId(1L)
                .targetSubscription(subscription)
                .provideSubscription(subscription)
                .dataAmount(1048576L)
                .build())
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", PresentDataErrorCode.PRESENT_DATA_SELF_GIFT);
    }
}
