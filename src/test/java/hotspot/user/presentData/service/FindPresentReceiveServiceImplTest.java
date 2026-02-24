package hotspot.user.presentData.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.member.domain.Member;
import hotspot.user.presentData.controller.response.PresentDataResponse;
import hotspot.user.presentData.domain.PresentData;
import hotspot.user.presentData.service.port.PresentDataRepository;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.subscription.service.SubscriptionService;

@ExtendWith(MockitoExtension.class)
class FindPresentReceiveServiceImplTest {

    @Mock
    SubscriptionService subscriptionService;

    @Mock
    PresentDataRepository presentDataRepository;

    @InjectMocks
    FindPresentReceiveServiceImpl service;

    @Test
    @DisplayName("선물 받은 목록 조회 성공")
    void findPresentReceiveSuccess() {

        // given
        Long memberId = 1L;
        Long subId = 100L;

        Subscription subscription =
                Subscription.builder()
                        .id(subId)
                        .build();

        PresentData presentData =
                PresentData.builder()
                        .presentDataId(1L)
                        .dataAmount(1024L * 1024L) // 1GB in KB
                        .createdTime(LocalDateTime.now())
                        .provideSubscription(
                                Subscription.builder()
                                        .id(2L)
                                        .member(Member.builder().name("신진훈").build())
                                        .build()
                        )
                        .build();

        when(subscriptionService.findByMemberId(memberId))
                .thenReturn(subscription);

        when(presentDataRepository.findPresentReceive(subId))
                .thenReturn(List.of(presentData));

        // when
        PresentDataResponse result =
                service.findPresentReceive(memberId);

        // then
        assertThat(result.totalReceivedGb()).isEqualTo(1.0);
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).provideSubId()).isEqualTo(2L);
        assertThat(result.items().get(0).subName()).isEqualTo("신진훈");

        verify(subscriptionService).findByMemberId(memberId);
        verify(presentDataRepository).findPresentReceive(subId);
    }

    @Test
    @DisplayName("선물 제공 목록 조회 성공")
    void findPresentProvideSuccess() {

        // given
        Long memberId = 1L;
        Long subId = 100L;

        Subscription subscription =
                Subscription.builder()
                        .id(subId)
                        .build();

        PresentData presentData =
                PresentData.builder()
                        .presentDataId(2L)
                        .dataAmount(2048L * 1024L) // 2GB in KB
                        .createdTime(LocalDateTime.now())
                        .targetSubscription(
                                Subscription.builder()
                                        .id(3L)
                                        .member(Member.builder().name("김태연").build())
                                        .build()
                        )
                        .build();

        when(subscriptionService.findByMemberId(memberId))
                .thenReturn(subscription);

        when(presentDataRepository.findPresentProvide(subId))
                .thenReturn(List.of(presentData));

        // when
        PresentDataResponse result =
                service.findPresentProvide(memberId);

        // then
        assertThat(result.totalReceivedGb()).isEqualTo(2.0);
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).provideSubId()).isEqualTo(3L);
        assertThat(result.items().get(0).subName()).isEqualTo("김태연");

        verify(subscriptionService).findByMemberId(memberId);
        verify(presentDataRepository).findPresentProvide(subId);
    }
}
