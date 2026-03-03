package hotspot.user.family.domain.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import hotspot.user.family.controller.request.RemoveFamilyMemberRequest;
import hotspot.user.family.controller.response.RemoveFamilyMemberResponse;
import hotspot.user.family.domain.ApplyStatus;
import hotspot.user.family.domain.ApplyType;
import hotspot.user.family.domain.FamilyApply;
import hotspot.user.family.domain.FamilyApplyTarget;
import hotspot.user.member.domain.FamilyRole;

class RemoveFamilyMemberMapperTest {

    @Test
    @DisplayName("성공: 삭제 신청서(FamilyApply) 도메인으로 변환한다.")
    void toFamilyApplySuccess() {
        // given
        RemoveFamilyMemberRequest request = new RemoveFamilyMemberRequest(List.of(20L));

        // when
        FamilyApply result = RemoveFamilyMemberMapper.toFamilyApply(10L, 1L, request);

        // then
        assertThat(result.getRequesterSubId()).isEqualTo(10L);
        assertThat(result.getApplyType()).isEqualTo(ApplyType.REMOVE);
        assertThat(result.getStatus()).isEqualTo(ApplyStatus.PENDING);
    }

    @Test
    @DisplayName("성공: 신청 대상자(FamilyApplyTarget) 리스트를 최종 응답 DTO로 변환한다.")
    void toRemoveFamilyMemberResponseSuccess() {
        // given
        FamilyApply apply = FamilyApply.builder().id(100L).familyId(1L).build();
        FamilyApplyTarget target = FamilyApplyTarget.builder()
                .targetSubId(20L).targetFamilyRole(FamilyRole.CHILD).build();

        // when
        RemoveFamilyMemberResponse response = RemoveFamilyMemberMapper
                .toRemoveFamilyMemberResponse(apply, List.of(target));

        // then
        assertThat(response.familyApplyId()).isEqualTo(100L);
        assertThat(response.subIdList()).containsExactly(20L);
    }
}
