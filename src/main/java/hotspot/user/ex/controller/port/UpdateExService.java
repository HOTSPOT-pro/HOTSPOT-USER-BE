package hotspot.user.ex.controller.port;

import hotspot.user.ex.controller.request.UpdateExRequest;

public interface UpdateExService {

  void update(long exId, UpdateExRequest request);
}
