package hotspot.user.family.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.family.domain.DeleteStatus;
import hotspot.user.family.domain.FamilyRemoveSchedule;
import hotspot.user.family.infrastructure.entity.FamilyRemoveScheduleEntity;

@ExtendWith(MockitoExtension.class)
class FamilyRemoveScheduleRepositoryImplTest {

    @InjectMocks
    private FamilyRemoveScheduleRepositoryImpl familyRemoveScheduleRepository;

    @Mock
    private FamilyRemoveScheduleJpaRepository familyRemoveScheduleJpaRepository;

    @Test
    @DisplayName("성공: 여러 개의 삭제 스케줄을 한꺼번에 저장한다.")
    void saveAllSuccess() {
        // given
        FamilyRemoveSchedule schedule1 = FamilyRemoveSchedule.builder()
                .targetSubId(10L).familyId(1L).scheduleDate(LocalDate.now()).status(DeleteStatus.SCHEDULED).build();
        FamilyRemoveSchedule schedule2 = FamilyRemoveSchedule.builder()
                .targetSubId(11L).familyId(1L).scheduleDate(LocalDate.now()).status(DeleteStatus.SCHEDULED).build();

        given(familyRemoveScheduleJpaRepository.saveAll(anyList())).willReturn(List.of(
                FamilyRemoveScheduleEntity.domainToEntity(schedule1),
                FamilyRemoveScheduleEntity.domainToEntity(schedule2)
        ));

        // when
        List<FamilyRemoveSchedule> result = familyRemoveScheduleRepository.saveAll(List.of(schedule1, schedule2));

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTargetSubId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("성공: 특정 회선 ID 리스트와 상태로 삭제 스케줄을 조회한다.")
    void findAllByTargetSubIdInAndStatusSuccess() {
        // given
        List<Long> subIds = List.of(10L, 11L);
        FamilyRemoveScheduleEntity entity = FamilyRemoveScheduleEntity.builder()
                .targetSubId(10L).familyId(1L).status(DeleteStatus.SCHEDULED).build();

        given(familyRemoveScheduleJpaRepository.findAllByTargetSubIdInAndStatus(subIds, DeleteStatus.SCHEDULED))
                .willReturn(List.of(entity));

        // when
        List<FamilyRemoveSchedule> result = familyRemoveScheduleRepository.findAllByTargetSubIdInAndStatus(subIds, DeleteStatus.SCHEDULED);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTargetSubId()).isEqualTo(10L);
    }
}
