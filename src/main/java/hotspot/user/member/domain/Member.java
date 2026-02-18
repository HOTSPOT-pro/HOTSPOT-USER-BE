package hotspot.user.member.domain;

import hotspot.user.member.controller.request.CreateMemberRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Member {
    private final Long id; // DB 저장 전에는 null, 저장 후 ID 할당됨
    private final String name;
    private final String birth;
    private final Status status;

    public static Member create(CreateMemberRequest request) {
        return Member.builder()
                .name(request.name())
                .birth(request.birth())
                .status(Status.PENDING)
                .build();
    }

    public Member onboard(String birth) {
        return Member.builder()
                .id(this.id)
                .name(this.name)
                .birth(birth)
                .status(Status.APPROVED)
                .build();
    }
}
