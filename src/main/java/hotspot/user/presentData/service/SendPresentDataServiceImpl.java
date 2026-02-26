package hotspot.user.presentData.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.AuthErrorCode;
import hotspot.user.common.exception.code.MemberErrorCode;
import hotspot.user.common.exception.code.PresentDataErrorCode;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
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
    private final PresentDataRepository presentDataRepository;
    private final FamilySubscriptionRepository familySubscriptionRepository;

    @Override
    @Transactional
    public SendPresentDataResponse sendPresentData(Long memberId, SendPresentDataRequest request) {

        log.info("전달 받은 memberId: {}, 전달 받은 subId: {}", memberId, request.targetSubId());

        // 1. 보내는 사람과 받는 사람의 가족 정보 조회
        // 보내는 사람: memberId 기반 조회
        FamilySubscription providerFamilySub = familySubscriptionRepository.findByMemberId(memberId)
                .orElseThrow(() -> new ApplicationException(MemberErrorCode.FAMILY_SUBSCRIPTION_NOT_FOUND));

        // 받는 사람: subId 기반 조회
        FamilySubscription targetFamilySub = familySubscriptionRepository.findBySubId(request.targetSubId())
                .orElseThrow(() -> new ApplicationException(MemberErrorCode.FAMILY_SUBSCRIPTION_NOT_FOUND));

        // 2. 같은 가족 구성원인지 확인
        if (!providerFamilySub.getFamily().getId().equals(targetFamilySub.getFamily().getId())) {
            throw new ApplicationException(AuthErrorCode.ACCESS_DENIED);
        }

        // 3. 자신에게 선물하는지 확인
        if (providerFamilySub.getSubscription().getId().equals(targetFamilySub.getSubscription().getId())) {
            throw new ApplicationException(PresentDataErrorCode.PRESENT_DATA_SELF_GIFT);
        }

        // 4. 선물 가능 단위 및 범위 확인 (1GB ~ 5GB, 1GB 단위)
        validateDataAmount(request.dataAmount());

        // [To-Do] 5. 현재 남은 데이터 양보다 더 많이 보내는지 확인

        // 6. DB 기록 저장
        PresentData presentData = SendPresentDataMapper.toPresentData(
                providerFamilySub.getSubscription(),
                targetFamilySub.getSubscription(),
                request.dataAmount()
        );
        PresentData sentPresentData = presentDataRepository.sendPresentData(presentData);

        // 6. [To-Do] Redis 사용량 업데이트
        // 주는 사람: 사용량 증가, 받는 사람: 선물 받은 데이터 양 증가?

        return SendPresentDataMapper.toSendPresentDataResponse(sentPresentData);
    }

    private void validateDataAmount(Long amount) {
        long oneGbInKb = 1048576L; // 1GB in KB
        long minAmount = oneGbInKb;
        long maxAmount = oneGbInKb * 5;

        if (amount == null || amount < minAmount || amount > maxAmount || amount % oneGbInKb != 0) {
            log.warn("데이터 선물 유효성 검증 실패: 요청량={}KB", amount);
            throw new ApplicationException(PresentDataErrorCode.PRESENT_DATA_INVALID_AMOUNT);
        }
    }
}
