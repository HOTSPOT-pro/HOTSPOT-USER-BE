package hotspot.user.common.ex.controller.port;

import hotspot.user.common.ex.controller.response.ExResponse;

public interface FindExService {

    ExResponse findEx(Long exId);
}
