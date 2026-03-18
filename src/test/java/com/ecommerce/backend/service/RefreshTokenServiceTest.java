package com.ecommerce.backend.service;

import com.ecommerce.backend.entity.RefreshToken;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.repository.RefreshTokenRepository;
import com.ecommerce.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User sampleUser;
    private RefreshToken sampleToken;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder().id("u1").email("test@example.com").build();
        sampleToken = new RefreshToken();
        sampleToken.setId(1L);
        sampleToken.setToken("token123");
        sampleToken.setUser(sampleUser);
        sampleToken.setExpiryDate(Instant.now().plusSeconds(3600));

        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenDurationMs", 3600000L);
    }

    @Test
    void findByToken_ShouldReturnToken() {
        when(refreshTokenRepository.findByToken("token123")).thenReturn(Optional.of(sampleToken));

        Optional<RefreshToken> result = refreshTokenService.findByToken("token123");

        assertTrue(result.isPresent());
        assertEquals("token123", result.get().getToken());
    }

    @Test
    void createRefreshToken_ShouldSucceed() {
        when(userRepository.findById("u1")).thenReturn(Optional.of(sampleUser));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(sampleToken);

        RefreshToken result = refreshTokenService.createRefreshToken("u1");

        assertNotNull(result);
        verify(refreshTokenRepository, times(1)).deleteByUser(sampleUser);
        verify(refreshTokenRepository, times(1)).flush();
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    void verifyExpiration_WhenValid_ShouldReturnToken() {
        RefreshToken result = refreshTokenService.verifyExpiration(sampleToken);

        assertEquals(sampleToken, result);
    }

    @Test
    void verifyExpiration_WhenExpired_ShouldThrowException() {
        sampleToken.setExpiryDate(Instant.now().minusSeconds(3600));

        assertThrows(RuntimeException.class, () -> refreshTokenService.verifyExpiration(sampleToken));
        verify(refreshTokenRepository).delete(sampleToken);
    }

    @Test
    void deleteByUserId_ShouldCallRepository() {
        when(userRepository.findById("u1")).thenReturn(Optional.of(sampleUser));
        when(refreshTokenRepository.deleteByUser(sampleUser)).thenReturn(1);

        int result = refreshTokenService.deleteByUserId("u1");

        assertEquals(1, result);
        verify(refreshTokenRepository).deleteByUser(sampleUser);
    }
}
