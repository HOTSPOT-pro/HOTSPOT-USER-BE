package hotspot.user.family.domain;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.FamilyErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 가족-회선 매핑 도메인 List 일급 컬렉션 (First Class Collection)
 */
@Getter
@Builder
@AllArgsConstructor
public class FamilySubscriptions {
    private final List<FamilySubscription> subscriptions;

    // FIFO 모드일 때의 일괄 업데이트
    public void updateAllToFifo() {
        subscriptions.forEach(sub -> sub.updatePriority(-1));
    }

    // PRIORITY 모드일 때의 일괄 업데이트 및 검증 로직
    public void updatePriorities(Map<Long, Integer> newPriorityMap) {
        // 1. DTO에 의존하지 않기 위해 Map으로 변환해서 받음

        // 2. 비즈니스 규칙 검증 (도메인 룰 수호)
        validateAllMembersIncluded(newPriorityMap);
        validateNoMinusOne(newPriorityMap);
        validateNoDuplicates(newPriorityMap);
        validateContinuousSequence(newPriorityMap);

        // 3. 실제 업데이트 실행
        subscriptions.forEach(sub -> {
            Integer newPriority = newPriorityMap.get(sub.getSubscription().getId());
            if (newPriority != null) {
                sub.updatePriority(newPriority);
            }
        });
    }

    private void validateAllMembersIncluded(Map<Long, Integer> newPriorityMap) {
        if (newPriorityMap.size() != subscriptions.size()) {
            throw new ApplicationException(FamilyErrorCode.MISSING_PRIORITY_VALUES);
        }
    }

    private void validateNoMinusOne(Map<Long, Integer> newPriorityMap) {
        if (newPriorityMap.containsValue(-1)) {
            throw new ApplicationException(FamilyErrorCode.INVALID_PRIORITY_VALUE);
        }
    }

    private void validateNoDuplicates(Map<Long, Integer> newPriorityMap) {
        long uniqueCount = newPriorityMap.values().stream().distinct().count();
        if (uniqueCount != newPriorityMap.size()) {
            throw new ApplicationException(FamilyErrorCode.DUPLICATE_PRIORITY);
        }
    }

    private void validateContinuousSequence(Map<Long, Integer> newPriorityMap) {
        List<Integer> sortedPriorities = newPriorityMap.values().stream()
                .sorted()
                .toList();

        for (int i = 0; i < sortedPriorities.size(); i++) {
            // 순위는 1부터 시작하고 1씩 증가해야 함 (1, 2, 3...)
            if (sortedPriorities.get(i) != i + 1) {
                throw new ApplicationException(FamilyErrorCode.NOT_CONTINUOUS_PRIORITY);
            }
        }
    }

    public List<FamilySubscription> toList() {
        return Collections.unmodifiableList(subscriptions);
    }
}
