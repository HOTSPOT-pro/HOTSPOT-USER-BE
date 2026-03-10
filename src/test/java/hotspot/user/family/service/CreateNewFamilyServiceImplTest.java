package hotspot.user.family.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
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
import hotspot.user.common.crpyto.PhoneHashIndexer;
import hotspot.user.common.crpyto.SubscriptionKeyInfo;
import hotspot.user.common.crpyto.SubscriptionKeyLookup;
import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.FamilyErrorCode;
import hotspot.user.common.exception.code.SubscriptionErrorCode;
import hotspot.user.common.util.s3.S3Util;
import hotspot.user.family.controller.request.CreateNewFamilyRequest;
import hotspot.user.family.controller.request.FamilyMemberRequest;
import hotspot.user.family.controller.response.CreateNewFamilyResponse;
import hotspot.user.family.domain.ApplyType;
import hotspot.user.family.domain.FamilyApply;
import hotspot.user.family.domain.FamilyApplyTarget;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.service.port.FamilyApplyRepository;
import hotspot.user.family.service.port.FamilyApplyTargetRepository;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.member.domain.Member;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.subscription.service.port.SubscriptionRepository;

@ExtendWith(MockitoExtension.class)
class CreateNewFamilyServiceImplTest {

    @InjectMocks
    private CreateNewFamilyServiceImpl createNewFamilyService;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private FamilySubscriptionRepository familySubscriptionRepository;

    @Mock
    private FamilyApplyRepository familyApplyRepository;

    @Mock
    private FamilyApplyTargetRepository familyApplyTargetRepository;

    @Mock
    private PhoneHashIndexer phoneHashIndexer;

    @Mock
    private SubscriptionKeyLookup subscriptionKeyLookup;

    @Mock
    private PhoneDecryptor phoneDecryptor;

    @Mock
    private S3Util s3Util;

    @Test
    @DisplayName("성공: 가족이 없는 사용자가 새로운 가족 생성을 신청하면 성공한다.")
    void createNewFamilySuccess() {
        // given
        Long requesterMemberId = 1L;
        String phone = "01011112222";
        String hash = "HASH";
        String tempKey = "temp-s3-key";
        String certKey = "cert-s3-key";

        given(familySubscriptionRepository.findByMemberId(requesterMemberId)).willReturn(Optional.empty());

        Subscription requesterSub = Subscription.builder()
                .id(100L).phoneEnc("ENC_SELF").phoneHash("SELF_HASH")
                .member(Member.builder().name("방장").build())
                .build();
        given(subscriptionRepository.findByMemberId(requesterMemberId)).willReturn(Optional.of(requesterSub));

        FamilyMemberRequest memberReq = new FamilyMemberRequest("구성원1", phone, FamilyRole.CHILD);
        CreateNewFamilyRequest request = new CreateNewFamilyRequest(ApplyType.CREATE, tempKey, List.of(memberReq));

        Subscription targetSub = Subscription.builder()
                .id(200L).phoneHash(hash).phoneEnc("ENC_TARGET")
                .member(Member.builder().name("구성원1").build())
                .build();

        given(phoneHashIndexer.toHash(phone)).willReturn(hash);
        given(subscriptionRepository.findAllByPhoneHashIn(anyList())).willReturn(List.of(targetSub));
        given(familySubscriptionRepository.findAllBySubIdIn(anyList())).willReturn(List.of());
        given(familyApplyTargetRepository.findAllPendingByTargetSubIdIn(anyList())).willReturn(List.of());

        given(s3Util.moveTempToCertificate(tempKey)).willReturn(certKey);
        given(familyApplyRepository.save(any())).willReturn(FamilyApply.builder().id(1L).build());
        given(familyApplyTargetRepository.saveAll(anyList())).willReturn(List.of(
                FamilyApplyTarget.builder().targetSubId(100L).targetFamilyRole(FamilyRole.OWNER).build(),
                FamilyApplyTarget.builder().targetSubId(200L).targetFamilyRole(FamilyRole.CHILD).build()
        ));

        SubscriptionKeyInfo requesterKeyInfo = new SubscriptionKeyInfo("encryptedDek-100", "kek-100");
        SubscriptionKeyInfo targetKeyInfo = new SubscriptionKeyInfo("encryptedDek-200", "kek-200");
        given(subscriptionKeyLookup.findKeyInfosBySubIds(List.of(100L, 200L)))
                .willReturn(Map.of(100L, requesterKeyInfo, 200L, targetKeyInfo));
        given(phoneDecryptor.decrypt("ENC_SELF", requesterKeyInfo)).willReturn("010-0000-0000");
        given(phoneDecryptor.decrypt("ENC_TARGET", targetKeyInfo)).willReturn("010-1111-2222");

        // when
        CreateNewFamilyResponse response = createNewFamilyService.createNewFamily(requesterMemberId, request);

        // then
        assertThat(response.familyId()).isNull();
        assertThat(response.familyMemberList()).hasSize(2);
        assertThat(response.familyMemberList().get(0).targetFamilyRole()).isEqualTo(FamilyRole.OWNER);
    }

