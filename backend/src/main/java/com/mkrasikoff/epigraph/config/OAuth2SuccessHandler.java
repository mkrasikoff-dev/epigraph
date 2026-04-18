package com.mkrasikoff.epigraph.config;

import com.mkrasikoff.epigraph.model.User;
import com.mkrasikoff.epigraph.repository.UserRepository;
import com.mkrasikoff.epigraph.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    public OAuth2SuccessHandler(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();

        String email      = oauthUser.getAttribute("email");
        String providerId = oauthUser.getAttribute("sub"); // Google user ID

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setProvider("google");
            newUser.setProviderId(providerId);
            newUser.setCreatedAt(System.currentTimeMillis());
            return userRepository.save(newUser);
        });

        String token = jwtService.generateToken(user.getId(), user.getEmail());

        // Передаём токен на фронтенд через query-параметр, он сразу кладёт его в localStorage
        response.sendRedirect("/?token=" + token);
    }
}
