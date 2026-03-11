package hotspot.user.presentData.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.AuthErrorCode;
import hotspot.user.common.exception.code.PresentDataErrorCode;
import hotspot.user.family.controller.port.FindFamilySubscriptionService;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.outbox.consistencyOutbox.domain.event.subscription.gift.GiftReceivedEvent;
import hotspot.user.outbox.notificationOutbox.domain.event.PresentDataGiftedOutboxEvent;
import hotspot.user.outbox.notificationOutbox.service.port.UserAlertNotificationOutboxPort;
import hotspot.user.plan.domain.DataPeriod;
import hotspot.user.presentData.controller.port.SendPresentDataService;
import hotspot.user.presentData.controller.request.SendPresentDataRequest;
import hotspot.user.presentData.controller.response.SendPresentDataResponse;
import hotspot.user.presentData.domain.PresentData;
import hotspot.user.presentData.domain.mapper.SendPresentDataMapper;
import hotspot.user.presentData.service.port.PresentDataRepository;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.usage.subscriptionUsage.service.port.SubscriptionUsageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendPresentDataServiceImpl implements SendPresentDataService {

    private static final long MIN_PRESENT_AMOUNT_GB = 1L;
    private static final long MAX_PRESENT_AMOUNT_GB = 5L;
    private static final long GB_TO_KB_UNIT = 1_048_576L;
    private static final String DEFAULT_SENDER_NAME = "사용자";

    private final PresentDataRepository presentDataRepository;
    private final FindFamilySubscriptionService findFamilySubscriptionService;
    private final SubscriptionUsageRepository subscriptionUsageRepository;

    private final ApplicationEventPublisher eventPublisher;

    private final Clock clock;
    private final UserAlertNotificationOutboxPort userAlertNotificationOutboxPort;

    @Override
    @Transactional
    public SendPresentDataResponse sendPresentData(Long memberId, SendPresentDataRequest request) {

        long giftKb = toKb(request.dataAmount());

        // 1) 입력 검증 (단위/범위)
        validateGiftAmountGb(request.dataAmount());

        // 2) 도메인 조회
        FamilySubscription giver = findFamilySubscriptionService.findByMemberId(memberId);
        FamilySubscription receiver = findFamilySubscriptionService.findBySubId(request.targetSubId());

        // 3) 권한/관계 검증 (같은 가족인지)
        validateSameFamily(giver, receiver);

        // 4) 정책 검증
        validateEnoughPlanRemaining(giver.getSubscription(), giftKb);
        validateMonthlyGiftLimit(giver.getSubscription().getId(), giftKb);

        // 5) 저장
        PresentData saved = savePresentData(giver, receiver, giftKb);

        // 6) 이벤트 발행 (Outbox로 흘러가게)
        publishGiftReceivedEvent(giver, receiver, saved, giftKb);
        publishPresentDataGiftedEvent(giver, saved, request.dataAmount());

        return SendPresentDataMapper.toSendPresentDataResponse(saved);
    }

    private long toKb(Long amountGb) {
        if (amountGb == null) {
            return 0L;
        }
        return amountGb * GB_TO_KB_UNIT;
    }

    private void validateGiftAmountGb(Long amountGb) {
        if (amountGb == null || amountGb < MIN_PRESENT_AMOUNT_GB || amountGb > MAX_PRESENT_AMOUNT_GB) {
            log.warn("데이터 선물 유효성 검증 실패: 요청량={}GB", amountGb);
            throw new ApplicationException(PresentDataErrorCode.PRESENT_DATA_INVALID_AMOUNT);
        }
    }

    private void validateSameFamily(FamilySubscription giver, FamilySubscription receiver) {
        if (!giver.getFamily().getId().equals(receiver.getFamily().getId())) {
            throw new ApplicationException(AuthErrorCode.ACCESS_DENIED);
        }
    }

    private void validateEnoughPlanRemaining(Subscription giverSubscription, long giftKb) {

        DataPeriod period = giverSubscription.getPlan().getDataPeriod();

        long dataAmount = giverSubscription.getPlan().getDataAmount();

        // 무제한 용금제일 경우에는 무조건 선물 가능하도록 예외 처리 로직 추가
        if(dataAmount <= -1) return;

        long remainingKb =
                subscriptionUsageRepository.findRemainingPlanKb(giverSubscription.getId(), period);

        if (remainingKb < giftKb) {
            throw new ApplicationException(PresentDataErrorCode.NOT_ENOUGH_DATA);
        }
    }

    private void validateMonthlyGiftLimit(Long giverSubId, long giftKb) {

        YearMonth nowYm = YearMonth.now(clock);

        LocalDateTime start = nowYm.atDay(1).atStartOfDay();
        LocalDateTime end = nowYm.plusMonths(1).atDay(1).atStartOfDay();

        long sentThisMonthKb =
                presentDataRepository.sumMonthlySentKb(giverSubId, start, end);

        long maxMonthlyGiftKb = MAX_PRESENT_AMOUNT_GB * GB_TO_KB_UNIT;

        if (sentThisMonthKb + giftKb > maxMonthlyGiftKb) {
            throw new ApplicationException(PresentDataErrorCode.MONTHLY_GIFT_LIMIT_EXCEEDED);
        }
    }

    private PresentData savePresentData(
            FamilySubscription giver,
            FamilySubscription receiver,
            long giftKb
    ) {
        PresentData presentData = SendPresentDataMapper.toPresentData(
                giver.getSubscription(),
                receiver.getSubscription(),
                giftKb
        );
        return presentDataRepository.sendPresentData(presentData);
    }

    private void publishGiftReceivedEvent(
            FamilySubscription giver,
            FamilySubscription receiver,
            PresentData saved,
            long giftKb
    ) {

        String yyyyMM = YearMonth.now(clock).toString().replace("-", "");
        String yyyyMMdd = LocalDate.now(clock)
                .format(DateTimeFormatter.BASIC_ISO_DATE);

        eventPublisher.publishEvent(
                new GiftReceivedEvent(
                        "GIFT_RECEIVED",
                        receiver.getSubscription().getId(),
                        giver.getSubscription().getId(),
                        saved.getPresentDataId(),
                        giftKb,   // giftLimitBytes
                        giftKb,   // giftAmountBytes
                        yyyyMM,
                        yyyyMMdd,
                        UUID.randomUUID().toString()
                )
        );
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

        userAlertNotificationOutboxPort.appendPresentDataGiftedAlert(new PresentDataGiftedOutboxEvent(
                sentPresentData.getTargetSubscription().getId(),
                providerFamilySub.getFamily().getId(),
                senderName,
                requestAmountGb + "GB",
                String.valueOf(sentPresentData.getPresentDataId())
        ));
    }
}
