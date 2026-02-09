package hotspot.user.common.ex.service.port;

import hotspot.user.common.ex.domain.Ex;

public interface ExRepository {

    Ex findById(long exId);

    void save(Ex ex);

    void update(Ex updatedEx);
}
