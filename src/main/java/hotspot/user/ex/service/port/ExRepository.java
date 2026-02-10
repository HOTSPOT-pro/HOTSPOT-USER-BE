package hotspot.user.ex.service.port;

import hotspot.user.ex.domain.Ex;

public interface ExRepository {

  Ex findById(long exId);

  void save(Ex ex);

  void update(Ex updatedEx);
}
