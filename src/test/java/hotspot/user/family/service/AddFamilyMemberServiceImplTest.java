package hotspot.user.family.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

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

    @Test
    @DisplayName("가족 OWNER가 새로운 구성원 추가 신청을 하면 성공한다.")
    void addFamilyMember_success() {
        // given
        Long requesterMemberId = 1L;
        Long familyId = 10L;
        String phone1 = "01011112222";
        String hash1 = "HASH1";

        // 요청자 정보 (OWNER)
        Family family = Family.builder().id(familyId).build();
        Subscription requesterSub = Subscription.builder().id(100L).build();
        FamilySubscription requesterFs = FamilySubscription.builder()
                .family(family)
                .subscription(requesterSub)
                .familyRole(FamilyRole.OWNER)
                .build();

        // 피신청자 정보
        FamilyMemberRequest memberReq = new FamilyMemberRequest("홍길동", phone1, FamilyRole.CHILD);
        AddFamilyMemberRequest request = new AddFamilyMemberRequest(ApplyType.ADD, "doc-url", List.of(memberReq));

        Subscription targetSub = Subscription.builder()
                .id(200L)
                .phoneHash(hash1)
                .phoneEnc("ENC1")
                .member(Member.builder().name("홍길동").build())
                .build();

        given(familySubscriptionRepository.findByMemberId(requesterMemberId)).willReturn(Optional.of(requesterFs));
        given(phoneHashIndexer.toHash(phone1)).willReturn(hash1);
        given(subscriptionRepository.findAllByPhoneHashIn(anyList())).willReturn(List.of(targetSub));
        given(familySubscriptionRepository.findAllBySubIdIn(anyList())).willReturn(List.of()); // 아직 가족 아님
        given(familyApplyTargetRepository.findAllPendingByTargetSubIdIn(anyList())).willReturn(List.of()); // 대기중인 신청 없음

        FamilyApply savedApply = FamilyApply.builder().id(1L).build();
        given(familyApplyRepository.save(any())).willReturn(savedApply);
        given(familyApplyTargetRepository.saveAll(anyList())).willReturn(List.of(
                FamilyApplyTarget.builder().id(101L).targetSubId(200L).targetFamilyRole(FamilyRole.CHILD).build()
        ));
        given(phoneDecryptor.decrypt("ENC1")).willReturn("010-1111-2222");

        // when
        AddFamilyMemberResponse response = addFamilyMemberService.addFamilyMember(requesterMemberId, familyId, request);

        // then
        assertThat(response.familyId()).isEqualTo(familyId);
        assertThat(response.familyMemberList()).hasSize(1);
        assertThat(response.familyMemberList().get(0).name()).isEqualTo("홍길동");
        assertThat(response.familyMemberList().get(0).phone()).isEqualTo("010-1111-2222");
    }

    @Test
    @DisplayName("가족 OWNER가 아닌 사람이 신청하면 예외가 발생한다.")
    void addFamilyMember_fail_not_owner() {
        // given
        Long requesterMemberId = 1L;
        Long familyId = 10L;
        FamilySubscription requesterFs = FamilySubscription.builder()
                .family(Family.builder().id(familyId).build())
                .familyRole(FamilyRole.CHILD) // OWNER 아님
                .build();

        given(familySubscriptionRepository.findByMemberId(requesterMemberId)).willReturn(Optional.of(requesterFs));

        AddFamilyMemberRequest request = new AddFamilyMemberRequest(ApplyType.ADD, "url", List.of());

        // when & then
        assertThatThrownBy(() -> addFamilyMemberService.addFamilyMember(requesterMemberId, familyId, request))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(FamilyErrorCode.ONLY_OWNER_CAN_MANAGE.getMessage());
    }

    @Test
    @DisplayName("자기 자신을 추가 리스트에 넣으면 예외가 발생한다.")
    void addFamilyMember_fail_add_self() {
        // given
        Long requesterMemberId = 1L;
        Long familyId = 10L;
        Subscription requesterSub = Subscription.builder().id(100L).build();
        FamilySubscription requesterFs = FamilySubscription.builder()
                .family(Family.builder().id(familyId).build())
                .subscription(requesterSub)
                .familyRole(FamilyRole.OWNER)
                .build();

        given(familySubscriptionRepository.findByMemberId(requesterMemberId)).willReturn(Optional.of(requesterFs));

        String phone = "01000000000";
        String hash = "SELF_HASH";
        FamilyMemberRequest selfReq = new FamilyMemberRequest("나자신", phone, FamilyRole.CHILD);
        AddFamilyMemberRequest request = new AddFamilyMemberRequest(ApplyType.ADD, "url", List.of(selfReq));

        Subscription selfSub = Subscription.builder().id(100L).phoneHash(hash).build(); // 요청자와 동일한 ID

        given(phoneHashIndexer.toHash(phone)).willReturn(hash);
        given(subscriptionRepository.findAllByPhoneHashIn(anyList())).willReturn(List.of(selfSub));

        // when & then
        assertThatThrownBy(() -> addFamilyMemberService.addFamilyMember(requesterMemberId, familyId, request))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(FamilyErrorCode.TARGET_ALREADY_IN_FAMILY.getMessage());
    }

    @Test
    @DisplayName("이미 대기 중인 신청이 있는 대상을 추가하면 예외가 발생한다.")
    void addFamilyMember_fail_duplicate_apply() {
        // given
        Long requesterMemberId = 1L;
        Long familyId = 10L;
        Subscription requesterSub = Subscription.builder().id(100L).build();
        FamilySubscription requesterFs = FamilySubscription.builder()
                .family(Family.builder().id(familyId).build())
                .subscription(requesterSub)
                .familyRole(FamilyRole.OWNER)
                .build();

        given(familySubscriptionRepository.findByMemberId(requesterMemberId)).willReturn(Optional.of(requesterFs));

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
}
