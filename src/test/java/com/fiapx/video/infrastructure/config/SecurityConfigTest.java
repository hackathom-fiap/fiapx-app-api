package com.fiapx.video.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private JwtFilter jwtFilter;

    @Test
    void securityConfig_shouldBeInstantiatable() {
        SecurityConfig config = new SecurityConfig(jwtFilter);
        assertNotNull(config);
    }
}
