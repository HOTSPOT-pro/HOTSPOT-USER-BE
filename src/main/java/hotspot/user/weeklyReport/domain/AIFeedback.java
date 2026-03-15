package hotspot.user.weeklyReport.domain;

import java.util.List;

import lombok.Builder;

@Builder
public record AIFeedback(
        FeedbackMessage feedback,
        List<String> keyInsights,
        SummaryText summaryText,
        List<PolicyRecommend> policyRecommendList
) {
    @Builder
    public record FeedbackMessage(String toChild, String toParent) {}

    @Builder
    public record SummaryText(String overall, String daily, String hourly, String category) {}

    @Builder
    public record PolicyRecommend(String title, String description, String reason) {}
}
