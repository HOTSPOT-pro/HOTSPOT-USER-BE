package hotspot.user.familyReport.domain;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

import hotspot.user.family.domain.Family;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class FamilyReport {

    private Long id;
    private Family family;
    private DayOfWeek receiveDay;
    private boolean active;
    private LocalDateTime createdTime;
    private LocalDateTime modifiedTime;

    public void updateReceiveDay(DayOfWeek receiveDay) {
        this.receiveDay = receiveDay;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }
}
