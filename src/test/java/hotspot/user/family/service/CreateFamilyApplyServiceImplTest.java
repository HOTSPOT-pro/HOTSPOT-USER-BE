package hotspot.user.family.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.FamilyErrorCode;
import hotspot.user.family.controller.request.CreateFamilyApplyRequest;
import hotspot.user.family.controller.response.CreateFamilyApplyResponse;
import hotspot.user.family.domain.ApplyType;
import hotspot.user.family.domain.Family;
import hotspot.user.family.domain.FamilyApply;
import hotspot.user.family.domain.FamilySubscription;
import hotspot.user.family.service.port.FamilyApplyRepository;
import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.subscription.service.port.SubscriptionRepository;

@ExtendWith(MockitoExtension.class)
class CreateFamilyApplyServiceImplTest {

    @InjectMocks
    private CreateFamilyApplyServiceImpl createFamilyApplyService;

    @Mock
    private FamilyApplyRepository familyApplyRepository;
    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private FamilySubscriptionRepository familySubscriptionRepository;

    @Test
    @DisplayName("성공: 유효한 추가(ADD) 신청을 생성한다")
    void manageAddSuccess() {
        // given
        Long memberId = 1L;
        Long familyId = 100L;
        CreateFamilyApplyRequest request = new CreateFamilyApplyRequest(
                2L, ApplyType.ADD, FamilyRole.CHILD, "http://doc.url");

        given(subscriptionRepository.findByMemberId(memberId))
                .willReturn(Optional.of(Subscription.builder().id(10L).build()));
        given(subscriptionRepository.findById(2L))
                .willReturn(Optional.of(Subscription.builder().id(2L).build()));
        given(familySubscriptionRepository.findBySubId(2L)).willReturn(Optional.empty());
        given(familyApplyRepository.save(any(FamilyApply.class)))
                .willReturn(FamilyApply.builder().requesterSubId(10L).targetSubId(2L).build());

        // when
        CreateFamilyApplyResponse response = createFamilyApplyService.manage(
                memberId, familyId, FamilyRole.OWNER, request);

        // then
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("성공: 유효한 삭제(REMOVE) 신청을 생성한다")
    void manageRemoveSuccess() {
        // given
        Long memberId = 1L;
        Long familyId = 100L;
        CreateFamilyApplyRequest request = new CreateFamilyApplyRequest(
                2L, ApplyType.REMOVE, null, null);

        given(subscriptionRepository.findByMemberId(memberId))
                .willReturn(Optional.of(Subscription.builder().id(10L).build()));
        given(subscriptionRepository.findById(2L))
                .willReturn(Optional.of(Subscription.builder().id(2L).build()));

        Family family = Family.builder().id(familyId).build();
        FamilySubscription targetFamilySub = FamilySubscription.builder().family(family).build();
        given(familySubscriptionRepository.findBySubId(2L)).willReturn(Optional.of(targetFamilySub));
        given(familyApplyRepository.save(any(FamilyApply.class))).willReturn(FamilyApply.builder().build());

        // when
        CreateFamilyApplyResponse response = createFamilyApplyService.manage(
                memberId, familyId, FamilyRole.OWNER, request);

        // then
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("실패: OWNER 권한이 아니면 예외가 발생한다")
    void manageFailByRole() {
        CreateFamilyApplyRequest request = new CreateFamilyApplyRequest(
                2L, ApplyType.ADD, FamilyRole.CHILD, "url");
        assertThatThrownBy(() -> createFamilyApplyService.manage(1L, 100L, FamilyRole.CHILD, request))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(FamilyErrorCode.ONLY_OWNER_CAN_MANAGE.getMessage());
    }

    @Test
    @DisplayName("실패: ADD 신청 시 서류 URL이 없으면 예외가 발생한다")
    void manageAddFailByNoDoc() {
        Long memberId = 1L;
        Long familyId = 100L;
        CreateFamilyApplyRequest request = new CreateFamilyApplyRequest(
                2L, ApplyType.ADD, FamilyRole.CHILD, "");

        given(subscriptionRepository.findByMemberId(memberId))
                .willReturn(Optional.of(Subscription.builder().id(10L).build()));
        given(subscriptionRepository.findById(2L))
                .willReturn(Optional.of(Subscription.builder().id(2L).build()));
        given(familySubscriptionRepository.findBySubId(2L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> createFamilyApplyService.manage(memberId, familyId, FamilyRole.OWNER, request))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(FamilyErrorCode.DOC_URL_REQUIRED.getMessage());
    }
}
