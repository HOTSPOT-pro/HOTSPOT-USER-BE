package hotspot.user.family.domain;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FamilyRemoveSchedule {
    private Long id;
    private Long targetSubId;
    private Long familyId;
    private DeleteStatus status;
    private LocalDate scheduleDate;
}
