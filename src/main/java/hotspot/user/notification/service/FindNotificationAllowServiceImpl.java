package hotspot.user.notification.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.notification.controller.port.FindNotificationAllowService;
import hotspot.user.notification.controller.response.NotificationAllowListResponse;
import hotspot.user.notification.domain.mapper.NotificationAllowMapper;
import hotspot.user.notification.service.port.NotificationAllowRepository;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindNotificationAllowServiceImpl implements FindNotificationAllowService {

    private final NotificationAllowRepository notificationAllowRepository;
    private final SubscriptionService subscriptionService;

    // 해당 subId의 알림 허용 설정 목록을 조회한뒤 응답 DTO로 반환한다.
    @Override
    public NotificationAllowListResponse findNotificationAllows(Long memberId) {
        Subscription subscription = subscriptionService.findByMemberId(memberId);
        Long subId = subscription.getId();
        return NotificationAllowMapper.toListResponse(notificationAllowRepository.findAllBySubId(subId));
    }
}
