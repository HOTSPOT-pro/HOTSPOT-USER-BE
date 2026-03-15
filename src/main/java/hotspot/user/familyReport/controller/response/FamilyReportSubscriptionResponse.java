package hotspot.user.familyReport.controller.response;

import lombok.Builder;

@Builder
public record FamilyReportSubscriptionResponse(
        boolean subscribed
) {

    public static FamilyReportSubscriptionResponse unsubscribed() {
        return FamilyReportSubscriptionResponse.builder()
                .subscribed(false)
                .build();
    }
}
