package com.example.library.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Swagger - acces liber
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/users").permitAll()

                        // catalog de carți, cautare: toti
                        .requestMatchers(HttpMethod.GET, "/api/books/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()

                        // doar LIBRARIAN + ADMIN pot adăuga carti
                        .requestMatchers(HttpMethod.POST, "/api/books/**").hasAnyRole("LIBRARIAN", "ADMIN")

                        .requestMatchers(HttpMethod.PUT, "/api/books/**").hasAnyRole("LIBRARIAN", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/books/**").hasAnyRole("LIBRARIAN", "ADMIN")

                        // categorii - le poate administra LIBRARIAN + ADMIN
                        .requestMatchers("/api/categories/**").hasAnyRole("LIBRARIAN", "ADMIN")

                        // user management (listare, dezactivare, etc.) - doar ADMIN
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/users/**").hasRole("ADMIN")
                        // USER NEAUTENTIFICAT PENTRU POST
                        .requestMatchers(HttpMethod.POST, "/api/users/**").anonymous()

                        // orice user autentificat poate cere imprumut/vedea istoricul propriu
                        .requestMatchers("/api/loans/**").authenticated()

                        // notificari: orice user autentificat
                        .requestMatchers("/api/notifications/**").authenticated()

                        // rapoarte: doar ADMIN
                        .requestMatchers("/api/reports/**").hasRole("ADMIN")

                        // orice altceva -> autentificat
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