    @Test
    @DisplayName("성공: 가족 생성 시 초대 목록에 본인을 포함하더라도 무시하고 성공한다.")
    void createNewFamilySuccessEvenIfSelfIncluded() {
        // given
        Long requesterMemberId = 1L;
        String selfPhone = "01000000000";
        String selfHash = "SELF_HASH";
        String tempKey = "temp-s3-key";
        String certKey = "cert-s3-key";

        given(familySubscriptionRepository.findByMemberId(requesterMemberId)).willReturn(Optional.empty());

        Subscription requesterSub = Subscription.builder()
                .id(100L).phoneEnc("ENC_SELF").phoneHash(selfHash)
                .member(Member.builder().name("방장").build())
                .build();
        given(subscriptionRepository.findByMemberId(requesterMemberId)).willReturn(Optional.of(requesterSub));

        // 초대 목록에 본인(방장)의 번호를 넣음
        FamilyMemberRequest selfReq = new FamilyMemberRequest("방장", selfPhone, FamilyRole.CHILD);
        CreateNewFamilyRequest request = new CreateNewFamilyRequest(ApplyType.CREATE, tempKey, List.of(selfReq));

        given(phoneHashIndexer.toHash(selfPhone)).willReturn(selfHash);
        given(subscriptionRepository.findAllByPhoneHashIn(anyList())).willReturn(List.of(requesterSub));
        given(familySubscriptionRepository.findAllBySubIdIn(anyList())).willReturn(List.of());
        given(familyApplyTargetRepository.findAllPendingByTargetSubIdIn(anyList())).willReturn(List.of());

        given(s3Util.moveTempToCertificate(tempKey)).willReturn(certKey);
        given(familyApplyRepository.save(any())).willReturn(FamilyApply.builder().id(1L).build());
        // 결과 타겟은 중복 없이 1명(본인 OWNER)만 저장되어야 함
        given(familyApplyTargetRepository.saveAll(anyList())).willReturn(List.of(
                FamilyApplyTarget.builder().targetSubId(100L).targetFamilyRole(FamilyRole.OWNER).build()
        ));

        SubscriptionKeyInfo requesterKeyInfo = new SubscriptionKeyInfo("encryptedDek-100", "kek-100");
        given(subscriptionKeyLookup.findKeyInfosBySubIds(List.of(100L))).willReturn(Map.of(100L, requesterKeyInfo));
        given(phoneDecryptor.decrypt("ENC_SELF", requesterKeyInfo)).willReturn("010-0000-0000");

        // when
        CreateNewFamilyResponse response = createNewFamilyService.createNewFamily(requesterMemberId, request);

        // then
        assertThat(response.familyMemberList()).hasSize(1); // 본인만 포함됨
        assertThat(response.familyMemberList().get(0).targetFamilyRole()).isEqualTo(FamilyRole.OWNER);
    }

