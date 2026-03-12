package com.fiapx.video.infrastructure.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.security.Key;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtFilter jwtFilter;

    private final String SECRET = "hackathon-secret-key-fiap-x-soat11-java-test-key";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtFilter, "secret", SECRET);
        SecurityContextHolder.clearContext();
    }

    @Test
    @SuppressWarnings("deprecation")
    void doFilterInternal_withValidToken_shouldAuthenticate() throws ServletException, IOException {
        Key key = Keys.hmacShaKeyFor(SECRET.getBytes());
        String token = Jwts.builder()
                .setSubject("testuser")
                .claim("email", "test@example.com")
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        jwtFilter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals("testuser", auth.getName());
        assertTrue(auth.getDetails() instanceof Map);
        Map<?, ?> details = (Map<?, ?>) auth.getDetails();
        assertEquals("test@example.com", details.get("email"));

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_withoutToken_shouldNotAuthenticate() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_withInvalidToken_shouldClearContext() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_withTokenWithoutBearer_shouldNotAuthenticate() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("invalid-token");

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_withEmptyToken_shouldNotAuthenticate() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("");

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @SuppressWarnings("deprecation")
    void doFilterInternal_withTokenWithoutEmail_shouldAuthenticateWithoutEmail() throws ServletException, IOException {
        Key key = Keys.hmacShaKeyFor(SECRET.getBytes());
        String token = Jwts.builder()
                .setSubject("testuser")
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        jwtFilter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals("testuser", auth.getName());
        Map<?, ?> details = (Map<?, ?>) auth.getDetails();
        assertNull(details.get("email"));

        verify(filterChain).doFilter(request, response);
    }
}
