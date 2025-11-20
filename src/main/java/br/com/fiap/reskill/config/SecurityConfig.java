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
                        // Permite acesso público à página inicial e à de login
                        // ATENÇÃO: A página /login agora é só um "portal"
                        .requestMatchers("/", "/login", "/css/**", "/js/**", "/h2-console/**")
                        .permitAll()
                        // Todas as outras URLs exigem autenticação
                        .anyRequest()
                        .authenticated())
                // Configuração do OAuth2
                .oauth2Login(oauth -> oauth
                        // Diz ao Spring Security qual é a sua página de "portal de login"
                        .loginPage("/login")
                        // URL para onde o usuário é redirecionado após o login (Google)
                        .defaultSuccessUrl("/meu-perfil", true))
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll())
                // Configurações para o H2 Console (pode manter)
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }

}