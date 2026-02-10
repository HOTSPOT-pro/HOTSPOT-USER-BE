package hotspot.user.ex.controller.port;

import hotspot.user.ex.controller.request.CreateExRequest;

public interface SaveExService {

  void save(CreateExRequest request);
}
