package hotspot.user.family.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.FamilyErrorCode;
import hotspot.user.family.controller.request.RemoveFamilyMemberRequest;
import hotspot.user.family.controller.response.RemoveFamilyMemberResponse;
import hotspot.user.family.domain.Family;
import hotspot.user.family.domain.FamilyApply;
import hotspot.user.family.domain.FamilyApplyTarget;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.service.port.FamilyApplyRepository;
import hotspot.user.family.service.port.FamilyApplyTargetRepository;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.subscription.domain.Subscription;

@ExtendWith(MockitoExtension.class)
class RemoveFamilyMemberServiceImplTest {

    @InjectMocks
    private RemoveFamilyMemberServiceImpl removeFamilyMemberService;

    @Mock
    private FamilyApplyRepository familyApplyRepository;

    @Mock
    private FamilySubscriptionRepository familySubscriptionRepository;

    @Mock
    private FamilyApplyTargetRepository familyApplyTargetRepository;

    @Test
    @DisplayName("성공: 방장이 자신을 포함한 가족 구성원 삭제 신청을 하면 성공한다.")
    void removeFamilyMemberSuccess() {
        // given
        Long requesterMemberId = 1L;
        Long familyId = 10L;
        Long requesterSubId = 100L;
        Long targetSubId = 200L;

        FamilySubscription requesterFs = createFs(familyId, requesterSubId, FamilyRole.OWNER);
        given(familySubscriptionRepository.findByMemberId(requesterMemberId)).willReturn(Optional.of(requesterFs));

        RemoveFamilyMemberRequest request = new RemoveFamilyMemberRequest(List.of(requesterSubId, targetSubId));

        FamilySubscription targetFs = createFs(familyId, targetSubId, FamilyRole.CHILD);
        given(familySubscriptionRepository.findAllBySubIdIn(anyList())).willReturn(List.of(requesterFs, targetFs));

        // 중복 신청 없음 모킹 (FamilyApplyTarget 테이블 조회)
        given(familyApplyTargetRepository.findAllPendingByTargetSubIdIn(anyList())).willReturn(List.of());

        given(familyApplyRepository.save(any())).willReturn(FamilyApply.builder().id(1L).familyId(familyId).build());
        given(familyApplyTargetRepository.saveAll(anyList())).willReturn(List.of(
                FamilyApplyTarget.builder().targetSubId(requesterSubId).build(),
                FamilyApplyTarget.builder().targetSubId(targetSubId).build()
        ));

        // when
        RemoveFamilyMemberResponse response = removeFamilyMemberService.removeFamilyMember(requesterMemberId, request);

        // then
        assertThat(response.subIdList()).hasSize(2);
        assertThat(response.familyId()).isEqualTo(familyId);
    }

    @Test
    @DisplayName("실패: 요청자가 OWNER가 아니면 예외가 발생한다.")
    void removeFamilyMemberFailNotOwner() {
        // given
        FamilySubscription requesterFs = createFs(10L, 100L, FamilyRole.CHILD);
        given(familySubscriptionRepository.findByMemberId(anyLong())).willReturn(Optional.of(requesterFs));

        RemoveFamilyMemberRequest request = new RemoveFamilyMemberRequest(List.of());

        // when & then
        assertThatThrownBy(() -> removeFamilyMemberService.removeFamilyMember(1L, request))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(FamilyErrorCode.ONLY_OWNER_CAN_MANAGE.getMessage());
    }

    @Test
    @DisplayName("실패: 삭제 대상 중 존재하지 않는 회선이 포함되어 있으면 예외가 발생한다.")
    void removeFamilyMemberFailSubscriptionNotFound() {
        // given
        Long requesterMemberId = 1L;
        given(familySubscriptionRepository.findByMemberId(requesterMemberId))
                .willReturn(Optional.of(createFs(10L, 100L, FamilyRole.OWNER)));

        RemoveFamilyMemberRequest request = new RemoveFamilyMemberRequest(List.of(999L));
        given(familySubscriptionRepository.findAllBySubIdIn(anyList())).willReturn(List.of()); // 아무도 못찾음

        // when & then
        assertThatThrownBy(() -> removeFamilyMemberService.removeFamilyMember(requesterMemberId, request))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(FamilyErrorCode.FAMILY_SUBSCRIPTION_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("실패: 이미 삭제 대기 중인(PENDING) 신청이 있으면 예외가 발생한다.")
    void removeFamilyMemberFailDuplicateApply() {
        // given
        Long requesterMemberId = 1L;
        Long familyId = 10L;
        given(familySubscriptionRepository.findByMemberId(requesterMemberId))
                .willReturn(Optional.of(createFs(familyId, 100L, FamilyRole.OWNER)));

        Long targetSubId = 200L;
        RemoveFamilyMemberRequest request = new RemoveFamilyMemberRequest(List.of(targetSubId));

        given(familySubscriptionRepository.findAllBySubIdIn(anyList()))
                .willReturn(List.of(createFs(familyId, targetSubId, FamilyRole.CHILD)));

        // 이미 대기 중인 신청 존재 모킹
        given(familyApplyTargetRepository.findAllPendingByTargetSubIdIn(anyList()))
                .willReturn(List.of(FamilyApplyTarget.builder().targetSubId(targetSubId).build()));

        // when & then
        assertThatThrownBy(() -> removeFamilyMemberService.removeFamilyMember(requesterMemberId, request))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(FamilyErrorCode.DUPLICATE_FAMILY_APPLY.getMessage());
    }

    private FamilySubscription createFs(Long familyId, Long subId, FamilyRole role) {
        return FamilySubscription.builder()
                .family(Family.builder().id(familyId).build())
                .subscription(Subscription.builder().id(subId).build())
                .familyRole(role)
                .build();
    }
}
