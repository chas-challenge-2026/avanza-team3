package se.comerit.avanza.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Collections;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import se.comerit.avanza.config.RateLimitProperties;

class RateLimitFilterTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void apiRequestOverLimitReturns429AndRetryAfter() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(properties(
                new RateLimitProperties.Limit(5, 5, Duration.ofMinutes(1)),
                new RateLimitProperties.Limit(1, 1, Duration.ofMinutes(1))));

        authenticate(7);

        MockHttpServletRequest firstRequest = apiRequest("10.0.0.1");
        MockHttpServletResponse firstResponse = new MockHttpServletResponse();
        filter.doFilter(firstRequest, firstResponse, new MockFilterChain());

        MockHttpServletRequest secondRequest = apiRequest("10.0.0.1");
        MockHttpServletResponse secondResponse = new MockHttpServletResponse();
        filter.doFilter(secondRequest, secondResponse, new MockFilterChain());

        assertThat(firstResponse.getStatus()).isEqualTo(200);
        assertThat(secondResponse.getStatus()).isEqualTo(429);
        assertThat(secondResponse.getHeader("Retry-After")).isNotBlank();
        assertThat(secondResponse.getContentAsString()).contains("Too many requests");
    }

    @Test
    void authenticatedUsersUseSeparateApiBuckets() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(properties(
                new RateLimitProperties.Limit(5, 5, Duration.ofMinutes(1)),
                new RateLimitProperties.Limit(1, 1, Duration.ofMinutes(1))));

        authenticate(7);
        MockHttpServletResponse firstUserResponse = new MockHttpServletResponse();
        filter.doFilter(apiRequest("10.0.0.1"), firstUserResponse, new MockFilterChain());

        authenticate(8);
        MockHttpServletResponse secondUserResponse = new MockHttpServletResponse();
        filter.doFilter(apiRequest("10.0.0.1"), secondUserResponse, new MockFilterChain());

        assertThat(firstUserResponse.getStatus()).isEqualTo(200);
        assertThat(secondUserResponse.getStatus()).isEqualTo(200);
    }

    @Test
    void loginRequestsFromSameIpShareLoginBucket() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(properties(
                new RateLimitProperties.Limit(1, 1, Duration.ofMinutes(1)),
                new RateLimitProperties.Limit(50, 2, Duration.ofSeconds(1))));

        MockHttpServletResponse firstResponse = new MockHttpServletResponse();
        filter.doFilter(loginRequest("10.0.0.2"), firstResponse, new MockFilterChain());

        MockHttpServletResponse secondResponse = new MockHttpServletResponse();
        filter.doFilter(loginRequest("10.0.0.2"), secondResponse, new MockFilterChain());

        assertThat(firstResponse.getStatus()).isEqualTo(200);
        assertThat(secondResponse.getStatus()).isEqualTo(429);
    }

    @Test
    void nonApiRequestIsNotRateLimited() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(properties(
                new RateLimitProperties.Limit(1, 1, Duration.ofMinutes(1)),
                new RateLimitProperties.Limit(1, 1, Duration.ofMinutes(1))));

        for (int i = 0; i < 3; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/swagger-ui/index.html");
            request.setRemoteAddr("10.0.0.3");
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilter(request, response, new MockFilterChain());

            assertThat(response.getStatus()).isEqualTo(200);
        }
    }

    @Test
    void optionsRequestIsNotRateLimited() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(properties(
                new RateLimitProperties.Limit(1, 1, Duration.ofMinutes(1)),
                new RateLimitProperties.Limit(1, 1, Duration.ofMinutes(1))));

        for (int i = 0; i < 3; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/holdings");
            request.setRemoteAddr("10.0.0.4");
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilter(request, response, new MockFilterChain());

            assertThat(response.getStatus()).isEqualTo(200);
        }
    }

    private RateLimitProperties properties(
            RateLimitProperties.Limit login,
            RateLimitProperties.Limit api) {
        return new RateLimitProperties(true, login, api);
    }

    private MockHttpServletRequest apiRequest(String ip) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/holdings");
        request.setRemoteAddr(ip);
        return request;
    }

    private MockHttpServletRequest loginRequest(String ip) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        request.setRemoteAddr(ip);
        return request;
    }

    private void authenticate(int userId) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken("user@example.com", null, Collections.emptyList());
        authentication.setDetails(userId);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
