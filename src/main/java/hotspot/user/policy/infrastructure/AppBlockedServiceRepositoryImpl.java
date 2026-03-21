package hotspot.user.policy.infrastructure;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Repository;

import hotspot.user.policy.domain.AppBlockedService;
import hotspot.user.policy.infrastructure.entity.AppBlockedServiceEntity;
import hotspot.user.policy.service.port.AppBlockedServiceRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AppBlockedServiceRepositoryImpl implements AppBlockedServiceRepository {
    private final AppBlockedServiceJpaRepository appBlockedServiceJpaRepository;

    @Override
    public List<AppBlockedService> findAll() {
        return appBlockedServiceJpaRepository.findByIsActiveTrue().stream()
                .map(AppBlockedServiceEntity::entityToDomain)
                .toList();
    }

    @Override
    public long countByIdIn(Set<Long> ids) {
        return appBlockedServiceJpaRepository.countByAppBlockedServiceIdIn(ids);
    }

    // 관리자가 만든 앱 차단 서비스 중 id에 해당하는 걸 리스트로 가져온다.
    @Override
    public List<AppBlockedService> findAllActiveAndInDeleteByAppBlockedServiceIds(List<Long> ids) {
        return appBlockedServiceJpaRepository.findByIdInAndIsActiveTrue(ids)
                .stream()
                .map(AppBlockedServiceEntity::entityToDomain)
                .toList();
    }

    @Override
    public List<AppBlockedService> findAllByAppBlockedServiceIds(List<Long> ids) {
        return appBlockedServiceJpaRepository.findByIdIn(ids)
                .stream()
                .map(AppBlockedServiceEntity::entityToDomain)
                .toList();
    }
}
