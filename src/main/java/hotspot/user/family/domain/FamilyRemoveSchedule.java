package hotspot.user.family.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class FamilyRemoveSchedule {
    private Long id;
    private Long targetSubId;
    private Long familyId;
    private DeleteStatus status;
    private LocalDate scheduleDate;
}
