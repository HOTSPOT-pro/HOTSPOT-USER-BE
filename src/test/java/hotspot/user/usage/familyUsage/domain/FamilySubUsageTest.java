package hotspot.user.usage.familyUsage.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FamilySubUsageTest {

    @Test
    @DisplayName("남은 사용량과 퍼센트를 정상 계산한다")
    void shouldCalculateRemainAndPercent() {

        // 10GB limit, 4GB used
        double limitKb = 10485760;     // 10GB
        double usedKb = 4194304;       // 4GB

        FamilySubUsage usage = new FamilySubUsage(limitKb, usedKb);

        assertEquals(6.0, usage.remainGb());
        assertEquals(10.0, usage.limitGb());
        assertEquals(4.0, usage.familyUsedGb());
        assertEquals(40, usage.usagePercent());
    }

    @Test
    @DisplayName("존재하지 않는 경우 zero 객체를 반환한다")
    void shouldReturnZeroInstance() {

        FamilySubUsage zero = FamilySubUsage.zero();

        assertEquals(0.0, zero.limitGb());
        assertEquals(0.0, zero.familyUsedGb());
        assertEquals(0.0, zero.remainGb());
        assertEquals(0, zero.usagePercent());
    }
}
