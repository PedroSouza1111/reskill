package br.com.fiap.reskill.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .authorizeHttpRequests(authorize -> authorize

                                                .requestMatchers("/", "/login", "/css/**", "/js/**", "/h2-console/**")
                                                .permitAll()

                                                .anyRequest()
                                                .authenticated())

                                .oauth2Login(oauth -> oauth

                                                .loginPage("/login")

                                                .defaultSuccessUrl("/meu-perfil", true))
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/login?logout")
                                                .permitAll())

                                .csrf(csrf -> csrf.disable())
                                .headers(headers -> headers.frameOptions(frame -> frame.disable()));

                return http.build();
        }

}