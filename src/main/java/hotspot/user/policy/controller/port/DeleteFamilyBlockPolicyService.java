package hotspot.user.policy.controller.port;


import java.util.List;

/**
 * 가족 정책 삭제 서비스
 */
public interface DeleteFamilyBlockPolicyService {
    void delete(List<Long> policyIdList, Long memberId, Long familyId);
}
