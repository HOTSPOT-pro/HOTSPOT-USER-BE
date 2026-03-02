package hotspot.user.family.domain.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import hotspot.user.family.controller.response.RemoveFamilyMemberResponse;
import hotspot.user.family.domain.ApplyStatus;
import hotspot.user.family.domain.ApplyType;
import hotspot.user.family.domain.DeleteStatus;
import hotspot.user.family.domain.FamilyApply;
import hotspot.user.family.domain.FamilyRemoveSchedule;

class RemoveFamilyMemberMapperTest {

    @Test
    @DisplayName("성공: 삭제 신청서(FamilyApply) 도메인으로 변환한다.")
    void toFamilyApplySuccess() {
        // when
        FamilyApply result = RemoveFamilyMemberMapper.toFamilyApply(10L, 1L);

        // then
        assertThat(result.getRequesterSubId()).isEqualTo(10L);
        assertThat(result.getApplyType()).isEqualTo(ApplyType.REMOVE);
        assertThat(result.getStatus()).isEqualTo(ApplyStatus.PENDING);
    }

    @Test
    @DisplayName("성공: 개별 삭제 스케줄 도메인으로 변환한다.")
    void toFamilyRemoveScheduleSuccess() {
        LocalDate date = LocalDate.now();
        // when
        FamilyRemoveSchedule result = RemoveFamilyMemberMapper.toFamilyRemoveSchedule(1L, 20L, date);

        // then
        assertThat(result.getFamilyId()).isEqualTo(1L);
        assertThat(result.getTargetSubId()).isEqualTo(20L);
        assertThat(result.getScheduleDate()).isEqualTo(date);
        assertThat(result.getStatus()).isEqualTo(DeleteStatus.SCHEDULED);
    }

    @Test
    @DisplayName("성공: 삭제 스케줄 리스트를 최종 응답 DTO로 변환한다.")
    void toRemoveFamilyMemberResponseSuccess() {
        // given
        FamilyApply apply = FamilyApply.builder().id(100L).familyId(1L).build();
        FamilyRemoveSchedule schedule = FamilyRemoveSchedule.builder()
                .targetSubId(20L).scheduleDate(LocalDate.now()).status(DeleteStatus.SCHEDULED).build();

        // when
        RemoveFamilyMemberResponse response = RemoveFamilyMemberMapper
                .toRemoveFamilyMemberResponse(apply, List.of(schedule));

        // then
        assertThat(response.familyApplyId()).isEqualTo(100L);
        assertThat(response.subIdList()).containsExactly(20L);
        assertThat(response.status()).isEqualTo(DeleteStatus.SCHEDULED);
    }
}
