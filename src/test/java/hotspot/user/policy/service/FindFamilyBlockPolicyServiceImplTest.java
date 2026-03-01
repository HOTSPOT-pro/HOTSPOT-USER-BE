package hotspot.user.policy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import hotspot.user.common.exception.code.AuthErrorCode;
import hotspot.user.common.exception.code.FamilyErrorCode;
import hotspot.user.family.domain.Family;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.policy.controller.response.BlockPolicyResponse;
import hotspot.user.policy.domain.BlockPolicy;
import hotspot.user.policy.service.port.BlockPolicyRepository;

/**
 * 우리 가족 정책 조회 서비스(FindFamilyBlockPolicyServiceImpl) 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class FindFamilyBlockPolicyServiceImplTest {

    @Mock
    private BlockPolicyRepository blockPolicyRepository;

    @Mock
    private FamilySubscriptionRepository familySubscriptionRepository;

    @InjectMocks
    private FindFamilyBlockPolicyServiceImpl findFamilyBlockPolicyService;

    @Test
    @DisplayName("성공: OWNER 권한을 가진 사용자가 본인 가족의 정책 목록을 조회한다")
    void findFamilyPoliciesSuccess() {
        // given
        Long memberId = 1L;
        Long familyId = 100L;

        Family family = Family.builder().id(familyId).build();
        FamilySubscription familySub = FamilySubscription.builder()
                .family(family)
                .familyRole(FamilyRole.OWNER)
                .build();

        BlockPolicy policy = BlockPolicy.builder().id(10L).name("가족 정책").familyId(familyId).build();

        given(familySubscriptionRepository.findByMemberId(memberId)).willReturn(Optional.of(familySub));
        given(blockPolicyRepository.findAllByFamilyId(familyId)).willReturn(List.of(policy));

        // when
        List<BlockPolicyResponse> result = findFamilyBlockPolicyService.findAllByFamilyId(memberId, familyId);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("가족 정책");
    }

    @Test
    @DisplayName("성공: PARENT 권한을 가진 사용자가 본인 가족의 정책 목록을 조회한다")
    void findFamilyPoliciesSuccessWithParentRole() {
        // given
        Long memberId = 1L;
        Long familyId = 100L;

        Family family = Family.builder().id(familyId).build();
        FamilySubscription familySub = FamilySubscription.builder()
                .family(family)
                .familyRole(FamilyRole.PARENT) // PARENT 역할
                .build();

        BlockPolicy policy = BlockPolicy.builder().id(10L).name("가족 정책").familyId(familyId).build();

        given(familySubscriptionRepository.findByMemberId(memberId)).willReturn(Optional.of(familySub));
        given(blockPolicyRepository.findAllByFamilyId(familyId)).willReturn(List.of(policy));

        // when
        List<BlockPolicyResponse> result = findFamilyBlockPolicyService.findAllByFamilyId(memberId, familyId);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("가족 정책");
    }

    @Test
    @DisplayName("실패: 가족 서비스에 가입되지 않은 회원이 조회 시도 시 예외가 발생한다")
    void findFamilyPoliciesFailByNotSubscribed() {
        // given
        Long memberId = 1L;
        given(familySubscriptionRepository.findByMemberId(memberId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> findFamilyBlockPolicyService.findAllByFamilyId(memberId, 100L))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(FamilyErrorCode.FAMILY_SUBSCRIPTION_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("실패: 요청한 가족 ID가 실제 소속된 가족 ID와 다를 경우 예외가 발생한다")
    void findFamilyPoliciesFailByOtherFamily() {
        // given
        Long memberId = 1L;
        Long myFamilyId = 100L;
        Long otherFamilyId = 200L;

        FamilySubscription familySub = FamilySubscription.builder()
                .family(Family.builder().id(myFamilyId).build())
                .build();

        given(familySubscriptionRepository.findByMemberId(memberId)).willReturn(Optional.of(familySub));

        // when & then
        assertThatThrownBy(() -> findFamilyBlockPolicyService.findAllByFamilyId(memberId, otherFamilyId))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(FamilyErrorCode.NOT_FAMILY_MEMBER.getMessage());
    }

    @Test
    @DisplayName("실패: 조회 권한이 없는 역할(CHILD)이 조회 시도 시 예외가 발생한다")
    void findFamilyPoliciesFailByAccessDenied() {
        // given
        Long memberId = 1L;
        Long familyId = 100L;

        FamilySubscription familySub = FamilySubscription.builder()
                .family(Family.builder().id(familyId).build())
                .familyRole(FamilyRole.CHILD) // 권한 없음
                .build();

        given(familySubscriptionRepository.findByMemberId(memberId)).willReturn(Optional.of(familySub));

        // when & then
        assertThatThrownBy(() -> findFamilyBlockPolicyService.findAllByFamilyId(memberId, familyId))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(AuthErrorCode.ACCESS_DENIED.getMessage());
    }
}
