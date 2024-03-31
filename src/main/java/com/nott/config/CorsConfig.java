package com.nott.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {
    @Bean
    public FilterRegistrationBean<CorsFilter>
    filterFilterRegistrationBean() {
        UrlBasedCorsConfigurationSource configSource =
                new UrlBasedCorsConfigurationSource();

        CorsConfiguration corsConfiguration =
                new CorsConfiguration();

        corsConfiguration.addAllowedOrigin("*");
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.setAllowCredentials(true);

        configSource.registerCorsConfiguration("/**",
                corsConfiguration);
        FilterRegistrationBean<CorsFilter> fBean =
                new FilterRegistrationBean<>(
                        new CorsFilter(configSource));

        fBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return fBean;
    }
}
