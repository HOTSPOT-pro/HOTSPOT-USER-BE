package hotspot.user.policy.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.AuthErrorCode;
import hotspot.user.common.exception.code.FamilyErrorCode;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.outbox.consistencyOutbox.domain.event.subscription.app.AppBlockListUpdateEvent;
import hotspot.user.outbox.notificationOutbox.domain.event.AlertAction;
import hotspot.user.outbox.notificationOutbox.domain.event.ServiceAccessAlertOutboxEvent;
import hotspot.user.outbox.notificationOutbox.service.port.UserAlertNotificationOutboxPort;
import hotspot.user.policy.controller.port.UpdateAppBlockedServiceService;
import hotspot.user.policy.controller.request.UpdateAppBlockedServiceRequest;
import hotspot.user.policy.controller.response.UpdateAppBlockedServiceResponse;
import hotspot.user.policy.domain.AppBlockedService;
import hotspot.user.policy.domain.mapper.AppBlockedServiceMapper;
import hotspot.user.policy.service.port.AppBlockedServiceRepository;
import hotspot.user.policy.service.port.BlockedServiceSubRepository;
import lombok.RequiredArgsConstructor;

/**
 * 회선의 앱 차단 서비스 목록을 갱신한다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UpdateAppBlockedServiceServiceImpl implements UpdateAppBlockedServiceService {

    private final BlockedServiceSubRepository blockedServiceSubRepository;
    private final FamilySubscriptionRepository familySubscriptionRepository;
    private final AppBlockedServiceRepository appBlockedServiceRepository;
    private final UserAlertNotificationOutboxPort userAlertNotificationOutboxPort;

    // ✅ 정책 동기화 outbox(스냅샷 이벤트)
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public UpdateAppBlockedServiceResponse updateAppBlockedService(
            UpdateAppBlockedServiceRequest request,
            Long requesterFamilyId,
            FamilyRole requesterRole) {

        // 차단 서비스 변경은 OWNER만 가능하다.
        if (requesterRole != FamilyRole.OWNER) {
            throw new ApplicationException(AuthErrorCode.ACCESS_DENIED);
        }

        Long subId = request.subId();

        // 대상 회선과 가족 소속을 검증한다.
        FamilySubscription familySub = familySubscriptionRepository.findBySubId(subId)
                .orElseThrow(() -> new ApplicationException(FamilyErrorCode.FAMILY_SUBSCRIPTION_NOT_FOUND));

        if (!familySub.getFamily().getId().equals(requesterFamilyId)) {
            throw new ApplicationException(FamilyErrorCode.NOT_FAMILY_MEMBER);
        }

        // 요청된 서비스 ID가 모두 유효한지 검증한다.
        Set<Long> targetIds = new HashSet<>(request.blockedServiceIdList());
        if (!targetIds.isEmpty()) {
            long validCount = appBlockedServiceRepository.countByIdIn(targetIds);
            if (validCount != targetIds.size()) {
                throw new ApplicationException(FamilyErrorCode.BLOCKED_SERVICE_NOT_FOUND);
            }
        }

        // 현재 활성 차단 서비스 ID 목록을 조회한다.
        Set<Long> existingIds = new HashSet<>(blockedServiceSubRepository.findActiveServiceIdsBySubId(subId));

        // 변경분 계산: 추가/해제 대상 서비스 ID.
        Set<Long> toAddIds = new HashSet<>(targetIds);
        toAddIds.removeAll(existingIds);

        Set<Long> toRemoveIds = new HashSet<>(existingIds);
        toRemoveIds.removeAll(targetIds);

        if (!toAddIds.isEmpty()) {
            blockedServiceSubRepository.saveAll(subId, toAddIds);
        }
        if (!toRemoveIds.isEmpty()) {
            blockedServiceSubRepository.deleteAll(subId, toRemoveIds);
        }
        publishServiceAccessAlerts(subId, familySub.getFamily().getId(), toAddIds, toRemoveIds);

        List<Long> finalBlockedIdList =
                blockedServiceSubRepository.findActiveServiceIdsBySubId(subId);

        // Outbox 이벤트 발행: 스냅샷(list) 기반
        publishAppBlockSnapshotEvent(subId, finalBlockedIdList);

        return AppBlockedServiceMapper.toUpdateAppBlockedServiceResponse(
                familySub.getFamily().getId(),
                subId,
                finalBlockedIdList);
    }

    private void publishAppBlockSnapshotEvent(Long subId, List<Long> finalBlockedIds) {
        eventPublisher.publishEvent(
                new AppBlockListUpdateEvent(
                        "APP_BLOCK_LIST_UPDATED",
                        subId,
                        finalBlockedIds,
                        UUID.randomUUID().toString()
                )
        );
    }

    // 변경된 서비스 ID 기준으로 차단/해제 알림 outbox 이벤트를 발행한다.
    private void publishServiceAccessAlerts(
            Long subId,
            Long familyId,
            Set<Long> addedServiceIds,
            Set<Long> removedServiceIds
    ) {
        Set<Long> changedServiceIds = new HashSet<>(addedServiceIds);
        changedServiceIds.addAll(removedServiceIds);
        if (changedServiceIds.isEmpty()) {
            return;
        }

        Map<Long, String> serviceNameById = new HashMap<>();
        List<AppBlockedService> services = appBlockedServiceRepository.findAllByAppBlockedServiceIds(
                new ArrayList<>(changedServiceIds)
        );
        for (AppBlockedService service : services) {
            serviceNameById.put(service.getId(), service.getName());
        }

        for (Long serviceId : addedServiceIds) {
            userAlertNotificationOutboxPort.appendServiceAccessAlert(new ServiceAccessAlertOutboxEvent(
                    subId,
                    familyId,
                    serviceNameById.getOrDefault(serviceId, "service"),
                    AlertAction.APPLIED
            ));
        }
        for (Long serviceId : removedServiceIds) {
            userAlertNotificationOutboxPort.appendServiceAccessAlert(new ServiceAccessAlertOutboxEvent(
                    subId,
                    familyId,
                    serviceNameById.getOrDefault(serviceId, "service"),
                    AlertAction.RELEASED
            ));
        }
    }
}
