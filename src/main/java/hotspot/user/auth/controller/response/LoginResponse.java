package hotspot.user.auth.controller.response;

import hotspot.user.member.domain.FamilyRole;
import hotspot.user.member.domain.Status;

/**
 * 소셜 로그인/회원가입 결과 DTO
 * 서비스 레이어에서 인증 객체 생성을 위해 필요한 정보를 담는다.
 * @param memberId    회원 ID
 * @param email       회원 이메일
 * @param status      회원 상태 (PENDING, APPROVED 등)
 * @param familyRole  회원 역할 (CHILD, PARENT 등)
 */
public record LoginResponse(
        Long memberId,
        String email,
        Status status,
        FamilyRole familyRole,
        Long familyId // 가족 ID 추가
) {}
