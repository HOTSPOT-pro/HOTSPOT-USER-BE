package hotspot.user.usage.familyUsage.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FamilyUsageTest {

    @Test
    @DisplayName("가족 전체 사용량 계산을 정상 수행한다")
    void shouldCalculateFamilyUsage() {

        double limitKb = 20971520;   // 20GB
        double usedKb = 5242880;     // 5GB

        FamilySubUsage subUsage =
                new FamilySubUsage(10485760, 3145728); // 10GB / 3GB

        FamilyUsage usage =
                new FamilyUsage(limitKb, usedKb, Map.of(1L, subUsage));

        assertEquals(20.0, usage.familyLimitGb());
        assertEquals(5.0, usage.familyUsedGb());
        assertEquals(15.0, usage.familyRemainGb());
        assertEquals(25, usage.familyUsagePercent());
    }

    @Test
    @DisplayName("존재하지 않는 subId는 zero 객체를 반환한다")
    void shouldReturnZeroWhenSubNotExists() {

        FamilyUsage usage =
                new FamilyUsage(0, 0, Map.of());

        FamilySubUsage subUsage = usage.getSubOrZero(999L);

        assertEquals(0.0, subUsage.limitGb());
        assertEquals(0.0, subUsage.familyUsedGb());
    }
}
