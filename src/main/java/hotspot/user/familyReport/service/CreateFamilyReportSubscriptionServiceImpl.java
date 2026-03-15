package hotspot.user.familyReport.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.AuthErrorCode;
import hotspot.user.family.domain.Family;
import hotspot.user.familyReport.controller.port.CreateFamilyReportSubscriptionService;
import hotspot.user.familyReport.controller.request.CreateFamilyReportSubscriptionRequest;
import hotspot.user.familyReport.domain.FamilyReport;
import hotspot.user.familyReport.service.port.FamilyReportRepository;
import hotspot.user.member.domain.FamilyRole;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CreateFamilyReportSubscriptionServiceImpl implements CreateFamilyReportSubscriptionService {

    private final FamilyReportRepository familyReportRepository;

    @Override
    public void createSubscription(
            Long familyId,
            FamilyRole requesterRole,
            CreateFamilyReportSubscriptionRequest request
    ) {
        if (requesterRole != FamilyRole.OWNER) {
            throw new ApplicationException(AuthErrorCode.ACCESS_DENIED);
        }

        FamilyReport familyReport = familyReportRepository.findByFamilyId(familyId)
                .map(existing -> {
                    existing.updateReceiveDay(request.receiveDay());
                    existing.activate();
                    return existing;
                })
                .orElseGet(() -> FamilyReport.builder()
                        .family(Family.builder().id(familyId).build())
                        .receiveDay(request.receiveDay())
                        .active(true)
                        .build());

        familyReportRepository.save(familyReport);
    }
}
