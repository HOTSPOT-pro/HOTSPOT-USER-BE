package hotspot.user.common.ex.domain.mapper;

import hotspot.user.common.ex.controller.response.ExResponse;
import hotspot.user.common.ex.domain.Ex;
import org.springframework.stereotype.Component;

@Component
public class ExResponseMapper {

    public static ExResponse toExResponse(Ex ex) {
        return new ExResponse(
                ex.getExId(),
                ex.getExName(),
                ex.getExDescription()
        );
    }
}
