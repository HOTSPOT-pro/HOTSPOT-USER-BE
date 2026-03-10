package hotspot.user.family.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.common.crpyto.PhoneDecryptor;
import hotspot.user.common.crpyto.PhoneHashIndexer;
import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.FamilyErrorCode;
import hotspot.user.common.exception.code.SubscriptionErrorCode;
import hotspot.user.common.util.s3.S3Util;
import hotspot.user.family.controller.request.AddFamilyMemberRequest;
import hotspot.user.family.controller.request.FamilyMemberRequest;
import hotspot.user.family.controller.response.AddFamilyMemberResponse;
import hotspot.user.family.domain.ApplyType;
import hotspot.user.family.domain.Family;
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
class AddFamilyMemberServiceImplTest {

    @InjectMocks
    private AddFamilyMemberServiceImpl addFamilyMemberService;

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
    private PhoneDecryptor phoneDecryptor;

    @Mock
    private S3Util s3Util;

    @Test
    @DisplayName("성공: 가족 OWNER가 새로운 구성원들을 추가 신청한다.")
    void addFamilyMemberSuccess() {
        // given
        Long requesterMemberId = 1L;
        Long familyId = 10L;
        String phone = "01011112222";
        String hash = "HASH";
        String tempKey = "temp-s3-key";
        String certKey = "cert-s3-key";

        FamilySubscription requesterFs = createRequesterFs(familyId, 100L, FamilyRole.OWNER);
        given(familySubscriptionRepository.findByMemberId(requesterMemberId)).willReturn(Optional.of(requesterFs));

        FamilyMemberRequest memberReq = new FamilyMemberRequest("홍길동", phone, FamilyRole.CHILD);
        AddFamilyMemberRequest request = new AddFamilyMemberRequest(ApplyType.ADD, tempKey, List.of(memberReq));

        Subscription targetSub = Subscription.builder()
                .id(200L).phoneHash(hash).phoneEnc("ENC")
                .member(Member.builder().name("홍길동").build()).build();

        given(phoneHashIndexer.toHash(phone)).willReturn(hash);
        given(subscriptionRepository.findAllByPhoneHashIn(anyList())).willReturn(List.of(targetSub));
        given(familySubscriptionRepository.findAllBySubIdIn(anyList())).willReturn(List.of());
        given(familyApplyTargetRepository.findAllPendingByTargetSubIdIn(anyList())).willReturn(List.of());

        given(s3Util.moveTempToCertificate(tempKey)).willReturn(certKey);
        given(familyApplyRepository.save(any())).willReturn(FamilyApply.builder().id(1L).build());
        given(familyApplyTargetRepository.saveAll(anyList())).willReturn(List.of(
                FamilyApplyTarget.builder().targetSubId(200L).targetFamilyRole(FamilyRole.CHILD).build()
        ));
        given(phoneDecryptor.decrypt("ENC", 200L)).willReturn("010-1111-2222");

        // when
        AddFamilyMemberResponse response = addFamilyMemberService.addFamilyMember(requesterMemberId, familyId, request);

        // then
        assertThat(response.familyMemberList()).hasSize(1);
        assertThat(response.familyMemberList().get(0).phone()).isEqualTo("010-1111-2222");
    }

    @Test
    @DisplayName("실패: 요청자가 해당 가족의 OWNER가 아니면 예외가 발생한다.")
    void addFamilyMemberFailNotOwner() {
        // given
        Long requesterMemberId = 1L;
        Long familyId = 10L;
        FamilySubscription requesterFs = createRequesterFs(familyId, 100L, FamilyRole.CHILD); // OWNER 아님
        given(familySubscriptionRepository.findByMemberId(requesterMemberId)).willReturn(Optional.of(requesterFs));

        AddFamilyMemberRequest request = new AddFamilyMemberRequest(ApplyType.ADD, "url", List.of());

        // when & then
        assertThatThrownBy(() -> addFamilyMemberService.addFamilyMember(requesterMemberId, familyId, request))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(FamilyErrorCode.ONLY_OWNER_CAN_MANAGE.getMessage());
    }

    @Test
    @DisplayName("실패: 신청 타입이 ADD가 아니면 예외가 발생한다.")
    void addFamilyMemberFailInvalidType() {
        // given
        Long requesterMemberId = 1L;
        Long familyId = 10L;
        given(familySubscriptionRepository.findByMemberId(requesterMemberId))
                .willReturn(Optional.of(createRequesterFs(familyId, 100L, FamilyRole.OWNER)));

        AddFamilyMemberRequest request = new AddFamilyMemberRequest(ApplyType.REMOVE, "url", List.of());

        // when & then
        assertThatThrownBy(() -> addFamilyMemberService.addFamilyMember(requesterMemberId, familyId, request))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(FamilyErrorCode.INVALID_APPLY_TYPE.getMessage());
    }

