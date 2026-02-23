package hotspot.user.family.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.FamilyErrorCode;
import hotspot.user.family.controller.response.FamilyInfoResponse;
import hotspot.user.family.domain.FamilyDetailInfo;
import hotspot.user.family.service.port.FamilyRepository;
import hotspot.user.member.domain.Member;
import hotspot.user.member.domain.MemberDetailInfo;
import hotspot.user.member.domain.Status;

@ExtendWith(MockitoExtension.class)
class FindFamilyInfoServiceImplTest {

    @Mock
    private FamilyRepository familyRepository;

    @InjectMocks
    private FindFamilyInfoServiceImpl findFamilyInfoService;

    @Test
    @DisplayName("성공: 가족 ID로 가족 및 구성원 전체 정보를 조회한다")
    void findFamilyInfoByIdSuccess() {
        // given
        Long familyId = 1L;
        Member member = Member.builder().id(10L).name("멤버1").status(Status.APPROVED).build();
        MemberDetailInfo memberInfo = MemberDetailInfo.builder()
                .member(member)
                .email("test@test.com")
                .build();

        FamilyDetailInfo detailInfo = FamilyDetailInfo.builder()
                .familyId(familyId)
                .familyNum(1)
                .memberDetailInfoList(List.of(memberInfo))
                .build();

        given(familyRepository.findInfoById(familyId)).willReturn(Optional.of(detailInfo));

        // when
        FamilyInfoResponse response = findFamilyInfoService.findFamilyInfoById(familyId);

        // then
        assertThat(response.familyId()).isEqualTo(familyId);
        assertThat(response.familyNum()).isEqualTo(1);
        assertThat(response.memberInfoList()).hasSize(1);
        assertThat(response.memberInfoList().get(0).email()).isEqualTo("test@test.com");
    }

    @Test
    @DisplayName("실패: 존재하지 않는 가족 ID로 조회 시 예외가 발생한다")
    void findFamilyInfoByIdFail() {
        // given
        Long familyId = 999L;
        given(familyRepository.findInfoById(familyId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> findFamilyInfoService.findFamilyInfoById(familyId))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(FamilyErrorCode.FAMILY_NOT_FOUND.getMessage());
    }
}
