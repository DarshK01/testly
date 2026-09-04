package com.testly.config;

import com.testly.security.CustomUserDetailsService;
import com.testly.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthFilter jwtAuthFilter;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/tags").permitAll()

                        // Teacher-only: creating/managing tests, questions, tags, and viewing results.
                        .requestMatchers(HttpMethod.POST, "/api/tests").hasRole("TEACHER")
                        .requestMatchers(HttpMethod.PUT, "/api/tests/**").hasRole("TEACHER")
                        .requestMatchers(HttpMethod.POST, "/api/tests/*/questions").hasRole("TEACHER")
                        .requestMatchers(HttpMethod.PUT, "/api/questions/**").hasRole("TEACHER")
                        .requestMatchers(HttpMethod.DELETE, "/api/questions/**").hasRole("TEACHER")
                        .requestMatchers("/api/tests/*/results").hasRole("TEACHER")
                        .requestMatchers("/api/tests/my").hasRole("TEACHER")
                        .requestMatchers(HttpMethod.POST, "/api/tags").hasRole("TEACHER")
                        .requestMatchers(HttpMethod.POST, "/api/classrooms").hasRole("TEACHER")
                        .requestMatchers("/api/classrooms/my").hasRole("TEACHER")

                        // Student-only: browsing available tests and attempting them.
                        .requestMatchers("/api/tests/available").hasRole("STUDENT")
                        .requestMatchers("/api/tests/*/attempt").hasRole("STUDENT")
                        .requestMatchers("/api/attempts/**").hasRole("STUDENT")
                        .requestMatchers("/api/classrooms/join").hasRole("STUDENT")
                        .requestMatchers("/api/classrooms/joined").hasRole("STUDENT")

                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
