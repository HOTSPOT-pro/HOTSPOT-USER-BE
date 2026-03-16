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
import org.springframework.context.annotation.Configuration;
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
        basePackages = {
                "hotspot.user.family.infrastructure",
                "hotspot.user.familyReport.infrastructure",
                "hotspot.user.member.infrastructure",
                "hotspot.user.notification.infrastructure",
                "hotspot.user.outbox.consistencyOutbox.infrastructure",
                "hotspot.user.outbox.notificationOutbox.infrastructure",
                "hotspot.user.plan.infrastructure",
                "hotspot.user.policy.infrastructure",
                "hotspot.user.presentData.infrastructure",
                "hotspot.user.subscription.infrastructure"
                // weeklyReport는 BatchDbConfig에서 관리하므로 제외
                // auth.infrastructure는 Redis용 TokenCrudRepository가 있어 제외 (필요 시 JpaRepository만 따로 뺌)
        },
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
    @ConfigurationProperties(prefix = "spring.datasource.main") // postgres.yml의 메인 DB 설정
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
                        "hotspot.user.common",
                        "hotspot.user.auth.infrastructure.entity",
                        "hotspot.user.family.infrastructure.entity",
                        "hotspot.user.familyReport.infrastructure.entity",
                        "hotspot.user.member.infrastructure.entity",
                        "hotspot.user.notification.infrastructure.entity",
                        "hotspot.user.outbox.consistencyOutbox.infrastructure.entity",
                        "hotspot.user.outbox.notificationOutbox.infrastructure.entity",
                        "hotspot.user.plan.infrastructure.entity",
                        "hotspot.user.policy.infrastructure.entity",
                        "hotspot.user.presentData.infrastructure.entity",
                        "hotspot.user.subscription.infrastructure.entity"
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
