package com.example.sdhucanteendrone.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 关掉 CSRF（方便 Postman、前后端分离）
                .csrf(csrf -> csrf.disable())
                // 配置权限规则
                .authorizeHttpRequests(auth -> auth
                        // 你现在的用户接口全部放行
                        .requestMatchers("/api/**").permitAll()
                        // 其它也先全部放行（开发阶段）
                        .anyRequest().permitAll()
                )
                // 关闭默认登录表单、httpBasic 等（可选）
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        return http.build();
    }
}
