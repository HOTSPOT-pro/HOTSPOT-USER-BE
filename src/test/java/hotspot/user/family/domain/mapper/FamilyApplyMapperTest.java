package hotspot.user.family.domain.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import hotspot.user.family.controller.request.AddFamilyMemberRequest;
import hotspot.user.family.controller.request.CreateNewFamilyRequest;
import hotspot.user.family.controller.response.AddFamilyMemberResponse;
import hotspot.user.family.controller.response.CreateNewFamilyResponse;
import hotspot.user.family.domain.ApplyStatus;
import hotspot.user.family.domain.ApplyType;
import hotspot.user.family.domain.FamilyApply;
import hotspot.user.family.domain.FamilyApplyTarget;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.member.domain.Member;
import hotspot.user.subscription.domain.Subscription;

class FamilyApplyMapperTest {

    @Test
    @DisplayName("성공: CreateNewFamilyRequest를 FamilyApply 도메인으로 변환한다.")
    void toFamilyApplyFromCreateNewRequestSuccess() {
        // given
        CreateNewFamilyRequest request = new CreateNewFamilyRequest(ApplyType.CREATE, "url", List.of());
        String certificatedKey = "certificated";

        // when
        FamilyApply result = FamilyApplyMapper.toFamilyApply(10L, null, request, certificatedKey);

        // then
        assertThat(result.getRequesterSubId()).isEqualTo(10L);
        assertThat(result.getApplyType()).isEqualTo(ApplyType.CREATE);
        assertThat(result.getStatus()).isEqualTo(ApplyStatus.PENDING);
        assertThat(certificatedKey).isEqualTo("certificated");
    }

    @Test
    @DisplayName("성공: AddFamilyMemberRequest를 FamilyApply 도메인으로 변환한다.")
    void toFamilyApplyFromAddRequestSuccess() {
        // given
        AddFamilyMemberRequest request = new AddFamilyMemberRequest(ApplyType.ADD, "url", List.of());
        String certificatedKey = "certificated";
        // when
        FamilyApply result = FamilyApplyMapper.toFamilyApply(10L, 1L, request, certificatedKey);

        // then
        assertThat(result.getRequesterSubId()).isEqualTo(10L);
        assertThat(result.getFamilyId()).isEqualTo(1L);
        assertThat(result.getApplyType()).isEqualTo(ApplyType.ADD);
        assertThat(certificatedKey).isEqualTo("certificated");
    }

    @Test
    @DisplayName("성공: 타겟 도메인 객체를 생성한다.")
    void toFamilyApplyTargetSuccess() {
        // when
        FamilyApplyTarget result = FamilyApplyMapper.toFamilyApplyTarget(1L, 20L, FamilyRole.CHILD);

        // then
        assertThat(result.getFamilyApplyId()).isEqualTo(1L);
        assertThat(result.getTargetSubId()).isEqualTo(20L);
        assertThat(result.getTargetFamilyRole()).isEqualTo(FamilyRole.CHILD);
    }

    @Test
    @DisplayName("성공: 신규 가족 생성 응답 DTO를 생성한다.")
    void toCreateNewFamilyResponseSuccess() {
        // given
        List<FamilyApplyTarget> targets = List.of(
                FamilyApplyTarget.builder().targetSubId(10L).targetFamilyRole(FamilyRole.OWNER).build()
        );
        Subscription sub = Subscription.builder().id(10L).member(Member.builder().name("방장").build()).build();
        Map<Long, Subscription> subMap = Map.of(10L, sub);
        Map<Long, String> phoneMap = Map.of(10L, "010-0000-0000");

        // when
        CreateNewFamilyResponse response = FamilyApplyMapper.toCreateNewFamilyResponse(
                null, ApplyType.CREATE, targets, subMap, phoneMap);

        // then
        assertThat(response.familyId()).isNull();
        assertThat(response.familyMemberList()).hasSize(1);
        assertThat(response.familyMemberList().get(0).phone()).isEqualTo("010-0000-0000");
    }

    @Test
    @DisplayName("성공: 구성원 추가 신청 응답 DTO를 생성한다.")
    void toAddFamilyMemberResponseSuccess() {
        // given
        List<FamilyApplyTarget> targets = List.of(
                FamilyApplyTarget.builder().targetSubId(20L).targetFamilyRole(FamilyRole.CHILD).build()
        );
        Subscription sub = Subscription.builder().id(20L).member(Member.builder().name("구성원").build()).build();
        Map<Long, Subscription> subMap = Map.of(20L, sub);
        Map<Long, String> phoneMap = Map.of(20L, "010-1111-2222");

        // when
        AddFamilyMemberResponse response = FamilyApplyMapper.toAddFamilyMemberResponse(
                1L, ApplyType.ADD, targets, subMap, phoneMap);

        // then
        assertThat(response.familyId()).isEqualTo(1L);
        assertThat(response.familyMemberList()).hasSize(1);
        assertThat(response.familyMemberList().get(0).name()).isEqualTo("구성원");
    }
}
