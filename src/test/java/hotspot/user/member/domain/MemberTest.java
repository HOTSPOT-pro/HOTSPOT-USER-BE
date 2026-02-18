package hotspot.user.member.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import hotspot.user.member.controller.request.CreateMemberRequest;

class MemberTest {

    @Test
    @DisplayName("onboard 메서드 호출 시 상태가 APPROVED로 변경되고 생년월일이 업데이트된다")
    void onboardSuccess() {
        // given
        Member member = Member.builder()
                .id(1L)
                .name("홍길동")
                .status(Status.PENDING)
                .build();

        // when
        Member onboardedMember = member.onboard("950101");

        // then
        assertThat(onboardedMember.getStatus()).isEqualTo(Status.APPROVED);
        assertThat(onboardedMember.getBirth()).isEqualTo("950101");
        assertThat(onboardedMember.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("create 정적 팩토리 메서드로 PENDING 상태의 멤버를 생성할 수 있다")
    void createSuccess() {
        // given
        CreateMemberRequest request = new CreateMemberRequest("테스트", "test123@test.com", "010-1234-5678", "980724");

        // when
        Member member = Member.create(request);

        // then
        assertThat(member.getName()).isEqualTo("테스트");
        assertThat(member.getBirth()).isEqualTo("980724");
        assertThat(member.getStatus()).isEqualTo(Status.PENDING);
    }
}
