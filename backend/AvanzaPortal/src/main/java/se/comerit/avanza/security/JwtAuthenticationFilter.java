package se.comerit.avanza.security;

import java.io.IOException;
import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String token = null;

        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("access_token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            if (jwtService.isTokenValid(token) && SecurityContextHolder
                    .getContext()
                    .getAuthentication() == null) {

                String email = jwtService.extractEmail(token);
                Integer userId = jwtService.extractUserId(token);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        Collections.emptyList());

                authentication.setDetails(userId);

                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);
            }
        } catch (Exception exception) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);

    }

}
