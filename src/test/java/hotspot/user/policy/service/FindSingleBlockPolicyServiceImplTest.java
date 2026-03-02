package hotspot.user.policy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import hotspot.user.policy.controller.response.BlockPolicyResponse;
import hotspot.user.policy.domain.BlockPolicy;
import hotspot.user.policy.service.port.BlockPolicyRepository;

@ExtendWith(MockitoExtension.class)
class FindSingleBlockPolicyServiceImplTest {

    @Mock
    private BlockPolicyRepository blockPolicyRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private FindSingleBlockPolicyServiceImpl service;

    private static final Long MEMBER_ID = 1L;
    private static final Long FAMILY_ID = 100L;
    private static final Long POLICY_ID = 10L;

    @Test
    @DisplayName("성공: 우리 가족 정책을 조회한다")
    void findFamilyPolicySuccess() {
        // given
        MemberDetailInfo memberDetail = MemberDetailInfo.builder()
                .familyId(FAMILY_ID)
                .role(FamilyRole.OWNER)
                .build();
        given(memberRepository.findDetailByIdAndEmail(anyLong(), isNull()))
                .willReturn(Optional.of(memberDetail));

        BlockPolicy policy = BlockPolicy.builder()
                .id(POLICY_ID)
                .familyId(FAMILY_ID)
                .name("가족 정책")
                .isActive(true)
                .build();
        given(blockPolicyRepository.findByBlockPolicyId(POLICY_ID)).willReturn(policy);

        // when
        BlockPolicyResponse result = service.find(POLICY_ID, MEMBER_ID, FAMILY_ID);

        // then
        assertThat(result.id()).isEqualTo(POLICY_ID);
        assertThat(result.familyId()).isEqualTo(FAMILY_ID);
        verify(blockPolicyRepository).findByBlockPolicyId(POLICY_ID);
    }

    @Test
    @DisplayName("성공: 관리자 정책은 누구나 조회 가능하다")
    void findAdminPolicySuccess() {
        // given
        MemberDetailInfo memberDetail = MemberDetailInfo.builder()
                .familyId(FAMILY_ID)
                .role(FamilyRole.OWNER)
                .build();
        given(memberRepository.findDetailByIdAndEmail(anyLong(), isNull()))
                .willReturn(Optional.of(memberDetail));

        BlockPolicy policy = BlockPolicy.builder()
                .id(POLICY_ID)
                .familyId(null) // 관리자 정책
                .name("관리자 정책")
                .isActive(true)
                .build();
        given(blockPolicyRepository.findByBlockPolicyId(POLICY_ID)).willReturn(policy);

        // when
        BlockPolicyResponse result = service.find(POLICY_ID, MEMBER_ID, FAMILY_ID);

        // then
        assertThat(result.id()).isEqualTo(POLICY_ID);
        assertThat(result.familyId()).isNull();
    }

    @Test
    @DisplayName("실패: 타인의 가족 정책 조회 시 예외가 발생한다")
    void findForeignPolicyFail() {
        // given
        MemberDetailInfo memberDetail = MemberDetailInfo.builder()
                .familyId(FAMILY_ID)
                .role(FamilyRole.OWNER)
                .build();
        given(memberRepository.findDetailByIdAndEmail(anyLong(), isNull()))
                .willReturn(Optional.of(memberDetail));

        BlockPolicy policy = BlockPolicy.builder()
                .id(POLICY_ID)
                .familyId(200L) // 타인 가족
                .build();
        given(blockPolicyRepository.findByBlockPolicyId(POLICY_ID)).willReturn(policy);

        // when & then
        assertThatThrownBy(() -> service.find(POLICY_ID, MEMBER_ID, FAMILY_ID))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", PolicyErrorCode.POLICY_ACCESS_DENIED);
    }
}
