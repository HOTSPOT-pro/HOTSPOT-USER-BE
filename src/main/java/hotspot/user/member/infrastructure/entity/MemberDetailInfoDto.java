package hotspot.user.member.infrastructure.entity;

import hotspot.user.member.domain.FamilyRole;
import lombok.Builder;

/**
 * 회원 상세 정보 조회를 위한 통합 도메인 모델
 */
@Builder
public record MemberDetailInfoDto(
        MemberEntity memberEntity,
        String email,
        String phone,
        Long subId,
        FamilyRole role,
        Long familyId
) { }
