package hotspot.user.presentData.controller.response;

import java.time.LocalDateTime;
import java.util.List;

public record PresentDataResponse(
        double totalReceivedGb,
        List<PresentItemResponse> items
) {
    public record PresentItemResponse(
            Long provideSubId,
            String subName,
            double amountGb,
            LocalDateTime createdTime
    ) {}
}
