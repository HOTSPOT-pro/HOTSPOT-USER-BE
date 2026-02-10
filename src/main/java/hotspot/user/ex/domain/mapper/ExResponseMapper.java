package hotspot.user.ex.domain.mapper;

import org.springframework.stereotype.Component;

import hotspot.user.ex.controller.response.ExResponse;
import hotspot.user.ex.domain.Ex;

@Component
public class ExResponseMapper {

  public static ExResponse toExResponse(Ex ex) {
    return new ExResponse(ex.getExId(), ex.getExName(), ex.getExDescription());
  }
}
