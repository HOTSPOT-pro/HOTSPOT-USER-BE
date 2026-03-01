package hotspot.user.policy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
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
import hotspot.user.common.exception.code.MemberErrorCode;
import hotspot.user.common.exception.code.PolicyErrorCode;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.member.domain.MemberDetailInfo;
import hotspot.user.member.service.port.MemberRepository;
import hotspot.user.policy.controller.request.UpdateFamilyBlockPolicyStatusRequest;
import hotspot.user.policy.controller.response.UpdateFamilyBlockPolicyStatusResponse;
import hotspot.user.policy.domain.BlockPolicy;
import hotspot.user.policy.service.port.BlockPolicyRepository;
import hotspot.user.policy.service.port.PolicySubRepository;

@ExtendWith(MockitoExtension.class)
class UpdateFamilyBlockPolicyStatusServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private BlockPolicyRepository blockPolicyRepository;

    @Mock
    private PolicySubRepository policySubRepository;

    @InjectMocks
    private UpdateFamilyBlockPolicyStatusServiceImpl service;

    private final Long MEMBER_ID = 1L;
    private final Long FAMILY_ID = 100L;

    @Test
    @DisplayName("성공: 가족 소유 정책 상태를 일괄 동기화한다")
    void updateFamilyBlockPolicyStatusSuccess() {
        // given
        // 1. 요청: 정책 1, 2를 활성화하고 싶음 (기존 3은 목록에 없음 -> 비활성화 대상)
        List<Long> requestActiveIds = List.of(1L, 2L);
        UpdateFamilyBlockPolicyStatusRequest request = new UpdateFamilyBlockPolicyStatusRequest(FAMILY_ID, requestActiveIds);

        // 2. 권한 검증용 Mock
        MemberDetailInfo memberDetail = MemberDetailInfo.builder()
                .familyId(FAMILY_ID)
                .role(FamilyRole.OWNER)
                .build();
        given(memberRepository.findDetailByIdAndEmail(MEMBER_ID, null)).willReturn(Optional.of(memberDetail));

        // 3. 기존 가족 정책 Mock (1: 이미 활성, 2: 비활성, 3: 활성)
        BlockPolicy p1 = BlockPolicy.builder().id(1L).isActive(true).build();
        BlockPolicy p2 = BlockPolicy.builder().id(2L).isActive(false).build();
        BlockPolicy p3 = BlockPolicy.builder().id(3L).isActive(true).build();
        given(blockPolicyRepository.findAllByFamilyId(FAMILY_ID)).willReturn(List.of(p1, p2, p3));

        // when
        UpdateFamilyBlockPolicyStatusResponse response = service.updateFamilyBlockPolicyStatus(request, MEMBER_ID, FAMILY_ID);

        // then
        // - p2는 켜져야 함 (Activate)
        verify(blockPolicyRepository).bulkActivate(List.of(2L));
        // - p3는 꺼져야 함 (Deactivate)
        verify(blockPolicyRepository).bulkDeActive(List.of(3L));
        // - 정책이 꺼졌으므로 policy_sub 연관 데이터도 비활성화되어야 함
        verify(policySubRepository).bulkDeActiveByBlockPolicyIds(List.of(3L));

        assertThat(response.familyId()).isEqualTo(FAMILY_ID);
        assertThat(response.blockedPolicyIdList()).containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    @DisplayName("실패: 요청자가 가족의 OWNER가 아니면 예외가 발생한다")
    void validateOwnerAuthorityFailNotOwner() {
        // given
        UpdateFamilyBlockPolicyStatusRequest request = new UpdateFamilyBlockPolicyStatusRequest(FAMILY_ID, List.of(1L));
        MemberDetailInfo memberDetail = MemberDetailInfo.builder()
                .familyId(FAMILY_ID)
                .role(FamilyRole.PARENT) // PARENT는 권한 없음
                .build();
        given(memberRepository.findDetailByIdAndEmail(MEMBER_ID, null)).willReturn(Optional.of(memberDetail));

        // when & then
        assertThatThrownBy(() -> service.updateFamilyBlockPolicyStatus(request, MEMBER_ID, FAMILY_ID))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", AuthErrorCode.ACCESS_DENIED);
    }

    @Test
    @DisplayName("실패: 요청자의 가족 ID와 회원의 소속 가족 ID가 다르면 예외가 발생한다")
    void validateOwnerAuthorityFailDifferentFamily() {
        // given
        UpdateFamilyBlockPolicyStatusRequest request = new UpdateFamilyBlockPolicyStatusRequest(FAMILY_ID, List.of(1L));
        MemberDetailInfo memberDetail = MemberDetailInfo.builder()
                .familyId(200L) // 다른 가족
                .role(FamilyRole.OWNER)
                .build();
        given(memberRepository.findDetailByIdAndEmail(MEMBER_ID, null)).willReturn(Optional.of(memberDetail));

        // when & then
        assertThatThrownBy(() -> service.updateFamilyBlockPolicyStatus(request, MEMBER_ID, FAMILY_ID))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", FamilyErrorCode.NOT_FAMILY_MEMBER);
    }

    @Test
    @DisplayName("실패: 요청한 정책 ID 중 우리 가족이 생성하지 않은 ID가 포함되어 있으면 예외가 발생한다")
    void validateAllPoliciesBelongToFamilyFail() {
        // given
        // 요청에는 1번(가족꺼), 999번(타인꺼) 포함
        List<Long> requestActiveIds = List.of(1L, 999L);
        UpdateFamilyBlockPolicyStatusRequest request = new UpdateFamilyBlockPolicyStatusRequest(FAMILY_ID, requestActiveIds);

        MemberDetailInfo memberDetail = MemberDetailInfo.builder()
                .familyId(FAMILY_ID)
                .role(FamilyRole.OWNER)
                .build();
        given(memberRepository.findDetailByIdAndEmail(MEMBER_ID, null)).willReturn(Optional.of(memberDetail));

        // 우리 가족 정책은 1, 2번뿐
        BlockPolicy p1 = BlockPolicy.builder().id(1L).isActive(true).build();
        BlockPolicy p2 = BlockPolicy.builder().id(2L).isActive(false).build();
        given(blockPolicyRepository.findAllByFamilyId(FAMILY_ID)).willReturn(List.of(p1, p2));

        // when & then
        assertThatThrownBy(() -> service.updateFamilyBlockPolicyStatus(request, MEMBER_ID, FAMILY_ID))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", PolicyErrorCode.POLICY_ACCESS_DENIED);
    }

    @Test
    @DisplayName("성공: 변경 사항이 없는 경우(이미 요청 상태와 동일한 경우) 벌크 업데이트를 호출하지 않는다")
    void updateNoChangesNoBulkCall() {
        // given
        List<Long> requestActiveIds = List.of(1L);
        UpdateFamilyBlockPolicyStatusRequest request = new UpdateFamilyBlockPolicyStatusRequest(FAMILY_ID, requestActiveIds);

        MemberDetailInfo memberDetail = MemberDetailInfo.builder()
                .familyId(FAMILY_ID)
                .role(FamilyRole.OWNER)
                .build();
        given(memberRepository.findDetailByIdAndEmail(MEMBER_ID, null)).willReturn(Optional.of(memberDetail));

        // 이미 1번은 켜져있고, 2번은 꺼져있음 -> 요청(1번만 켜기)과 동일함
        BlockPolicy p1 = BlockPolicy.builder().id(1L).isActive(true).build();
        BlockPolicy p2 = BlockPolicy.builder().id(2L).isActive(false).build();
        given(blockPolicyRepository.findAllByFamilyId(FAMILY_ID)).willReturn(List.of(p1, p2));

        // when
        service.updateFamilyBlockPolicyStatus(request, MEMBER_ID, FAMILY_ID);

        // then
        verify(blockPolicyRepository, never()).bulkActivate(anyList());
        verify(blockPolicyRepository, never()).bulkDeActive(anyList());
        verify(policySubRepository, never()).bulkDeActiveByBlockPolicyIds(anyList());
    }
}
