package hotspot.user.notification.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.notification.controller.port.UpdateNotificationAllowService;
import hotspot.user.notification.controller.request.UpdateNotificationAllowRequest;
import hotspot.user.notification.controller.response.NotificationAllowResponse;
import hotspot.user.notification.domain.NotificationAllow;
import hotspot.user.notification.domain.mapper.NotificationAllowMapper;
import hotspot.user.notification.service.port.NotificationAllowRepository;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UpdateNotificationAllowServiceImpl implements UpdateNotificationAllowService {

    private final NotificationAllowRepository notificationAllowRepository;
    private final SubscriptionService subscriptionService;

    // 요청한 알림 허용값이 true면 "허용 처리", false면 "차단 처리"로 응답을 반환한다.
    @Override
    @Transactional
    public NotificationAllowResponse updateNotificationAllow(Long memberId, UpdateNotificationAllowRequest request) {
        Subscription subscription = subscriptionService.findByMemberId(memberId);
        Long subId = subscription.getId();

        if (request.notificationAllow()) {
            return handleAllow(subId, request);
        }

        return handleDisallow(subId, request);
    }

    // 해당 subId와 카테고리 설정이 이미 있으면 그대로 반환하고, 없으면 새로 생성해서 저장 후 반환한다.
    private NotificationAllowResponse handleAllow(Long subId, UpdateNotificationAllowRequest request) {
        NotificationAllow existing = notificationAllowRepository
                .findBySubIdAndCategory(subId, request.notificationCategory())
                .orElse(null);
        if (existing != null) {
            return NotificationAllowMapper.toResponse(existing);
        }

        NotificationAllow created = NotificationAllow.builder()
                .subId(subId)
                .notificationCategory(request.notificationCategory())
                .notificationAllow(true)
                .isDeleted(false)
                .build();
        NotificationAllow saved = notificationAllowRepository.save(created);
        return NotificationAllowMapper.toResponse(saved);
    }

    // 해당 subId와 카테고리 설정이 있으면 deactivate 상태로 저장해 soft 삭제 처리하고, 응답은 allow false로 반환한다.
    private NotificationAllowResponse handleDisallow(Long subId, UpdateNotificationAllowRequest request) {
        notificationAllowRepository
                .findBySubIdAndCategory(subId, request.notificationCategory())
                .ifPresent(existing -> notificationAllowRepository.save(existing.deactivate()));

        return NotificationAllowResponse.builder()
                .notificationCategory(request.notificationCategory())
                .notificationAllow(false)
                .build();
    }
}
