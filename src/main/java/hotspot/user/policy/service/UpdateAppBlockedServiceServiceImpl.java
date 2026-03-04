package hotspot.user.policy.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.AuthErrorCode;
import hotspot.user.common.exception.code.FamilyErrorCode;
import hotspot.user.common.exception.code.PolicyErrorCode;
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
import hotspot.user.policy.domain.BlockedServiceSub;
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

    // 정책 동기화 outbox(스냅샷 이벤트)
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public UpdateAppBlockedServiceResponse updateAppBlockedService(
            UpdateAppBlockedServiceRequest request,
            Long requesterFamilyId,
            FamilyRole requesterRole) {

        // 1. 요청자의 권한과 가족 소속을 검증한다.
        validateAuthorityAndFamily(request.subId(), requesterFamilyId, requesterRole);

        List<Long> targetIdsList = request.blockedServiceIdList();

        // 2. 해당 회선의 모든 정책 매핑 정보를 먼저 조회한다. (활성 + 비활성 포함)
        List<BlockedServiceSub> existingSubs = blockedServiceSubRepository.findBySubId(request.subId());

        // 3. 쿼리 최적화: 신규 요청 ID와 기존 등록된 ID를 합쳐 1번의 DB 조회를 수행한다.
        // Set: 중복 방지
        Set<Long> allRequiredBlockedServiceIds = existingSubs.stream()
                .map(BlockedServiceSub::getAppBlockedServiceId)
                .collect(Collectors.toSet());

        allRequiredBlockedServiceIds.addAll(targetIdsList);

        Map<Long, AppBlockedService> blockedServiceMap = Map.of();
        if (!allRequiredBlockedServiceIds.isEmpty()) {
            blockedServiceMap = appBlockedServiceRepository
                    .findAllByAppBlockedServiceIds(new ArrayList<>(allRequiredBlockedServiceIds)).stream()
                    .collect(Collectors.toMap(AppBlockedService::getId, p -> p));
        }

        // 4. 요청된 타겟 정책 검증 (Map을 이용한 O(1) 검증)
        for (Long targetId : targetIdsList) {
            AppBlockedService appBlockedService = blockedServiceMap.get(targetId);
            if (appBlockedService == null) {
                throw new ApplicationException(FamilyErrorCode.BLOCKED_SERVICE_NOT_FOUND);
            }

            if (!appBlockedService.isActive()) {
                throw new ApplicationException(PolicyErrorCode.INACTIVE_POLICY_CANNOT_APPLY);
            }
        }

        Map<Long, BlockedServiceSub> existingMap = existingSubs.stream()
                .collect(Collectors.toMap(BlockedServiceSub::getAppBlockedServiceId, sub -> sub));

        List<BlockedServiceSub> domainsToSave = new ArrayList<>();
        List<AppBlockedService> appliedAlertBlockedServices = new ArrayList<>();
        List<AppBlockedService> releasedAlertBlockedServices = new ArrayList<>();

        // 5. 요청된 정책들을 순회하며 신규 추가 또는 활성화 처리
        for (Long targetId : targetIdsList) {
            AppBlockedService appBlockedService = blockedServiceMap.get(targetId);
            BlockedServiceSub sub = existingMap.remove(targetId);

            if (sub != null) {
                // 비활성 상태인 경우에만 활성화 및 알림 대상 추가
                if (!sub.isActive()) {
                    sub.updateIsActive(true);
                    domainsToSave.add(sub);
                    appliedAlertBlockedServices.add(appBlockedService);
                }
            } else {
                // DB에 아예 없는 경우 신규 생성 및 알림 대상 추가
                BlockedServiceSub newSub = BlockedServiceSub.builder()
                        .subId(request.subId())
                        .appBlockedServiceId(targetId)
                        .isActive(true)
                        .build();
                domainsToSave.add(newSub);
                appliedAlertBlockedServices.add(appBlockedService);
            }
        }

        // 6. 요청에 없는데 DB에는 활성 상태로 남아있는 정책들을 비활성화 처리
        for (BlockedServiceSub remainingSub : existingMap.values()) {
            if (remainingSub.isActive()) {
                remainingSub.updateIsActive(false);
                domainsToSave.add(remainingSub);

                // 미리 조회해둔 Map에서 AppBlockedService를 바로 꺼내 알림 대상에 추가
                AppBlockedService appBlockedService = blockedServiceMap.get(remainingSub.getAppBlockedServiceId());
                if (appBlockedService != null) {
                    releasedAlertBlockedServices.add(appBlockedService);
                }
            }
        }

        // 7. 변경 사항이 있는 경우에만 저장 및 알림 발송
        if (!domainsToSave.isEmpty()) {
            blockedServiceSubRepository.saveAll(domainsToSave);
            publishServiceAccessAlerts(
                    request.subId(),
                    requesterFamilyId,
                    appliedAlertBlockedServices,
                    releasedAlertBlockedServices
            );
        }

        // 8. 정책 동기화 이벤트 발행 및 응답 (변경 여부와 상관없이 최종 요청된 상태 반영)
        publishAppBlockSnapshotEvent(request.subId(), targetIdsList);

        return AppBlockedServiceMapper.toUpdateAppBlockedServiceResponse(
                requesterFamilyId,
                request.subId(),
                targetIdsList
        );
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

    // 변경된 서비스 도메인 기준으로 차단/해제 알림 outbox 이벤트를 발행한다.
    private void publishServiceAccessAlerts(
            Long subId,
            Long familyId,
            List<AppBlockedService> addedServices,
            List<AppBlockedService> removedServices
    ) {
        for (AppBlockedService service : addedServices) {
            userAlertNotificationOutboxPort.appendServiceAccessAlert(new ServiceAccessAlertOutboxEvent(
                    subId,
                    familyId,
                    service.getName(),
                    AlertAction.APPLIED
            ));
        }
        for (AppBlockedService service : removedServices) {
            userAlertNotificationOutboxPort.appendServiceAccessAlert(new ServiceAccessAlertOutboxEvent(
                    subId,
                    familyId,
                    service.getName(),
                    AlertAction.RELEASED
            ));
        }
    }

    // 요청자 권한과 가족 소유 관계를 검증한다.
    private void validateAuthorityAndFamily(Long subId, Long requesterFamilyId, FamilyRole requesterRole) {
        if (requesterRole != FamilyRole.OWNER) {
            throw new ApplicationException(AuthErrorCode.ACCESS_DENIED);
        }

        FamilySubscription familySub = familySubscriptionRepository.findBySubId(subId)
                .orElseThrow(() -> new ApplicationException(FamilyErrorCode.FAMILY_SUBSCRIPTION_NOT_FOUND));

        if (!familySub.getFamily().getId().equals(requesterFamilyId)) {
            throw new ApplicationException(FamilyErrorCode.NOT_FAMILY_MEMBER);
        }
    }
}
