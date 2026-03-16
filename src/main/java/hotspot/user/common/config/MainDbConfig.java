package hotspot.user.common.config;

import java.util.Map;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * 메인 DB 설정
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "hotspot", // hotspot 하위를 전부 스캔하되
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "hotspot\\.user\\.weeklyReport\\..*" // weeklyReport 패키지는 스캔에서 제외
        ),
        entityManagerFactoryRef = "mainEntityManagerFactory",
        transactionManagerRef = "mainTransactionManager"
)
public class MainDbConfig {

    private final JpaProperties jpaProperties;
    private final HibernateProperties hibernateProperties;

    public MainDbConfig(JpaProperties jpaProperties, HibernateProperties hibernateProperties) {
        this.jpaProperties = jpaProperties;
        this.hibernateProperties = hibernateProperties;
    }

    @Primary
    @Bean(name = "mainDataSource")
    @ConfigurationProperties(prefix = "spring.datasource") // postgres.yml의 기본 DB 설정
    public DataSource mainDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = "mainEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean mainEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("mainDataSource") DataSource dataSource) {

        Map<String, Object> properties = hibernateProperties.determineHibernateProperties(
                jpaProperties.getProperties(), new HibernateSettings());

        return builder
                .dataSource(dataSource)
                .packages(
                        // 실제 프로젝트의 weeklyReport를 제외한 나머지 메인 도메인 패키지
                        "hotspot.user.auth",
                        "hotspot.user.common",
                        "hotspot.user.dispatch",
                        "hotspot.user.family",
                        "hotspot.user.familyReport",
                        "hotspot.user.kafka",
                        "hotspot.user.member",
                        "hotspot.user.notification",
                        "hotspot.user.outbox",
                        "hotspot.user.plan",
                        "hotspot.user.policy",
                        "hotspot.user.presentData",
                        "hotspot.user.s3",
                        "hotspot.user.subscription",
                        "hotspot.user.usage"
                )
                .persistenceUnit("main")
                .properties(properties)
                .build();
    }

    @Primary
    @Bean(name = "mainTransactionManager")
    public PlatformTransactionManager mainTransactionManager(
            @Qualifier("mainEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
