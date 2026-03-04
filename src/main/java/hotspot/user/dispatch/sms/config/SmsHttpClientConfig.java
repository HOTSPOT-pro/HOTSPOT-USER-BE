package hotspot.user.dispatch.sms.config;

import java.net.http.HttpClient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SmsHttpClientConfig {

    // SMS 외부 API 호출에 사용할 기본 HttpClient 빈을 등록한다.
    @Bean
    public HttpClient smsHttpClient() {
        return HttpClient.newHttpClient();
    }
}
