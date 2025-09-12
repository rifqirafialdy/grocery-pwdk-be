package com.pwdk.grocereach.Auth.Infrastructure.Securities;

import com.pwdk.grocereach.Auth.Application.Services.TokenGeneratorService;
import com.pwdk.grocereach.Auth.Domain.Entities.User;
import com.pwdk.grocereach.Auth.Domain.Enums.UserRole;
import com.pwdk.grocereach.Auth.Domain.ValueOfObject.Token;
import com.pwdk.grocereach.Auth.Infrastructure.Repositories.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final TokenGeneratorService tokenGeneratorService;
    private final PasswordEncoder passwordEncoder;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setFullName(oauthUser.getAttribute("name"));
                    newUser.setPhotoUrl(oauthUser.getAttribute("picture"));
                    newUser.setRole(UserRole.CUSTOMER);
                    newUser.setVerified(true);
                    String randomPassword = UUID.randomUUID().toString();
                    newUser.setPassword(passwordEncoder.encode(randomPassword));
                    return userRepository.save(newUser);
                });


        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        Authentication authForToken = new UsernamePasswordAuthenticationToken(
                new CustomUserDetails(user), null, authorities
        );

        Token accessToken = tokenGeneratorService.generateAccessToken(authForToken);
        Token refreshToken = tokenGeneratorService.generateRefreshToken(authForToken);

        ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", accessToken.getValue())
                .httpOnly(true).secure(false).path("/").maxAge(15 * 60).sameSite("Lax").build();
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken.getValue())
                .httpOnly(true).secure(false).path("/api/v1/auth").maxAge(30 * 24 * 60 * 60).sameSite("Lax").build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        String targetUrl = "http://localhost:3000";
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}