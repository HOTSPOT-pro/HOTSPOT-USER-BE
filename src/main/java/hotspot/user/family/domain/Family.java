package hotspot.user.family.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 가족 도메인
 */
@Getter
@Builder
@AllArgsConstructor
public class Family {
    private final Long id;
    private final int familyNum;
    private final int familyDataAmount;
    private final PriorityType priorityType;
}
