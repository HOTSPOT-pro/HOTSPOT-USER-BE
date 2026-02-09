package hotspot.user.ex.service;

import org.springframework.stereotype.Service;

import hotspot.user.ex.controller.port.FindExService;
import hotspot.user.ex.controller.response.ExResponse;
import hotspot.user.ex.domain.Ex;
import hotspot.user.ex.domain.mapper.ExResponseMapper;
import hotspot.user.ex.service.port.ExRepository;
import lombok.RequiredArgsConstructor;

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
