package hotspot.user.common.ex.service;

import hotspot.user.common.ex.controller.port.FindExService;
import hotspot.user.common.ex.controller.response.ExResponse;
import hotspot.user.common.ex.domain.Ex;
import hotspot.user.common.ex.domain.mapper.ExResponseMapper;
import hotspot.user.common.ex.service.port.ExRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FindExServiceImpl implements FindExService {

    private final ExRepository exRepository;

    @Override
    public ExResponse findEx(Long exId) {

        Ex ex = exRepository.findById(exId);
        return ExResponseMapper.toExResponse(ex);
    }
}
