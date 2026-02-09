package hotspot.user.common.ex.service;

import hotspot.user.common.ex.controller.port.UpdateExService;
import hotspot.user.common.ex.controller.request.UpdateExRequest;
import hotspot.user.common.ex.domain.Ex;
import hotspot.user.common.ex.service.port.ExRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateExServiceImpl implements UpdateExService {

    private final ExRepository exRepository;

    @Override
    public void update(long exId, UpdateExRequest request) {
        Ex ex = exRepository.findById(exId);

        ex.update(request);

        exRepository.update(ex);
    }
}
