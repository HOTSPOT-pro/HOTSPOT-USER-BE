package hotspot.user.presentData.infrastructure.mapper;

import hotspot.user.presentData.domain.SubUsage;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class FamilySubUsageMapper {

    public Map<Long, SubUsage> map(
            List<Long> subIds,
            List<Object> results
    ) {

        Map<Long, SubUsage> resultMap = new HashMap<>();

        int index = 0;

        for (Long subId : subIds) {

            double usedKb = toKb(results.get(index++));
            double limitKb = toKb(results.get(index++));

            resultMap.put(subId, new SubUsage(usedKb, limitKb));
        }

        return resultMap;
    }

    private double toKb(Object value) {
        if (value == null) return 0;

        if (value instanceof byte[] bytes) {
            return Double.parseDouble(new String(bytes));
        }

        if (value instanceof String str) {
            return Double.parseDouble(str);
        }

        return 0;
    }
}