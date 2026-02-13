package hotspot.user.member.domain;

import java.util.List;

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
    private SocialAccount socialAccount; // email 존재 [To-Do] List<SocialAccount> 가지도록 변경 필요
    // private Subscription subscription; [To-Do] subscription 패키지 생성 (phone, role 존재)

    public static Member create(CreateMemberRequest request) {
        return Member.builder()
                .name(request.name())
                .birth(request.birth())
                .status(Status.PENDING)
                .build();
    }
}
