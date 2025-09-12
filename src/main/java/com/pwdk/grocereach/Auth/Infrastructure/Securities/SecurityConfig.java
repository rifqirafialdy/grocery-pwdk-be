package com.pwdk.grocereach.Auth.Infrastructure.Securities;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.pwdk.grocereach.Auth.Application.Services.UserService;
import jakarta.servlet.http.Cookie;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
<<<<<<< Updated upstream
=======
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
>>>>>>> Stashed changes
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtConfigProperties jwtConfigProperties;
    private final UserService userService;
<<<<<<< Updated upstream

    public SecurityConfig(JwtConfigProperties jwtConfigProperties, UserService userService) {
        this.jwtConfigProperties = jwtConfigProperties;
        this.userService = userService;
=======
    private final CustomOAuth2SuccessHandler customOAuth2SuccessHandler;
    private final JwtDecoder jwtDecoder;
    private final PasswordEncoder passwordEncoder;


    public SecurityConfig(UserService userService,
                          CustomOAuth2SuccessHandler customOAuth2SuccessHandler,
                          @Qualifier("jwtDecoder") JwtDecoder jwtDecoder,
                          PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.customOAuth2SuccessHandler = customOAuth2SuccessHandler;
        this.jwtDecoder = jwtDecoder;
        this.passwordEncoder = passwordEncoder;
>>>>>>> Stashed changes
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        var authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(authProvider);
    }

    @Bean
    @Order(1)
    public SecurityFilterChain oauth2LoginFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/oauth2/**", "/login/oauth2/**")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .oauth2Login(oauth2 -> oauth2.successHandler(customOAuth2SuccessHandler))
                .build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/api/**")
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
<<<<<<< Updated upstream
                        .requestMatchers("/api/auth/**").permitAll()
=======
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/products/public/**").permitAll()
                        .requestMatchers("/api/v1/locations/**").permitAll()
>>>>>>> Stashed changes
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2 -> oauth2
<<<<<<< Updated upstream
                        .jwt(jwt -> jwt.decoder(jwtDecoder()))
                        .bearerTokenResolver(bearerTokenResolver()) // <-- ADD THIS
                )
=======
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder)
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                        .bearerTokenResolver(bearerTokenResolver())
                )
                // This tells Spring to return a 401 error for API calls, not redirect.
                .exceptionHandling(exceptions ->
                        exceptions.authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
                )
>>>>>>> Stashed changes
                .build();
    }

    @Bean
    public BearerTokenResolver bearerTokenResolver() {
        return request -> {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("accessToken".equals(cookie.getName())) {
                        return cookie.getValue();
                    }
                }
            }
            return new DefaultBearerTokenResolver().resolve(request);
        };
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        var key = new SecretKeySpec(jwtConfigProperties.getSecret().getBytes(), "HmacSHA256");
        return new NimbusJwtEncoder(new ImmutableSecret<>(key));
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        var key = new SecretKeySpec(jwtConfigProperties.getSecret().getBytes(), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).build();
    }

    @Bean
    public JwtEncoder refreshTokenEncoder() {
        var key = new SecretKeySpec(jwtConfigProperties.getRefreshSecret().getBytes(), "HmacSHA256");
        return new NimbusJwtEncoder(new ImmutableSecret<>(key));
    }

    @Bean
    public JwtDecoder refreshTokenDecoder() {
        var key = new SecretKeySpec(jwtConfigProperties.getRefreshSecret().getBytes(), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).build();
    }
}