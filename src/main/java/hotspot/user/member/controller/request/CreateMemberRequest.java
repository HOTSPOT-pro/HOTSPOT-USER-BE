package hotspot.user.member.controller.request;

/**
 * 회원 생성 DTO
 */
public record CreateMemberRequest(
    String name,
    String email,
    String phone,
    String birth
) {}
