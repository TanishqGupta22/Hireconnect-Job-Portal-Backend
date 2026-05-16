package com.hireconnect.auth.security.oauth2;

import com.hireconnect.auth.entity.AuthProvider;
import com.hireconnect.auth.entity.Role;
import com.hireconnect.auth.entity.User;
import com.hireconnect.auth.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);
        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (Exception ex) {
            log.error("Error processing OAuth2 user: {}", ex.getMessage());
            throw new OAuth2AuthenticationException(ex.getMessage());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

        if (email == null || email.isEmpty()) {
            throw new RuntimeException("Email not found from OAuth2 provider");
        }

        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
            if (!user.getProvider().equals(AuthProvider.GOOGLE)) {
                throw new RuntimeException("Looks like you're signed up with " +
                        user.getProvider() + " account. Please use your " + user.getProvider() +
                        " account to login.");
            }
            user = updateExistingUser(user, name);
        } else {
            user = registerNewUser(oAuth2UserRequest, email, name);
        }

        return oAuth2User;
    }

    private User registerNewUser(OAuth2UserRequest oAuth2UserRequest, String email, String name) {
        Role role = getRoleFromCookie();
        log.info("Registering new Google user {} with role {}", email, role);
        
        User user = User.builder()
                .name(name)
                .email(email)
                .provider(AuthProvider.GOOGLE)
                .role(role)
                .password(UUID.randomUUID().toString())
                .createdAt(LocalDateTime.now())
                .emailVerified(true)
                .build();
                
        user = userRepository.save(user);

        // Send registration event to RabbitMQ for Google users
        try {
            java.util.Map<String, Object> event = new java.util.HashMap<>();
            event.put("userId", user.getId());
            event.put("email", user.getEmail());
            event.put("name", user.getName());
            
            System.out.println("BEFORE RABBITMQ (OAUTH)");
            rabbitTemplate.convertAndSend("user.registered.queue", event);
            System.out.println("AFTER RABBITMQ (OAUTH)");
            System.out.println("RabbitMQ event sent successfully (OAuth)");
            log.info("Sent user.registered event for Google user: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send user.registered event for Google user", e);
        }

        return user;
    }

    private Role getRoleFromCookie() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("hc_role".equals(cookie.getName())) {
                        try {
                            return Role.valueOf(cookie.getValue().toUpperCase());
                        } catch (Exception e) {
                            log.warn("Invalid role in cookie: {}", cookie.getValue());
                        }
                    }
                }
            }
        }
        return Role.CANDIDATE; // Default
    }

    private User updateExistingUser(User user, String name) {
        user.setName(name);
        return userRepository.save(user);
    }
}
