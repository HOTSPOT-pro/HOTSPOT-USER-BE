package hotspot.user.usage.totalUsage.repository.schema;

public record TotalUsage(

        double totalDataAmount,
        double totalDataRemainAmount,
        int totalDataRemainPercent,

        double subDataRemainAmount,
        double giftDataRemainAmount,
        double familyDataRemainAmount
) {
}
