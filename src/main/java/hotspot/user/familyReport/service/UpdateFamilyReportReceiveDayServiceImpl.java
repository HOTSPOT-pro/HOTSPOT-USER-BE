package hotspot.user.familyReport.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.AuthErrorCode;
import hotspot.user.common.exception.code.FamilyReportErrorCode;
import hotspot.user.familyReport.controller.port.UpdateFamilyReportReceiveDayService;
import hotspot.user.familyReport.controller.request.UpdateFamilyReportReceiveDayRequest;
import hotspot.user.familyReport.domain.FamilyReport;
import hotspot.user.familyReport.service.port.FamilyReportRepository;
import hotspot.user.member.domain.FamilyRole;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateFamilyReportReceiveDayServiceImpl implements UpdateFamilyReportReceiveDayService {

    private final FamilyReportRepository familyReportRepository;

    @Override
    public void updateReceiveDay(
            Long familyId,
            FamilyRole requesterRole,
            UpdateFamilyReportReceiveDayRequest request
    ) {
        if (requesterRole != FamilyRole.OWNER) {
            throw new ApplicationException(AuthErrorCode.ACCESS_DENIED);
        }

        FamilyReport familyReport = familyReportRepository.findByFamilyId(familyId)
                .filter(FamilyReport::isActive)
                .orElseThrow(() -> new ApplicationException(FamilyReportErrorCode.FAMILY_REPORT_SUBSCRIPTION_NOT_FOUND));

        familyReport.updateReceiveDay(request.receiveDay());
        familyReportRepository.save(familyReport);
    }
}
