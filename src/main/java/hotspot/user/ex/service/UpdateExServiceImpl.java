package hotspot.user.ex.service;

import org.springframework.stereotype.Service;

import hotspot.user.ex.controller.port.UpdateExService;
import hotspot.user.ex.controller.request.UpdateExRequest;
import hotspot.user.ex.domain.Ex;
import hotspot.user.ex.service.port.ExRepository;
import lombok.RequiredArgsConstructor;

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
