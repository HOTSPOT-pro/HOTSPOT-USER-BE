package hotspot.user.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kms.KmsClient;

@Configuration
public class AwsKmsConfig {

    @Bean
    public KmsClient kmsClient(@Value("${AWS_REGION:${aws.region:ap-northeast-2}}") String awsRegion) {
        return KmsClient.builder()
                .region(Region.of(awsRegion))
                .build();
    }
}
