package hotspot.user.presentData.controller.port;

import hotspot.user.presentData.controller.request.SendPresentDataRequest;
import hotspot.user.presentData.controller.response.SendPresentDataResponse;

public interface SendPresentDataService {
    SendPresentDataResponse sendPresentData(Long memberId, SendPresentDataRequest request);
}
