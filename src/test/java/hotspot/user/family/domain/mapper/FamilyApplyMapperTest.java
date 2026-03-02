package hotspot.user.family.domain.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import hotspot.user.family.controller.response.AddFamilyMemberResponse;
import hotspot.user.family.domain.ApplyType;
import hotspot.user.family.domain.FamilyApplyTarget;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.member.domain.Member;
import hotspot.user.subscription.domain.Subscription;

class FamilyApplyMapperTest {

    @Test
    @DisplayName("성공: 도메인 객체 리스트와 전화번호 맵을 이용해 최종 응답 DTO를 생성한다.")
    void toAddFamilyMemberResponseSuccess() {
        // given
        Long familyId = 1L;
        List<FamilyApplyTarget> targets = List.of(
                FamilyApplyTarget.builder().targetSubId(10L).targetFamilyRole(FamilyRole.CHILD).build()
        );

        Subscription sub = Subscription.builder()
                .id(10L)
                .member(Member.builder().name("홍길동").build())
                .build();

        Map<Long, Subscription> subscriptionMap = Map.of(10L, sub);
        Map<Long, String> subIdToPhoneMap = Map.of(10L, "010-1234-5678");

        // when
        AddFamilyMemberResponse response = FamilyApplyMapper.toAddFamilyMemberResponse(
                familyId, ApplyType.ADD, targets, subscriptionMap, subIdToPhoneMap);

        // then
        assertThat(response.familyId()).isEqualTo(familyId);
        assertThat(response.familyMemberList()).hasSize(1);
        assertThat(response.familyMemberList().get(0).name()).isEqualTo("홍길동");
        assertThat(response.familyMemberList().get(0).phone()).isEqualTo("010-1234-5678");
        assertThat(response.familyMemberList().get(0).targetFamilyRole()).isEqualTo(FamilyRole.CHILD);
    }
}
