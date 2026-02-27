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
    private Long id;
    private int familyNum;
    private long familyDataAmount;
    private PriorityType priorityType;

    // 우선순위 정책 타입 업데이트
    public void updatePriorityType(PriorityType priorityType) {
        this.priorityType = priorityType;
    }
}
