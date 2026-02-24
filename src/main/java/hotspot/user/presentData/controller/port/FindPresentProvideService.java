package hotspot.user.presentData.controller.port;

import hotspot.user.presentData.controller.response.PresentDataResponse;

public interface FindPresentProvideService {

    PresentDataResponse findPresentProvide(Long memberId);
}
