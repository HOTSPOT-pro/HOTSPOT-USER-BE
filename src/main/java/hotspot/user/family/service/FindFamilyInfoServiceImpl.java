package hotspot.user.family.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hotspot.user.common.crpyto.PhoneDecryptor;
import hotspot.user.common.crpyto.SubscriptionKeyInfo;
import hotspot.user.common.crpyto.SubscriptionKeyLookup;
import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.FamilyErrorCode;
import hotspot.user.family.controller.port.FindFamilyInfoService;
import hotspot.user.family.controller.response.FamilyInfoResponse;
import hotspot.user.family.controller.response.FamilyMemberInfoResponse;
import hotspot.user.family.domain.FamilyDetailInfo;
import hotspot.user.family.domain.mapper.FamilyMapper;
import hotspot.user.family.service.port.FamilyRepository;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.member.domain.MemberDetailInfo;
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
    private final FamilySubscriptionRepository familySubscriptionRepository;
    private final SubscriptionKeyLookup subscriptionKeyLookup;
    private final PhoneDecryptor phoneDecryptor;

    @Override
    public FamilyInfoResponse findFamilyInfoById(Long requesterMemberId, Long id) {
        validateRequesterMembership(requesterMemberId, id);

        // 통합 조회
        FamilyDetailInfo detailInfo = familyRepository.findInfoById(id)
                .orElseThrow(() -> new ApplicationException(FamilyErrorCode.FAMILY_NOT_FOUND));

        Map<Long, SubscriptionKeyInfo> keyInfoBySubId = subscriptionKeyLookup.findKeyInfosBySubIds(
                detailInfo.getMemberDetailInfoList().stream()
                        .map(MemberDetailInfo::getSubId)
                        .toList()
        );

        // 서비스 계층에서 리스트 내 각 멤버의 정보를 복호화하여 매핑
        List<FamilyMemberInfoResponse> memberInfoList = detailInfo.getMemberDetailInfoList().stream()
                .map(info -> {
                    String decryptedPhone = phoneDecryptor.decrypt(
                            info.getPhone(),
                            keyInfoBySubId.get(info.getSubId())
                    );
                    return FamilyMemberInfoMapper.toFamilyMemberInfoResponse(info, decryptedPhone);
                })
                .toList();

        return FamilyMapper.toFamilyInfoResponse(detailInfo, memberInfoList);
    }

    private void validateRequesterMembership(Long requesterMemberId, Long familyId) {
        Long requesterFamilyId = familySubscriptionRepository.findByMemberId(requesterMemberId)
                .orElseThrow(() -> new ApplicationException(FamilyErrorCode.FAMILY_SUBSCRIPTION_NOT_FOUND))
                .getFamily()
                .getId();

        if (!requesterFamilyId.equals(familyId)) {
            throw new ApplicationException(FamilyErrorCode.NOT_FAMILY_MEMBER);
        }
    }
}
