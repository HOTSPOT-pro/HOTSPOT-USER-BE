package hotspot.user.member.domain;

import hotspot.user.member.infrastructure.entity.MemberEntity;
import lombok.Builder;
import lombok.Getter;

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
