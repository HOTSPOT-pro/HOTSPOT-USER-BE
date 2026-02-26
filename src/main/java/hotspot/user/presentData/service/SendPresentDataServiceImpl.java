package hotspot.user.presentData.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.AuthErrorCode;
import hotspot.user.common.exception.code.PresentDataErrorCode;
import hotspot.user.family.controller.port.FindFamilySubscriptionService;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.kafka.outbox.NotificationUserAlertOutboxPublisher;
import hotspot.user.presentData.controller.port.SendPresentDataService;
import hotspot.user.presentData.controller.request.SendPresentDataRequest;
import hotspot.user.presentData.controller.response.SendPresentDataResponse;
import hotspot.user.presentData.domain.PresentData;
import hotspot.user.presentData.domain.mapper.SendPresentDataMapper;
import hotspot.user.presentData.service.port.PresentDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 데이터 선물하기 서비스 구현체
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class SendPresentDataServiceImpl implements SendPresentDataService {

    // 선물 데이터 GB => KB 변하기 위해 필요한 상수
    private static final long MIN_PRESENT_AMOUNT_GB = 1L;
    private static final long MAX_PRESENT_AMOUNT_GB = 5L;
    private static final long GB_TO_KB_UNIT = 1048576L;
    private static final String DEFAULT_SENDER_NAME = "사용자";

    private final PresentDataRepository presentDataRepository;
    private final FindFamilySubscriptionService findFamilySubscriptionService;
    private final NotificationUserAlertOutboxPublisher userAlertOutboxPublisher;

    @Override
    @Transactional
    public SendPresentDataResponse sendPresentData(Long memberId, SendPresentDataRequest request) {

        log.info("전달 받은 memberId: {}, 전달 받은 subId: {}", memberId, request.targetSubId());

        // 1. 보내는 사람과 받는 사람의 가족 정보 조회
        // 보내는 사람: memberId 기반 조회
        FamilySubscription providerFamilySub = findFamilySubscriptionService.findByMemberId(memberId);

        // 받는 사람: subId 기반 조회
        FamilySubscription targetFamilySub = findFamilySubscriptionService.findBySubId(request.targetSubId());

        // 3. 선물 가능 단위 및 범위 확인 (1GB ~ 5GB, 1GB 단위)
        validateDataAmount(request.dataAmount());

        // KB 단위로 변환 (1GB = 1,048,576 KB)
        long dataAmountInKb = request.dataAmount() * GB_TO_KB_UNIT;

        // [To-Do] 4. 현재 남은 데이터 양보다 더 많이 보내는지 확인

        // 5. 같은 가족 구성원인지 확인 (가족 정보 기반 검증)
        if (!providerFamilySub.getFamily().getId().equals(targetFamilySub.getFamily().getId())) {
            throw new ApplicationException(AuthErrorCode.ACCESS_DENIED);
        }

        // 6. DB 기록 저장
        PresentData presentData = SendPresentDataMapper.toPresentData(
                providerFamilySub.getSubscription(),
                targetFamilySub.getSubscription(),
                dataAmountInKb
        );
        PresentData sentPresentData = presentDataRepository.sendPresentData(presentData);
        publishPresentDataGiftedEvent(providerFamilySub, sentPresentData, request.dataAmount());

        // 6. [To-Do] Redis 사용량 업데이트
        // 주는 사람: 사용량 증가, 받는 사람: 선물 받은 데이터 양 증가?

        return SendPresentDataMapper.toSendPresentDataResponse(sentPresentData);
    }

    private void publishPresentDataGiftedEvent(
            FamilySubscription providerFamilySub,
            PresentData sentPresentData,
            Long requestAmountGb
    ) {
        String senderName = Optional.ofNullable(providerFamilySub.getSubscription())
                .map(subscription -> subscription.getMember())
                .map(member -> member.getName())
                .orElse(DEFAULT_SENDER_NAME);

        userAlertOutboxPublisher.publishPresentDataGifted(
                sentPresentData.getTargetSubscription().getId(),
                providerFamilySub.getFamily().getId(),
                senderName,
                requestAmountGb + "GB",
                String.valueOf(sentPresentData.getPresentDataId())
        );
    }

    private void validateDataAmount(Long amountGb) {
        if (amountGb == null || amountGb < MIN_PRESENT_AMOUNT_GB || amountGb > MAX_PRESENT_AMOUNT_GB) {
            log.warn("데이터 선물 유효성 검증 실패: 요청량={}GB", amountGb);
            throw new ApplicationException(PresentDataErrorCode.PRESENT_DATA_INVALID_AMOUNT);
        }
    }
}
