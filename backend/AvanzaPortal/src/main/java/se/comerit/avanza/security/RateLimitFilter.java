package se.comerit.avanza.security;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import se.comerit.avanza.config.RateLimitProperties;

public class RateLimitFilter extends OncePerRequestFilter {

    private static final String LOGIN_PATH = "/api/auth/login";

    private final RateLimitProperties properties;

    private final Cache<String, Bucket> loginBuckets = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterAccess(Duration.ofMinutes(30))
            .build();

    private final Cache<String, Bucket> apiBuckets = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterAccess(Duration.ofMinutes(30))
            .build();

    public RateLimitFilter(RateLimitProperties properties) {
        this.properties = properties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/")
                || HttpMethod.OPTIONS.matches(request.getMethod());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        boolean loginRequest = LOGIN_PATH.equals(request.getRequestURI());

        RateLimitProperties.Limit limit = loginRequest
                ? properties.login()
                : properties.api();

        Cache<String, Bucket> cache = loginRequest
                ? loginBuckets
                : apiBuckets;

        String key = loginRequest
                ? "login:" + request.getRemoteAddr()
                : resolveApiKey(request);

        Bucket bucket = cache.get(key, ignored -> createBucket(limit));
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            filterChain.doFilter(request, response);
            return;
        }

        long retryAfterSeconds = Math.max(
                1,
                TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()));

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Retry-After", Long.toString(retryAfterSeconds));
        response.getWriter().write("{\"error\":\"Too many requests\"}");
    }

    private String resolveApiKey(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getDetails() instanceof Integer userId) {
            return "user:" + userId;
        }

        return "ip:" + request.getRemoteAddr();
    }

    private Bucket createBucket(RateLimitProperties.Limit limit) {
        return Bucket.builder()
                .addLimit(bucketLimit -> bucketLimit
                        .capacity(limit.capacity())
                        .refillGreedy(limit.refillTokens(), limit.refillPeriod()))
                .build();
    }
}
