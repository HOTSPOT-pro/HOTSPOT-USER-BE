package hotspot.user.ex.controller.port;

import hotspot.user.ex.controller.response.ExResponse;

public interface FindExService {

  ExResponse findEx(Long exId);
}
