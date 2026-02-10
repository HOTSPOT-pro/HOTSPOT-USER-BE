package hotspot.user.ex.service;

import org.springframework.stereotype.Service;

import hotspot.user.ex.controller.port.SaveExService;
import hotspot.user.ex.controller.request.CreateExRequest;
import hotspot.user.ex.domain.Ex;
import hotspot.user.ex.service.port.ExRepository;
import lombok.RequiredArgsConstructor;

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
