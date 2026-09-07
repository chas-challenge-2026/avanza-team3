package se.comerit.avanza.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private FilterChain filterChain;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void validTokenAuthenticatesUser() throws ServletException, IOException {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);

        MockHttpServletRequest request = new MockHttpServletRequest();

        request.addHeader("Authorization", "Bearer valid-token");

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.isTokenValid("valid-token"))
                .thenReturn(true);

        when(jwtService.extractEmail("valid-token"))
                .thenReturn("test@example.com");

        when(jwtService.extractUserId("valid-token"))
                .thenReturn(1);

        filter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        assertEquals("test@example.com", authentication.getPrincipal());

        assertEquals(1, authentication.getDetails());

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void invalidTokenDoesNotAuthenticateUser() throws ServletException, IOException {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);

        MockHttpServletRequest request = new MockHttpServletRequest();

        request.addHeader("Authorization", "Bearer invalid-token");

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.isTokenValid("invalid-token"))
                .thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder
                .getContext()
                .getAuthentication());

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void missingAuthorizationHeaderDoesNotAuthenticateUser() throws ServletException, IOException {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);

        MockHttpServletRequest request = new MockHttpServletRequest();

        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder
                .getContext()
                .getAuthentication());

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void tokenWithoutBearerPrefixIsIgnored() throws ServletException, IOException{

        JwtAuthenticationFilter filter =  new JwtAuthenticationFilter(jwtService);
        
        MockHttpServletRequest request = new MockHttpServletRequest();

        request.addHeader("Authorization", "Invalid valid-token");

        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder
                        .getContext()
                        .getAuthentication());
        
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void existingAuthenticationIsNotOverwritten() throws ServletException, IOException {

        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);

        Authentication existingAuthentication = new UsernamePasswordAuthenticationToken(
                "existing@example.com",
                null,
                java.util.Collections.emptyList());
        
        SecurityContextHolder.getContext()
                .setAuthentication(existingAuthentication);

        MockHttpServletRequest request = new MockHttpServletRequest();

        request.addHeader("Authorization", "Bearer valid-token");

        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        Authentication result = SecurityContextHolder.getContext().getAuthentication();

        assertEquals("existing@example.com", result.getPrincipal());

        verify(filterChain).doFilter(request, response);
    }
}
