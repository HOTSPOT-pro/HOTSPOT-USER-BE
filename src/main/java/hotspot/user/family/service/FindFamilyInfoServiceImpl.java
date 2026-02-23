package hotspot.user.family.service;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.FamilyErrorCode;
import hotspot.user.common.exception.code.MemberErrorCode;
import hotspot.user.family.controller.port.FindFamilyInfoService;
import hotspot.user.family.controller.response.FamilyInfoResponse;
import hotspot.user.family.domain.FamilyDetailInfo;
import hotspot.user.family.domain.mapper.FamilyMapper;
import hotspot.user.family.service.port.FamilyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 가족 정보 및 전체 구성원 정보 조회 서비스 구현체
 */

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindFamilyInfoServiceImpl implements FindFamilyInfoService {

    private final FamilyRepository familyRepository;

    @Override
    public FamilyInfoResponse findFamilyInfoById(Long id) {
        // 통합 조회
        FamilyDetailInfo detailInfo = familyRepository.findInfoById(id)
                .orElseThrow(() -> new ApplicationException(FamilyErrorCode.FAMILY_NOT_FOUND));

        return FamilyMapper.toFamilyInfoResponse(detailInfo);
    }
}
