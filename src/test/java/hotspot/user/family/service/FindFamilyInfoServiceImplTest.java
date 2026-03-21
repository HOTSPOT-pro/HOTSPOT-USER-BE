package hotspot.user.family.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.common.crpyto.PhoneDecryptor;
import hotspot.user.common.crpyto.SubscriptionKeyInfo;
import hotspot.user.common.crpyto.SubscriptionKeyLookup;
import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.FamilyErrorCode;
import hotspot.user.family.controller.response.FamilyInfoResponse;
import hotspot.user.family.domain.Family;
import hotspot.user.family.domain.FamilyDetailInfo;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.service.port.FamilyRepository;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.member.domain.Member;
import hotspot.user.member.domain.MemberDetailInfo;
import hotspot.user.member.domain.Status;

@ExtendWith(MockitoExtension.class)
class FindFamilyInfoServiceImplTest {

    @Mock
    private FamilyRepository familyRepository;

    @Mock
    private FamilySubscriptionRepository familySubscriptionRepository;

    @Mock
    private SubscriptionKeyLookup subscriptionKeyLookup;

    @Mock
    private PhoneDecryptor phoneDecryptor;

    @InjectMocks
    private FindFamilyInfoServiceImpl findFamilyInfoService;

    @Test
    @DisplayName("성공: 가족 ID로 가족 및 구성원 전체 정보를 조회한다 (이메일 제외)")
    void findFamilyInfoByIdSuccess() {
        // given
        Long requesterMemberId = 99L;
        Long familyId = 1L;
        Member member = Member.builder().id(10L).name("멤버1").status(Status.APPROVED).build();
        MemberDetailInfo memberInfo = MemberDetailInfo.builder()
                .member(member)
                .email("test@test.com")
                .phone("enc_phone")
                .subId(10L)
                .build();

        FamilyDetailInfo detailInfo = FamilyDetailInfo.builder()
                .familyId(familyId)
                .familyNum(1)
                .memberDetailInfoList(List.of(memberInfo))
                .build();

        SubscriptionKeyInfo keyInfo = new SubscriptionKeyInfo("encryptedDek", "kekKeyId");

        given(familySubscriptionRepository.findByMemberId(requesterMemberId))
                .willReturn(Optional.of(FamilySubscription.builder()
                        .family(Family.builder().id(familyId).build())
                        .familyRole(FamilyRole.OWNER)
                        .build()));
        given(familyRepository.findInfoById(familyId)).willReturn(Optional.of(detailInfo));
        given(subscriptionKeyLookup.findKeyInfosBySubIds(List.of(10L))).willReturn(Map.of(10L, keyInfo));
        given(phoneDecryptor.decrypt(eq("enc_phone"), eq(keyInfo))).willReturn("010-1234-5678");

        // when
        FamilyInfoResponse response = findFamilyInfoService.findFamilyInfoById(requesterMemberId, familyId);

        // then
        assertThat(response.familyId()).isEqualTo(familyId);
        assertThat(response.familyNum()).isEqualTo(1);
        assertThat(response.memberInfoList()).hasSize(1);
        assertThat(response.memberInfoList().get(0).phone()).isEqualTo("010-1234-5678");
        // 이메일 검증은 더 이상 수행하지 않음 (FamilyMemberInfoResponse에는 필드 없음)
    }

    @Test
    @DisplayName("실패: 존재하지 않는 가족 ID로 조회 시 예외가 발생한다")
    void findFamilyInfoByIdFail() {
        // given
        Long requesterMemberId = 99L;
        Long familyId = 999L;
        given(familySubscriptionRepository.findByMemberId(requesterMemberId))
                .willReturn(Optional.of(FamilySubscription.builder()
                        .family(Family.builder().id(familyId).build())
                        .familyRole(FamilyRole.OWNER)
                        .build()));
        given(familyRepository.findInfoById(familyId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> findFamilyInfoService.findFamilyInfoById(requesterMemberId, familyId))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(FamilyErrorCode.FAMILY_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("실패: 요청자가 다른 가족 소속이면 예외가 발생한다")
    void findFamilyInfoByIdFailNotFamilyMember() {
        Long requesterMemberId = 99L;
        Long familyId = 1L;

        given(familySubscriptionRepository.findByMemberId(requesterMemberId))
                .willReturn(Optional.of(FamilySubscription.builder()
                        .family(Family.builder().id(2L).build())
                        .familyRole(FamilyRole.OWNER)
                        .build()));

        assertThatThrownBy(() -> findFamilyInfoService.findFamilyInfoById(requesterMemberId, familyId))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(FamilyErrorCode.NOT_FAMILY_MEMBER.getMessage());
    }
}
