package hotspot.user.family.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.common.crpyto.PhoneDecryptor;
import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.FamilyErrorCode;
import hotspot.user.family.controller.port.FindFamilyInfoService;
import hotspot.user.family.controller.response.FamilyInfoResponse;
import hotspot.user.family.controller.response.FamilyMemberInfoResponse;
import hotspot.user.family.domain.FamilyDetailInfo;
import hotspot.user.family.domain.mapper.FamilyMapper;
import hotspot.user.family.service.port.FamilyRepository;
import hotspot.user.member.domain.mapper.FamilyMemberInfoMapper;
import lombok.RequiredArgsConstructor;

/**
 * 가족 정보 및 전체 구성원 정보 조회 서비스 구현체
 */

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindFamilyInfoServiceImpl implements FindFamilyInfoService {

    private final FamilyRepository familyRepository;
    private final PhoneDecryptor phoneDecryptor;

    @Override
    public FamilyInfoResponse findFamilyInfoById(Long id) {
        // 통합 조회
        FamilyDetailInfo detailInfo = familyRepository.findInfoById(id)
                .orElseThrow(() -> new ApplicationException(FamilyErrorCode.FAMILY_NOT_FOUND));

        // 서비스 계층에서 리스트 내 각 멤버의 정보를 복호화하여 매핑
        List<FamilyMemberInfoResponse> memberInfoList = detailInfo.getMemberDetailInfoList().stream()
                .map(info -> {
                    String decryptedPhone = phoneDecryptor.decrypt(info.getPhone());
                    return FamilyMemberInfoMapper.toFamilyMemberInfoResponse(info, decryptedPhone);
                })
                .toList();

        return FamilyMapper.toFamilyInfoResponse(detailInfo, memberInfoList);
    }
}
