package com.fonepay.devportal.common.config;

import javax.sql.DataSource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.zaxxer.hikari.HikariDataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {
        "com.fonepay.devportal.modules.user.repository",
        "com.fonepay.devportal.modules.auth.repository",
        "com.fonepay.devportal.modules.department.repository",
        "com.fonepay.devportal.modules.admin.developer.repository",
        "com.fonepay.devportal.modules.developer.repository",
        "com.fonepay.devportal.modules.notification.repository"
})
public class JpaConfig {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.app")
    public DataSourceProperties appDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.app.hikari")
    public DataSource dataSource(DataSourceProperties appDataSourceProperties) {
        return appDataSourceProperties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }
}
