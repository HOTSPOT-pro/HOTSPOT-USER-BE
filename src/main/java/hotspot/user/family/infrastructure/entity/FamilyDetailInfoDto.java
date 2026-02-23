package hotspot.user.family.infrastructure.entity;

import hotspot.user.member.domain.FamilyRole;
import hotspot.user.member.infrastructure.entity.MemberEntity;

/**
 * 가족 전체 구성원 조회를 위한 JPA Projection용 DTO (Infrastructure 계층 전용)
 */
public record FamilyDetailInfoDto(
        FamilyEntity familyEntity,
        MemberEntity memberEntity,
        String email,
        String phone,
        Long subId,
        FamilyRole role
) {
}
