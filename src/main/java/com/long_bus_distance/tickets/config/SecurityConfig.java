package com.long_bus_distance.tickets.config;

import com.long_bus_distance.tickets.repository.UserProvisioningFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           UserProvisioningFilter userProvisioningFilter) throws Exception{
        http
                .authorizeHttpRequests(authorize -> authorize
                .anyRequest().authenticated()) //dam bao tat cac yeu cu HTTP deu phai xac thuc
                //vo hieu hoa co de Cross-Site Request Forgery cho trien khai REST API
                .csrf(csrf -> csrf.disable())
                //vi thiet ke REST API nen STATELESS
                .sessionManagement((session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                )
                //Xac thuc bang OAuth2 va JWT
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(
                                Customizer.withDefaults()
                        ))
                //Dam bao bo loc thuc thi sau Bearer, doi tuong xac thuc da co mat va ng dung duoc xac thuc
                // =>Cho phep bo loc truy cap JWT
                .addFilterAfter(userProvisioningFilter, BasicAuthenticationFilter.class);

        return http.build();
    }
}
