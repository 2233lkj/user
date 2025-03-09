package com.springboot.logindemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class securityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // 禁用CSRF保护，适用于开发和测试
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/user/sendMsg", "/api/user/register", "/api/user/login/password",
                                "/api/user/login/verify", "/api/user/changePassword",
                                "/api/user/admin/updateLoginPermission", "/api/user/delete", "/api/user/update")
                        .permitAll() // 放行指定路径
                        .anyRequest().authenticated() // 所有其他请求需要认证
                );
        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
