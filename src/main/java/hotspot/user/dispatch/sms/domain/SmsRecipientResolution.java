package hotspot.user.dispatch.sms.domain;

public record SmsRecipientResolution(
        SmsRecipientResolutionStatus status,
        String phoneNumber
) {
    // 수신자 전화번호를 정상적으로 찾은 상태를 생성한다.
    public static SmsRecipientResolution found(String phoneNumber) {
        return new SmsRecipientResolution(SmsRecipientResolutionStatus.FOUND, phoneNumber);
    }

    // 구독 정보가 없어 수신자를 찾지 못한 상태를 생성한다.
    public static SmsRecipientResolution subscriptionNotFound() {
        return new SmsRecipientResolution(SmsRecipientResolutionStatus.SUBSCRIPTION_NOT_FOUND, null);
    }

    // 수신자 전화번호가 비어 있는 상태를 생성한다.
    public static SmsRecipientResolution phoneMissing() {
        return new SmsRecipientResolution(SmsRecipientResolutionStatus.PHONE_MISSING, null);
    }

    // 전화번호 복호화에 실패한 상태를 생성한다.
    public static SmsRecipientResolution decryptFailed() {
        return new SmsRecipientResolution(SmsRecipientResolutionStatus.DECRYPT_FAILED, null);
    }
}
