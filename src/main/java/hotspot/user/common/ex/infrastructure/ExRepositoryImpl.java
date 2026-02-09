package hotspot.user.common.ex.infrastructure;

import hotspot.user.common.ex.domain.Ex;
import hotspot.user.common.ex.infrastructure.entity.ExEntity;
import hotspot.user.common.ex.service.port.ExRepository;
import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.ExErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ExRepositoryImpl implements ExRepository {

    private final ExJpaRepository exJpaRepository;

    @Override
    public Ex findById(long exId) {
        return exJpaRepository.findById(exId)
                .map(ExEntity::entityToDomain)
                .orElseThrow(() -> new ApplicationException(ExErrorCode.EX_NOT_FOUND));
    }

    @Override
    public void save(Ex ex) {
        exJpaRepository.save(ExEntity.domainToEntity(ex));
    }

    @Override
    public void update(Ex updatedEx) {
        exJpaRepository.save(ExEntity.domainToEntity(updatedEx));
    }
}