    @Test
    @DisplayName("실패: 피신청자에게 OWNER 역할을 부여하려 하면 예외가 발생한다.")
    void addFamilyMemberFailAssignOwner() {
        // given
        Long requesterMemberId = 1L;
        Long familyId = 10L;
        given(familySubscriptionRepository.findByMemberId(requesterMemberId))
                .willReturn(Optional.of(createRequesterFs(familyId, 100L, FamilyRole.OWNER)));

        String phone = "01012345678";
        String hash = "HASH";
        FamilyMemberRequest invalidReq = new FamilyMemberRequest("타겟", phone, FamilyRole.OWNER); // 타인에게 OWNER 부여
        AddFamilyMemberRequest request = new AddFamilyMemberRequest(ApplyType.ADD, "url", List.of(invalidReq));

        Subscription targetSub = Subscription.builder().id(200L).phoneHash(hash).build();
        given(phoneHashIndexer.toHash(phone)).willReturn(hash);
        given(subscriptionRepository.findAllByPhoneHashIn(anyList())).willReturn(List.of(targetSub));
        given(familySubscriptionRepository.findAllBySubIdIn(anyList())).willReturn(List.of());
        given(familyApplyTargetRepository.findAllPendingByTargetSubIdIn(anyList())).willReturn(List.of());

        // when & then
        assertThatThrownBy(() -> addFamilyMemberService.addFamilyMember(requesterMemberId, familyId, request))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(FamilyErrorCode.CANNOT_ASSIGN_OWNER_ROLE.getMessage());
    }

    @Test
    @DisplayName("실패: 이미 다른 가족에 속한 대상을 추가하려 하면 예외가 발생한다.")
    void addFamilyMemberFailAlreadyInFamily() {
        // given
        Long requesterMemberId = 1L;
        Long familyId = 10L;
        given(familySubscriptionRepository.findByMemberId(requesterMemberId))
                .willReturn(Optional.of(createRequesterFs(familyId, 100L, FamilyRole.OWNER)));

        String phone = "01012345678";
        String hash = "HASH";
        FamilyMemberRequest memberReq = new FamilyMemberRequest("타겟", phone, FamilyRole.CHILD);
        AddFamilyMemberRequest request = new AddFamilyMemberRequest(ApplyType.ADD, "url", List.of(memberReq));

        Subscription targetSub = Subscription.builder().id(200L).phoneHash(hash).build();
        given(phoneHashIndexer.toHash(phone)).willReturn(hash);
        given(subscriptionRepository.findAllByPhoneHashIn(anyList())).willReturn(List.of(targetSub));

        // 이미 가족에 소속됨 모킹
        given(familySubscriptionRepository.findAllBySubIdIn(anyList()))
                .willReturn(List.of(FamilySubscription.builder().subscription(targetSub).build()));

        // when & then
        assertThatThrownBy(() -> addFamilyMemberService.addFamilyMember(requesterMemberId, familyId, request))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(FamilyErrorCode.TARGET_ALREADY_IN_FAMILY.getMessage());
    }

    @Test
    @DisplayName("실패: 이미 처리 대기 중인 신청이 있는 대상을 추가하면 예외가 발생한다.")
    void addFamilyMemberFailDuplicateApply() {
        // given
        Long requesterMemberId = 1L;
        Long familyId = 10L;
        given(familySubscriptionRepository.findByMemberId(requesterMemberId))
                .willReturn(Optional.of(createRequesterFs(familyId, 100L, FamilyRole.OWNER)));

        String phone = "01012345678";
        String hash = "HASH";
        FamilyMemberRequest memberReq = new FamilyMemberRequest("타겟", phone, FamilyRole.CHILD);
        AddFamilyMemberRequest request = new AddFamilyMemberRequest(ApplyType.ADD, "url", List.of(memberReq));

        Subscription targetSub = Subscription.builder().id(200L).phoneHash(hash).build();
        given(phoneHashIndexer.toHash(phone)).willReturn(hash);
        given(subscriptionRepository.findAllByPhoneHashIn(anyList())).willReturn(List.of(targetSub));
        given(familySubscriptionRepository.findAllBySubIdIn(anyList())).willReturn(List.of());

        // 이미 대기 중인 신청 리스트에 포함
        given(familyApplyTargetRepository.findAllPendingByTargetSubIdIn(anyList()))
                .willReturn(List.of(FamilyApplyTarget.builder().targetSubId(200L).build()));

        // when & then
        assertThatThrownBy(() -> addFamilyMemberService.addFamilyMember(requesterMemberId, familyId, request))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(FamilyErrorCode.DUPLICATE_FAMILY_APPLY.getMessage());
    }

    @Test
    @DisplayName("실패: 피신청자 중 일부 회선 정보를 찾을 수 없으면 예외가 발생한다.")
    void addFamilyMemberFailSubscriptionNotFound() {
        // given
        Long requesterMemberId = 1L;
        Long familyId = 10L;
        given(familySubscriptionRepository.findByMemberId(requesterMemberId))
                .willReturn(Optional.of(createRequesterFs(familyId, 100L, FamilyRole.OWNER)));

        FamilyMemberRequest memberReq = new FamilyMemberRequest("모르는사람", "01012345678", FamilyRole.CHILD);
        AddFamilyMemberRequest request = new AddFamilyMemberRequest(ApplyType.ADD, "url", List.of(memberReq));

        given(phoneHashIndexer.toHash(anyString())).willReturn("HASH");
        given(subscriptionRepository.findAllByPhoneHashIn(anyList())).willReturn(List.of()); // 아무도 못찾음

        // when & then
        assertThatThrownBy(() -> addFamilyMemberService.addFamilyMember(requesterMemberId, familyId, request))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(SubscriptionErrorCode.SUBSCRIPTION_NOT_FOUND.getMessage());
    }

    private FamilySubscription createRequesterFs(Long familyId, Long subId, FamilyRole role) {
        return FamilySubscription.builder()
                .family(Family.builder().id(familyId).build())
                .subscription(Subscription.builder().id(subId).build())
                .familyRole(role)
                .build();
    }
}
