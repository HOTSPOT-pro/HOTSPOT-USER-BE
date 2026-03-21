package hotspot.user.policy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
import hotspot.user.policy.domain.PolicySnapshot;
import hotspot.user.policy.domain.PolicyType;
import hotspot.user.policy.service.port.BlockPolicyRepository;

@ExtendWith(MockitoExtension.class)
class CreateBlockPolicyServiceImplTest {

    @Mock
    private BlockPolicyRepository blockPolicyRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private CreateBlockPolicyServiceImpl service;

    private static final Long MEMBER_ID = 1L;
    private static final Long FAMILY_ID = 100L;

    @Test
    @DisplayName("성공: 우리 가족 정책을 생성한다")
    void createSuccess() {
        // given
        PolicySnapshot snapshot = PolicySnapshot.builder()
                .durationMinutes(30)
                .build();
        BlockPolicyRequest request = new BlockPolicyRequest(
                "정책 이름", PolicyType.ONCE, snapshot, "정책 설명", true);

        MemberDetailInfo memberDetail = MemberDetailInfo.builder()
                .familyId(FAMILY_ID)
                .role(FamilyRole.OWNER)
                .build();
        given(memberRepository.findDetailById(anyLong()))
                .willReturn(Optional.of(memberDetail));

        BlockPolicy savedPolicy = BlockPolicy.builder()
                .id(10L)
                .name("정책 이름")
                .familyId(FAMILY_ID)
                .policyType(PolicyType.ONCE)
                .policySnapshot(snapshot)
                .policyDescription("정책 설명")
                .isActive(true)
                .build();
        given(blockPolicyRepository.save(any(BlockPolicy.class))).willReturn(savedPolicy);

        // when
        BlockPolicyResponse response = service.create(request, MEMBER_ID, FAMILY_ID);

        // then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.name()).isEqualTo("정책 이름");
        verify(blockPolicyRepository).save(any(BlockPolicy.class));
    }

    @Test
    @DisplayName("실패: 유효하지 않은 스냅샷 형식으로 정책 생성 시 예외가 발생한다")
    void createFailInvalidSnapshot() {
        // given
        PolicySnapshot invalidSnapshot = PolicySnapshot.builder().build(); // 빈 스냅샷
        BlockPolicyRequest request = new BlockPolicyRequest(
                "정책 이름", PolicyType.SCHEDULED, invalidSnapshot, "정책 설명", true);

        MemberDetailInfo memberDetail = MemberDetailInfo.builder()
                .familyId(FAMILY_ID)
                .role(FamilyRole.OWNER)
                .build();
        given(memberRepository.findDetailById(anyLong()))
                .willReturn(Optional.of(memberDetail));

        // when & then
        assertThatThrownBy(() -> service.create(request, MEMBER_ID, FAMILY_ID))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", PolicyErrorCode.INVALID_POLICY_FORMAT);
    }
}
