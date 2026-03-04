package hotspot.user.dispatch.sms.domain;

public enum SmsRecipientResolutionStatus {
    FOUND,
    SUBSCRIPTION_NOT_FOUND,
    PHONE_MISSING,
    DECRYPT_FAILED
}
