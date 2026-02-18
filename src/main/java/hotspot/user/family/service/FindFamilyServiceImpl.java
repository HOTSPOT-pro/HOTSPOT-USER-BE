package hotspot.user.family.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.FamilyErrorCode;
import hotspot.user.family.controller.port.FindFamilyService;
import hotspot.user.family.controller.response.FamilyResponse;
import hotspot.user.family.domain.mapper.FamilyMapper;
import hotspot.user.family.service.port.FamilyRepository;
import lombok.RequiredArgsConstructor;

/**
 * 가족 정보 조회 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindFamilyServiceImpl implements FindFamilyService {

    private final FamilyRepository familyRepository;

    @Override
    public FamilyResponse findById(Long id) {
        return familyRepository.findById(id)
                .map(FamilyMapper::toFamilyResponse)
                .orElseThrow(() -> new ApplicationException(FamilyErrorCode.FAMILY_NOT_FOUND));
    }
}
