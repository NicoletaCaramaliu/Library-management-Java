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


                        // catalog de carți, cautare: toti
                        .requestMatchers(HttpMethod.GET, "/api/books/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/categories/**").authenticated()

                        // doar LIBRARIAN + ADMIN pot adăuga carti
                        .requestMatchers(HttpMethod.POST, "/api/books/**").hasAnyRole("LIBRARIAN", "ADMIN")

                        .requestMatchers(HttpMethod.PUT, "/api/books/**").hasAnyRole("LIBRARIAN", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/books/**").hasAnyRole("LIBRARIAN", "ADMIN")

                        // categorii - le poate administra LIBRARIAN + ADMIN
                        .requestMatchers("/api/categories/**").hasAnyRole("LIBRARIAN", "ADMIN")

                        // user management (listare, dezactivare, etc.) - doar ADMIN
                        .requestMatchers(HttpMethod.GET, "/api/users/me").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/users/me").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/users/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/users/{id}").hasRole("ADMIN")
                        // USER NEAUTENTIFICAT PENTRU POST
                        .requestMatchers(HttpMethod.POST, "/api/users").anonymous()

                        // loans
                        .requestMatchers("/api/loans/borrow/**").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/api/loans").hasAnyRole("LIBRARIAN", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/loans/overdue").hasAnyRole("LIBRARIAN", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/loans/user/**").hasAnyRole("LIBRARIAN", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/loans").hasAnyRole("LIBRARIAN", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/loans/**").hasAnyRole("LIBRARIAN", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/loans/me/active").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/api/loans/me").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/api/loans/allActive").hasAnyRole("LIBRARIAN", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/loans/*/return").hasAnyRole("USER", "LIBRARIAN", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/loans", "/api/loans/overdue/notify").hasAnyRole("LIBRARIAN", "ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/notifications/overdue-alert").hasRole( "ADMIN")

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
