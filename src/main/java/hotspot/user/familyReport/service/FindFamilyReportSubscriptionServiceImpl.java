package hotspot.user.familyReport.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.familyReport.controller.port.FindFamilyReportSubscriptionService;
import hotspot.user.familyReport.controller.response.FamilyReportSubscriptionResponse;
import hotspot.user.familyReport.service.port.FamilyReportRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindFamilyReportSubscriptionServiceImpl implements FindFamilyReportSubscriptionService {

    private final FamilyReportRepository familyReportRepository;

    @Override
    public FamilyReportSubscriptionResponse findSubscription(Long familyId) {
        return familyReportRepository.findByFamilyId(familyId)
                .filter(familyReport -> familyReport.isActive())
                .map(familyReport -> FamilyReportSubscriptionResponse.builder()
                        .subscribed(true)
                        .build())
                .orElseGet(FamilyReportSubscriptionResponse::unsubscribed);
    }
}
