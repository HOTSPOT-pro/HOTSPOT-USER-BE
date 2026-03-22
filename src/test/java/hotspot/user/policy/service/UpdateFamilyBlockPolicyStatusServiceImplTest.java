package hotspot.user.policy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.AuthErrorCode;
import hotspot.user.common.exception.code.FamilyErrorCode;
import hotspot.user.common.exception.code.PolicyErrorCode;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.member.domain.MemberDetailInfo;
import hotspot.user.member.service.port.MemberRepository;
import hotspot.user.outbox.notificationOutbox.service.port.UserAlertNotificationOutboxPort;
import hotspot.user.policy.controller.request.UpdateFamilyBlockPolicyStatusRequest;
import hotspot.user.policy.controller.response.UpdateFamilyBlockPolicyStatusResponse;
import hotspot.user.policy.domain.BlockPolicy;
import hotspot.user.policy.domain.PolicyType;
import hotspot.user.policy.service.port.BlockPolicyRepository;
import hotspot.user.policy.service.port.PolicySubRepository;
import hotspot.user.policy.service.util.FamilyPolicyDeactivatePublisher;

@ExtendWith(MockitoExtension.class)
class UpdateFamilyBlockPolicyStatusServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private BlockPolicyRepository blockPolicyRepository;

    @Mock
    private PolicySubRepository policySubRepository;

    @Mock
    private FamilyPolicyDeactivatePublisher familyPolicyDeactivatePublisher;

    @Mock
    private UserAlertNotificationOutboxPort userAlertNotificationOutboxPort;

    @InjectMocks
    private UpdateFamilyBlockPolicyStatusServiceImpl service;

    private static final Long MEMBER_ID = 1L;
    private static final Long FAMILY_ID = 100L;

    @Test
    @DisplayName("성공: 가족 소유 정책 상태를 일괄 동기화한다")
    void updateFamilyBlockPolicyStatusSuccess() {

        List<Long> requestActiveIds = List.of(1L, 2L);

        UpdateFamilyBlockPolicyStatusRequest request =
                new UpdateFamilyBlockPolicyStatusRequest(FAMILY_ID, requestActiveIds);

        MemberDetailInfo memberDetail = MemberDetailInfo.builder()
                .familyId(FAMILY_ID)
                .role(FamilyRole.OWNER)
                .build();
        given(memberRepository.findDetailById(MEMBER_ID)).willReturn(Optional.of(memberDetail));

        BlockPolicy p1 = BlockPolicy.builder().id(1L).isActive(true).policyType(PolicyType.SCHEDULED).build();
        BlockPolicy p2 = BlockPolicy.builder().id(2L).isActive(false).policyType(PolicyType.SCHEDULED).build();
        BlockPolicy p3 = BlockPolicy.builder()
                .id(3L)
                .name("night-block")
                .isActive(true)
                .policyType(PolicyType.ONCE)
                .build();

        given(blockPolicyRepository.findAllByFamilyId(FAMILY_ID))
                .willReturn(List.of(p1, p2, p3));
        given(policySubRepository.findActiveSubIdsByBlockPolicyIds(List.of(3L)))
                .willReturn(Map.of(3L, List.of(777L)));

        UpdateFamilyBlockPolicyStatusResponse response =
                service.updateFamilyBlockPolicyStatus(request, MEMBER_ID, FAMILY_ID);

        verify(blockPolicyRepository).bulkActivate(List.of(2L));

        verify(familyPolicyDeactivatePublisher).publish(List.of(3L), FAMILY_ID);
        verify(blockPolicyRepository).bulkDeActive(List.of(3L));

        verify(policySubRepository).bulkDeActiveByBlockPolicyIds(List.of(3L));
        verify(userAlertNotificationOutboxPort, times(1)).appendPolicyAlert(argThat(event ->
                event.subId().equals(777L)
                        && event.familyId().equals(FAMILY_ID)
                        && event.policyName().equals("night-block")
        ));

        assertThat(response.familyId()).isEqualTo(FAMILY_ID);
        assertThat(response.blockedPolicyIdList())
                .containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    @DisplayName("실패: 요청자가 가족의 OWNER가 아니면 예외가 발생한다")
    void validateOwnerAuthorityFailNotOwner() {

        UpdateFamilyBlockPolicyStatusRequest request =
                new UpdateFamilyBlockPolicyStatusRequest(FAMILY_ID, List.of(1L));

        MemberDetailInfo memberDetail = MemberDetailInfo.builder()
                .familyId(FAMILY_ID)
                .role(FamilyRole.PARENT)
                .build();
        given(memberRepository.findDetailById(MEMBER_ID)).willReturn(Optional.of(memberDetail));

        // when & then
        assertThatThrownBy(() -> service.updateFamilyBlockPolicyStatus(request, MEMBER_ID, FAMILY_ID))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", AuthErrorCode.ACCESS_DENIED);
    }

    @Test
    @DisplayName("실패: 요청자의 가족 ID와 회원의 소속 가족 ID가 다르면 예외가 발생한다")
    void validateOwnerAuthorityFailDifferentFamily() {

        UpdateFamilyBlockPolicyStatusRequest request =
                new UpdateFamilyBlockPolicyStatusRequest(FAMILY_ID, List.of(1L));

        MemberDetailInfo memberDetail = MemberDetailInfo.builder()
                .familyId(200L)
                .role(FamilyRole.OWNER)
                .build();
        given(memberRepository.findDetailById(MEMBER_ID)).willReturn(Optional.of(memberDetail));

        // when & then
        assertThatThrownBy(() -> service.updateFamilyBlockPolicyStatus(request, MEMBER_ID, FAMILY_ID))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", FamilyErrorCode.NOT_FAMILY_MEMBER);
    }

    @Test
    @DisplayName("실패: 요청한 정책 ID 중 우리 가족이 생성하지 않은 ID가 포함되어 있으면 예외가 발생한다")
    void validateAllPoliciesBelongToFamilyFail() {

        List<Long> requestActiveIds = List.of(1L, 999L);

        UpdateFamilyBlockPolicyStatusRequest request =
                new UpdateFamilyBlockPolicyStatusRequest(FAMILY_ID, requestActiveIds);

        MemberDetailInfo memberDetail = MemberDetailInfo.builder()
                .familyId(FAMILY_ID)
                .role(FamilyRole.OWNER)
                .build();
        given(memberRepository.findDetailById(MEMBER_ID)).willReturn(Optional.of(memberDetail));

        // 우리 가족 정책은 1, 2번뿐
        BlockPolicy p1 = BlockPolicy.builder().id(1L).isActive(true).build();
        BlockPolicy p2 = BlockPolicy.builder().id(2L).isActive(false).build();

        given(blockPolicyRepository.findAllByFamilyId(FAMILY_ID))
                .willReturn(List.of(p1, p2));

        assertThatThrownBy(() ->
                service.updateFamilyBlockPolicyStatus(request, MEMBER_ID, FAMILY_ID)
        )
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", PolicyErrorCode.POLICY_ACCESS_DENIED);
    }

    @Test
    @DisplayName("성공: 변경 사항이 없는 경우 벌크 업데이트를 호출하지 않는다")
    void updateNoChangesNoBulkCall() {

        List<Long> requestActiveIds = List.of(1L);

        UpdateFamilyBlockPolicyStatusRequest request =
                new UpdateFamilyBlockPolicyStatusRequest(FAMILY_ID, requestActiveIds);

        MemberDetailInfo memberDetail = MemberDetailInfo.builder()
                .familyId(FAMILY_ID)
                .role(FamilyRole.OWNER)
                .build();
        given(memberRepository.findDetailById(MEMBER_ID)).willReturn(Optional.of(memberDetail));

        // 이미 1번은 켜져있고, 2번은 꺼져있음 -> 요청(1번만 켜기)과 동일함
        BlockPolicy p1 = BlockPolicy.builder().id(1L).isActive(true).build();
        BlockPolicy p2 = BlockPolicy.builder().id(2L).isActive(false).build();

        given(blockPolicyRepository.findAllByFamilyId(FAMILY_ID))
                .willReturn(List.of(p1, p2));

        service.updateFamilyBlockPolicyStatus(request, MEMBER_ID, FAMILY_ID);

        verify(blockPolicyRepository, never()).bulkActivate(anyList());
        verify(blockPolicyRepository, never()).bulkDeActive(anyList());
        verify(policySubRepository, never()).bulkDeActiveByBlockPolicyIds(anyList());
        verify(familyPolicyDeactivatePublisher, never()).publish(anyList(), anyLong());
        verify(userAlertNotificationOutboxPort, never()).appendPolicyAlert(argThat(event -> true));
    }
}
