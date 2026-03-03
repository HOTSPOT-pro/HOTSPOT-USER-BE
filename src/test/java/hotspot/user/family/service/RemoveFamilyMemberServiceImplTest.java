package hotspot.user.family.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
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
import hotspot.user.family.domain.DeleteStatus;
import hotspot.user.family.domain.Family;
import hotspot.user.family.domain.FamilyApply;
import hotspot.user.family.domain.FamilyRemoveSchedule;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.service.port.FamilyApplyRepository;
import hotspot.user.family.service.port.FamilyRemoveScheduleRepository;
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
    private FamilyRemoveScheduleRepository familyRemoveScheduleRepository;

    @Test
    @DisplayName("성공: OWNER가 자신을 포함한 가족 구성원 삭제 신청을 하면 성공한다.")
    void removeFamilyMemberSuccess() {
        // given
        Long requesterMemberId = 1L;
        Long familyId = 10L;
        Long requesterSubId = 100L;
        Long targetSubId = 200L;

        // 방장 정보 설정
        FamilySubscription requesterFs = createFs(familyId, requesterSubId, FamilyRole.OWNER);
        given(familySubscriptionRepository.findByMemberId(requesterMemberId)).willReturn(Optional.of(requesterFs));

        // 삭제 대상 리스트 (본인 포함)
        RemoveFamilyMemberRequest request = new RemoveFamilyMemberRequest(List.of(requesterSubId, targetSubId));

        // 일괄 조회 모킹
        FamilySubscription targetFs = createFs(familyId, targetSubId, FamilyRole.CHILD);
        given(familySubscriptionRepository.findAllBySubIdIn(anyList())).willReturn(List.of(requesterFs, targetFs));
        given(familyRemoveScheduleRepository.findAllByTargetSubIdInAndStatus(anyList(), eq(DeleteStatus.SCHEDULED)))
                .willReturn(List.of()); // 중복 신청 없음

        given(familyApplyRepository.save(any())).willReturn(FamilyApply.builder().id(1L).familyId(familyId).build());
        given(familyRemoveScheduleRepository.saveAll(anyList())).willReturn(List.of(
                FamilyRemoveSchedule.builder().targetSubId(requesterSubId).build(),
                FamilyRemoveSchedule.builder().targetSubId(targetSubId).build()
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
        FamilySubscription requesterFs = createFs(10L, 100L, FamilyRole.CHILD); // OWNER 아님
        given(familySubscriptionRepository.findByMemberId(anyLong())).willReturn(Optional.of(requesterFs));

        RemoveFamilyMemberRequest request = new RemoveFamilyMemberRequest(List.of());

        // when & then
        assertThatThrownBy(() -> removeFamilyMemberService.removeFamilyMember(1L, request))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(FamilyErrorCode.ONLY_OWNER_CAN_MANAGE.getMessage());
    }

    @Test
    @DisplayName("실패: 삭제 대상 중 다른 가족 구성원이 포함되어 있으면 예외가 발생한다.")
    void removeFamilyMemberFailNotMyFamily() {
        // given
        Long requesterMemberId = 1L;
        Long myFamilyId = 10L;
        given(familySubscriptionRepository.findByMemberId(requesterMemberId))
                .willReturn(Optional.of(createFs(myFamilyId, 100L, FamilyRole.OWNER)));

        Long otherFamilySubId = 999L;
        RemoveFamilyMemberRequest request = new RemoveFamilyMemberRequest(List.of(otherFamilySubId));

        // 다른 가족 소속인 구성원 모킹
        FamilySubscription otherFs = createFs(20L, otherFamilySubId, FamilyRole.CHILD); // familyId가 20임
        given(familySubscriptionRepository.findAllBySubIdIn(anyList())).willReturn(List.of(otherFs));

        // when & then
        assertThatThrownBy(() -> removeFamilyMemberService.removeFamilyMember(requesterMemberId, request))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(FamilyErrorCode.NOT_FAMILY_MEMBER.getMessage());
    }

    @Test
    @DisplayName("실패: 이미 삭제 예정인 구성원을 다시 삭제 신청하면 예외가 발생한다.")
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

        // 이미 스케줄이 존재함 모킹
        given(familyRemoveScheduleRepository.findAllByTargetSubIdInAndStatus(anyList(), eq(DeleteStatus.SCHEDULED)))
                .willReturn(List.of(FamilyRemoveSchedule.builder().targetSubId(targetSubId).build()));

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