    @Test
    @DisplayName("실패: 이미 가족에 소속된 사용자가 신규 생성을 요청하면 예외가 발생한다.")
    void createNewFamilyFailAlreadyInFamily() {
        // given
        Long requesterMemberId = 1L;
        given(familySubscriptionRepository.findByMemberId(requesterMemberId))
                .willReturn(Optional.of(FamilySubscription.builder().build()));

        CreateNewFamilyRequest request = new CreateNewFamilyRequest(ApplyType.CREATE, "temp-s3-key", List.of());

        // when & then
        assertThatThrownBy(() -> createNewFamilyService.createNewFamily(requesterMemberId, request))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(FamilyErrorCode.TARGET_ALREADY_IN_FAMILY.getMessage());
    }

    @Test
    @DisplayName("실패: 신청 타입이 CREATE가 아니면 예외가 발생한다.")
    void createNewFamilyFailInvalidType() {
        // given
        Long requesterMemberId = 1L;
        given(familySubscriptionRepository.findByMemberId(requesterMemberId)).willReturn(Optional.empty());

        CreateNewFamilyRequest request = new CreateNewFamilyRequest(ApplyType.ADD, "temp-s3-key", List.of());

        // when & then
        assertThatThrownBy(() -> createNewFamilyService.createNewFamily(requesterMemberId, request))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(FamilyErrorCode.INVALID_APPLY_TYPE.getMessage());
    }

    @Test
    @DisplayName("실패: 피신청자 정보를 찾을 수 없으면 예외가 발생한다.")
    void createNewFamilyFailSubscriptionNotFound() {
        // given
        Long requesterMemberId = 1L;
        given(familySubscriptionRepository.findByMemberId(requesterMemberId)).willReturn(Optional.empty());
        given(subscriptionRepository.findByMemberId(requesterMemberId))
                .willReturn(Optional.of(Subscription.builder().id(100L).build()));

        FamilyMemberRequest memberReq = new FamilyMemberRequest("유령", "01012345678", FamilyRole.CHILD);
        CreateNewFamilyRequest request = new CreateNewFamilyRequest(ApplyType.CREATE,
                "temp-s3-key", List.of(memberReq));

        given(phoneHashIndexer.toHash(any())).willReturn("HASH");
        given(subscriptionRepository.findAllByPhoneHashIn(anyList())).willReturn(List.of()); // 아무도 못찾음

        // when & then
        assertThatThrownBy(() -> createNewFamilyService.createNewFamily(requesterMemberId, request))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(SubscriptionErrorCode.SUBSCRIPTION_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("실패: 피신청자에게 OWNER 역할을 부여하려 하면 예외가 발생한다.")
    void createNewFamilyFailAssignOwner() {
        // given
        Long requesterMemberId = 1L;
        given(familySubscriptionRepository.findByMemberId(requesterMemberId)).willReturn(Optional.empty());
        Subscription requesterSub = Subscription.builder().id(100L).phoneHash("SELF").build();
        given(subscriptionRepository.findByMemberId(requesterMemberId)).willReturn(Optional.of(requesterSub));

        FamilyMemberRequest invalidReq = new FamilyMemberRequest("타인", "01011112222", FamilyRole.OWNER);
        CreateNewFamilyRequest request = new CreateNewFamilyRequest(ApplyType.CREATE,
                "temp-s3-key", List.of(invalidReq));

        Subscription targetSub = Subscription.builder().id(200L).phoneHash("HASH").build();
        given(subscriptionRepository.findAllByPhoneHashIn(anyList())).willReturn(List.of(targetSub));
        given(phoneHashIndexer.toHash(any())).willReturn("HASH");
        given(familySubscriptionRepository.findAllBySubIdIn(anyList())).willReturn(List.of());
        given(familyApplyTargetRepository.findAllPendingByTargetSubIdIn(anyList())).willReturn(List.of());

        // when & then
        assertThatThrownBy(() -> createNewFamilyService.createNewFamily(requesterMemberId, request))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(FamilyErrorCode.CANNOT_ASSIGN_OWNER_ROLE.getMessage());
    }
}
