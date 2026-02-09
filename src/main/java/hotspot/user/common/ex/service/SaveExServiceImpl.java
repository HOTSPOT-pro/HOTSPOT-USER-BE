package hotspot.user.common.ex.service;

import hotspot.user.common.ex.controller.port.SaveExService;
import hotspot.user.common.ex.controller.request.CreateExRequest;
import hotspot.user.common.ex.domain.Ex;
import hotspot.user.common.ex.service.port.ExRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SaveExServiceImpl implements SaveExService {

    private final ExRepository exRepository;

    @Override
    public void save(CreateExRequest request) {
        Ex ex = Ex.create(request);
        exRepository.save(ex);
    }
}
