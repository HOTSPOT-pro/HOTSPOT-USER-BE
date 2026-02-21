package hotspot.user.family.domain;

import hotspot.user.common.constant.FamilyConstant;
import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.FamilyErrorCode;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.subscription.domain.Subscription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 가족-회선 매핑 도메인
 */
@Getter
@Builder
@AllArgsConstructor
public class FamilySubscription {
    private Long id;
    private Subscription subscription;
    private Family family;
    private FamilyRole familyRole;
    private int priority;
    private int dataLimit;

    // 데이터 한도 업데이트
    public void updateDataLimit(int dataLimit) {
        if (dataLimit < FamilyConstant.UNLIMITED_DATA_LIMIT) {
            throw new ApplicationException(
                FamilyErrorCode.INVALID_DATA_LIMIT
            );
        }
        this.dataLimit = dataLimit;
    }
}
