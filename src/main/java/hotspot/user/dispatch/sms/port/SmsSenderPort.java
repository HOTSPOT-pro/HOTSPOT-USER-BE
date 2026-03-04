package hotspot.user.dispatch.sms.port;

public interface SmsSenderPort {

    // 수신번호와 메시지 본문으로 SMS 발송을 수행한다.
    void send(String to, String message);
}
