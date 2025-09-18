package com.long_bus_distance.tickets.repository;
import com.long_bus_distance.tickets.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
//OncePerRequestFilter: bo loc thuc thi 1 lan duy nhat cho moi yeu cau HTTP
public class UserProvisioningFilter extends OncePerRequestFilter {
    private final UserRepository userRepository;

    //Khi import lib nay se tu dong tao ham doFilterInternal
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof Jwt jwt){

            UUID keycloakId = UUID.fromString(jwt.getSubject());

            if (!userRepository.existsById(keycloakId)) {
                User user = new User();
                user.setId(keycloakId); //Day la li do tam thoi xoa @Builder khoi entity
                user.setUsername(jwt.getClaims().get("preferred_username").toString());
                user.setEmail(jwt.getClaims().get("email").toString());

                //Dat gia tri mac dinh cho cac truong bat buoc
                user.setFirstname(jwt.getClaims().get("given_name")!=null
                        ? jwt.getClaims().get("given_name").toString()
                        : "Unknow");
                user.setLastname(jwt.getClaims().get("family_name")!=null
                        ? jwt.getClaims().get("family_name").toString()
                        : "Unknown");

                userRepository.save(user);
            }
        }
        filterChain.doFilter(request, response);
    }
}
