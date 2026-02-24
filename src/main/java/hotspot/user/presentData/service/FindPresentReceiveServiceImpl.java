package hotspot.user.presentData.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.presentData.controller.port.FindPresentProvideService;
import hotspot.user.presentData.controller.port.FindPresentReceiveService;
import hotspot.user.presentData.controller.response.PresentDataResponse;
import hotspot.user.presentData.domain.PresentData;
import hotspot.user.presentData.domain.mapper.PresentDataMapper;
import hotspot.user.presentData.service.port.PresentDataRepository;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindPresentReceiveServiceImpl implements FindPresentReceiveService, FindPresentProvideService {

    private final SubscriptionService subscriptionService;
    private final PresentDataRepository presentDataRepository;

    @Override
    public PresentDataResponse findPresentReceive(Long memberId) {

        Subscription subscription =
                subscriptionService.findByMemberId(memberId);

        List<PresentData> presentDataList =
                presentDataRepository.findPresentReceive(subscription.getId());

        return PresentDataMapper.toPresentDataReceiveResponse(presentDataList);
    }

    @Override
    public PresentDataResponse findPresentProvide(Long memberId) {
        Subscription subscription =
                subscriptionService.findByMemberId(memberId);

        List<PresentData> presentDataList =
                presentDataRepository.findPresentProvide(subscription.getId());

        return PresentDataMapper.toPresentDataProvideResponse(presentDataList);
    }
}
