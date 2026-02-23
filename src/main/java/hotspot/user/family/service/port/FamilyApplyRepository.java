package hotspot.user.family.service.port;


import hotspot.user.family.domain.FamilyApply;

/**
 * 가족 구성원 추가 / 삭제 신청 레포지토리
 */
public interface FamilyApplyRepository {
    FamilyApply save(FamilyApply familyApply);
}
