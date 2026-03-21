package hotspot.user.plan.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 요금제 도메인
 */
@Getter
@Builder
@AllArgsConstructor
public class Plan {
    private final Long id; // DB 저장 전에는 null, 저장 후 ID 할당됨
    private final String name;
    private final Long dataAmount;
    private final DataPeriod dataPeriod;
}
