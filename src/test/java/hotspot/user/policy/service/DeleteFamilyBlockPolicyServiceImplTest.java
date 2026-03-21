package hotspot.user.policy.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
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
import hotspot.user.policy.domain.BlockPolicy;
import hotspot.user.policy.service.port.BlockPolicyRepository;
import hotspot.user.policy.service.port.PolicySubRepository;
import hotspot.user.policy.service.util.PolicyDeletedOutboxPublisher;

@ExtendWith(MockitoExtension.class)
class DeleteFamilyBlockPolicyServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private BlockPolicyRepository blockPolicyRepository;

    @Mock
    private PolicySubRepository policySubRepository;

    @Mock
    private PolicyDeletedOutboxPublisher policyDeletedOutboxPublisher;

    @InjectMocks
    private DeleteFamilyBlockPolicyServiceImpl deleteFamilyBlockPolicyService;

    private static final Long MEMBER_ID = 1L;
    private static final Long FAMILY_ID = 100L;

    @Test
    @DisplayName("성공: 우리 가족 정책을 일괄 삭제한다")
    void deleteSuccess() {

        List<Long> policyIds = List.of(10L, 20L);

        MemberDetailInfo memberDetail = MemberDetailInfo.builder()
                .familyId(FAMILY_ID)
                .role(FamilyRole.OWNER)
                .build();

        given(memberRepository.findDetailById(anyLong()))
                .willReturn(Optional.of(memberDetail));

        BlockPolicy policy1 = BlockPolicy.builder()
                .id(10L)
                .familyId(FAMILY_ID)
                .build();

        BlockPolicy policy2 = BlockPolicy.builder()
                .id(20L)
                .familyId(FAMILY_ID)
                .build();

        given(blockPolicyRepository.findAllById(policyIds))
                .willReturn(List.of(policy1, policy2));

        // when
        deleteFamilyBlockPolicyService.delete(policyIds, MEMBER_ID, FAMILY_ID);

        // then
        verify(policyDeletedOutboxPublisher).publishAll(policyIds, FAMILY_ID);

        verify(policySubRepository).bulkDeActiveByBlockPolicyIds(policyIds);
        verify(blockPolicyRepository).bulkDelete(policyIds);
    }

    @Test
    @DisplayName("실패: 요청자가 가족의 OWNER가 아니면 예외가 발생한다")
    void deleteFailNotOwner() {

        List<Long> policyIds = List.of(10L);

        MemberDetailInfo memberDetail = MemberDetailInfo.builder()
                .familyId(FAMILY_ID)
                .role(FamilyRole.PARENT)
                .build();

        given(memberRepository.findDetailById(anyLong()))
                .willReturn(Optional.of(memberDetail));

        assertThatThrownBy(() ->
                deleteFamilyBlockPolicyService.delete(policyIds, MEMBER_ID, FAMILY_ID)
        )
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", AuthErrorCode.ACCESS_DENIED);
    }

    @Test
    @DisplayName("실패: 요청된 정책 ID 중 존재하지 않는 ID가 있으면 예외가 발생한다")
    void deleteFailPolicyNotFound() {

        List<Long> policyIds = List.of(10L, 999L);

        MemberDetailInfo memberDetail = MemberDetailInfo.builder()
                .familyId(FAMILY_ID)
                .role(FamilyRole.OWNER)
                .build();

        given(memberRepository.findDetailById(anyLong()))
                .willReturn(Optional.of(memberDetail));

        BlockPolicy policy1 = BlockPolicy.builder()
                .id(10L)
                .familyId(FAMILY_ID)
                .build();

        given(blockPolicyRepository.findAllById(policyIds))
                .willReturn(List.of(policy1));

        assertThatThrownBy(() ->
                deleteFamilyBlockPolicyService.delete(policyIds, MEMBER_ID, FAMILY_ID)
        )
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", PolicyErrorCode.POLICY_NOT_FOUND);
    }

    @Test
    @DisplayName("실패: 요청된 정책 중 타인의 가족 정책이 포함되어 있으면 예외가 발생한다")
    void deleteFailForeignPolicy() {

        List<Long> policyIds = List.of(10L, 50L);

        MemberDetailInfo memberDetail = MemberDetailInfo.builder()
                .familyId(FAMILY_ID)
                .role(FamilyRole.OWNER)
                .build();

        given(memberRepository.findDetailById(anyLong()))
                .willReturn(Optional.of(memberDetail));

        BlockPolicy policy1 = BlockPolicy.builder()
                .id(10L)
                .familyId(FAMILY_ID)
                .build();

        BlockPolicy policy2 = BlockPolicy.builder()
                .id(50L)
                .familyId(200L)
                .build();

        given(blockPolicyRepository.findAllById(policyIds))
                .willReturn(List.of(policy1, policy2));

        assertThatThrownBy(() ->
                deleteFamilyBlockPolicyService.delete(policyIds, MEMBER_ID, FAMILY_ID)
        )
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", PolicyErrorCode.POLICY_ACCESS_DENIED);
    }

    @Test
    @DisplayName("실패: 회원을 찾을 수 없으면 예외가 발생한다")
    void deleteFailMemberNotFound() {
        // given
        given(memberRepository.findDetailById(anyLong()))
                .willReturn(Optional.empty());

        assertThatThrownBy(() ->
                deleteFamilyBlockPolicyService.delete(List.of(1L), MEMBER_ID, FAMILY_ID)
        )
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", MemberErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("실패: 요청자의 가족 ID와 회원의 소속 가족 ID가 다르면 예외가 발생한다")
    void deleteFailDifferentFamily() {

        MemberDetailInfo memberDetail = MemberDetailInfo.builder()
                .familyId(200L)
                .role(FamilyRole.OWNER)
                .build();

        given(memberRepository.findDetailById(anyLong()))
                .willReturn(Optional.of(memberDetail));

        assertThatThrownBy(() ->
                deleteFamilyBlockPolicyService.delete(List.of(1L), MEMBER_ID, FAMILY_ID)
        )
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", FamilyErrorCode.NOT_FAMILY_MEMBER);
    }
}
