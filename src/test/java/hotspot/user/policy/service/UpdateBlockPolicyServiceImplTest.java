package hotspot.user.policy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.PolicyErrorCode;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.member.domain.MemberDetailInfo;
import hotspot.user.member.service.port.MemberRepository;
import hotspot.user.policy.controller.request.BlockPolicyRequest;
import hotspot.user.policy.controller.response.BlockPolicyResponse;
import hotspot.user.policy.domain.BlockPolicy;
import hotspot.user.policy.domain.PolicyType;
import hotspot.user.policy.service.port.BlockPolicyRepository;

@ExtendWith(MockitoExtension.class)
class UpdateBlockPolicyServiceImplTest {

    @Mock
    private BlockPolicyRepository blockPolicyRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private UpdateBlockPolicyServiceImpl service;

    private static final Long MEMBER_ID = 1L;
    private static final Long FAMILY_ID = 100L;
    private static final Long POLICY_ID = 10L;

    @Test
    @DisplayName("성공: 우리 가족 정책을 수정한다")
    void updateSuccess() {
        // given
        BlockPolicyRequest request = new BlockPolicyRequest(
                "수정된 이름", PolicyType.ONCE, null, "수정된 설명", false);

        MemberDetailInfo memberDetail = MemberDetailInfo.builder()
                .familyId(FAMILY_ID)
                .role(FamilyRole.OWNER)
                .build();
        given(memberRepository.findDetailByIdAndEmail(anyLong(), isNull()))
                .willReturn(Optional.of(memberDetail));

        BlockPolicy existingPolicy = BlockPolicy.builder()
                .id(POLICY_ID)
                .familyId(FAMILY_ID)
                .name("기존 이름")
                .policyType(PolicyType.ONCE)
                .isActive(true)
                .build();
        given(blockPolicyRepository.findByBlockPolicyId(POLICY_ID)).willReturn(existingPolicy);
        given(blockPolicyRepository.save(any(BlockPolicy.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        BlockPolicyResponse response = service.update(request, POLICY_ID, MEMBER_ID, FAMILY_ID);

        // then
        assertThat(response.name()).isEqualTo("수정된 이름");
        assertThat(response.policyDescription()).isEqualTo("수정된 설명");
        assertThat(response.isActive()).isFalse();
        verify(blockPolicyRepository).save(any(BlockPolicy.class));
    }

    @Test
    @DisplayName("실패: 관리자 정책은 수정할 수 없다")
    void updateFailAdminPolicy() {
        // given
        BlockPolicyRequest request = new BlockPolicyRequest("이름", PolicyType.ONCE, null, "설명", true);

        MemberDetailInfo memberDetail = MemberDetailInfo.builder()
                .familyId(FAMILY_ID)
                .role(FamilyRole.OWNER)
                .build();
        given(memberRepository.findDetailByIdAndEmail(anyLong(), isNull()))
                .willReturn(Optional.of(memberDetail));

        BlockPolicy adminPolicy = BlockPolicy.builder()
                .id(POLICY_ID)
                .familyId(null) // 관리자 정책
                .build();
        given(blockPolicyRepository.findByBlockPolicyId(POLICY_ID)).willReturn(adminPolicy);

        // when & then
        assertThatThrownBy(() -> service.update(request, POLICY_ID, MEMBER_ID, FAMILY_ID))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", PolicyErrorCode.POLICY_ACCESS_DENIED);
    }

    @Test
    @DisplayName("실패: 타인의 가족 정책은 수정할 수 없다")
    void updateFailForeignPolicy() {
        // given
        BlockPolicyRequest request = new BlockPolicyRequest("이름", PolicyType.ONCE, null, "설명", true);

        MemberDetailInfo memberDetail = MemberDetailInfo.builder()
                .familyId(FAMILY_ID)
                .role(FamilyRole.OWNER)
                .build();
        given(memberRepository.findDetailByIdAndEmail(anyLong(), isNull()))
                .willReturn(Optional.of(memberDetail));

        BlockPolicy foreignPolicy = BlockPolicy.builder()
                .id(POLICY_ID)
                .familyId(200L) // 타인 가족
                .build();
        given(blockPolicyRepository.findByBlockPolicyId(POLICY_ID)).willReturn(foreignPolicy);

        // when & then
        assertThatThrownBy(() -> service.update(request, POLICY_ID, MEMBER_ID, FAMILY_ID))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", PolicyErrorCode.POLICY_ACCESS_DENIED);
    }
}
