package hotspot.user.ex.infrastructure;

import org.springframework.stereotype.Repository;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.ExErrorCode;
import hotspot.user.ex.domain.Ex;
import hotspot.user.ex.infrastructure.entity.ExEntity;
import hotspot.user.ex.service.port.ExRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ExRepositoryImpl implements ExRepository {

  private final ExJpaRepository exJpaRepository;

  @Override
  public Ex findById(long exId) {
    return exJpaRepository
        .findById(exId)
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
