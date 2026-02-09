package hotspot.user.common.ex.controller.port;

import hotspot.user.common.ex.controller.request.UpdateExRequest;

public interface UpdateExService {

    void update(long exId, UpdateExRequest request);
}
