package hotspot.user.common.crpyto;

public record SubscriptionKeyInfo(
        String encryptedDek,
        String kekKeyId
) {
}
