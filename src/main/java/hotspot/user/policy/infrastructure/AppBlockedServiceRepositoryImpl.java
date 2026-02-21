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
        return appBlockedServiceJpaRepository.findAll().stream()
                .map(AppBlockedServiceEntity::entityToDomain)
                .toList();
    }

    @Override
    public long countByIdIn(Set<Long> ids) {
        return appBlockedServiceJpaRepository.countByAppBlockedServiceIdIn(ids);
    }
}
