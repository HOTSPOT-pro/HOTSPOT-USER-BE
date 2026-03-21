package hotspot.user.usage.reportUsage.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.usage.reportUsage.controller.port.FindReportFamilyService;
import hotspot.user.usage.reportUsage.controller.response.ReportFamilyResponse;
import hotspot.user.usage.reportUsage.domain.mapper.ReportFamilyMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FindReportServiceImpl implements FindReportFamilyService {

    private final FamilySubscriptionRepository familySubscriptionRepository;

    // 가족 구성원 subId, subName 목록 조회 Method
    @Transactional(readOnly = true)
    @Override
    public List<ReportFamilyResponse> findReportFamily(Long familyId) {
        return familySubscriptionRepository.findByFamilyId(familyId)
                .stream()
                .map(ReportFamilyMapper::toReportFamilyResponse)
                .toList();
    }
}
