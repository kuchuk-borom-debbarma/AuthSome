package dev.kuku.authsome.core_services;

import dev.kuku.authsome.core_service.authsome.api.dto.SignInTokens;
import dev.kuku.authsome.core_service.authsome.api.exceptions.*;
import dev.kuku.authsome.core_service.authsome.impl.AuthsomeRefreshTokenJpaRepo;
import dev.kuku.authsome.core_service.authsome.impl.AuthsomeServiceImpl;
import dev.kuku.authsome.core_service.authsome.impl.AuthsomeUserIdentityJpaRepo;
import dev.kuku.authsome.core_service.authsome.impl.AuthsomeUserJpaRepo;
import dev.kuku.authsome.core_service.authsome.impl.entity.AuthsomeUserEntity;
import dev.kuku.authsome.core_service.authsome.impl.entity.AuthsomeUserIdentityEntity;
import dev.kuku.authsome.core_service.authsome.impl.entity.AuthsomeUserRefreshTokenEntity;
import dev.kuku.authsome.core_service.project.api.dto.IdentityType;
import dev.kuku.authsome.util_service.jwt.api.JwtService;
import dev.kuku.authsome.util_service.notifier.api.NotifierService;
import dev.kuku.authsome.util_service.otp.api.OtpService;
import dev.kuku.vfl.api.annotation.VFLAnnotation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Example;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthsomeService - Sign In & Refresh Token Tests")
public class AuthsomeServiceImplSignInTest {

    @Mock
    private AuthsomeUserJpaRepo userJpaRepo;

    @Mock
    private AuthsomeUserIdentityJpaRepo identityJpaRepo;

    @Mock
    private AuthsomeRefreshTokenJpaRepo refreshTokenJpaRepo;

    @Mock
    private JwtService jwtService;

    @Mock
    private OtpService otpService;

    @Mock
    private NotifierService notifierService;

    @Mock
    private VFLAnnotation log;

    private BCryptPasswordEncoder passwordEncoder;
    private AuthsomeServiceImpl authsomeService;

    // In-memory storage for mocking persistence
    private List<AuthsomeUserEntity> userStorage;
    private List<AuthsomeUserIdentityEntity> identityStorage;
    private List<AuthsomeUserRefreshTokenEntity> refreshTokenStorage;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        authsomeService = new AuthsomeServiceImpl(
                passwordEncoder,
                userJpaRepo,
                identityJpaRepo,
                refreshTokenJpaRepo,
                jwtService,
                otpService,
                notifierService,
                log
        );

        // Set configuration values
        ReflectionTestUtils.setField(authsomeService, "authsomeSignInJwtExpiry", 30);
        ReflectionTestUtils.setField(authsomeService, "authsomeSignInJwtExpiryUnit", TimeUnit.MINUTES);
        ReflectionTestUtils.setField(authsomeService, "maxActiveSession", 3);

        // Initialize in-memory storage
        userStorage = new ArrayList<>();
        identityStorage = new ArrayList<>();
        refreshTokenStorage = new ArrayList<>();

        setupMockBehaviors();
    }

    private void setupMockBehaviors() {
        // Mock identity repository to use in-memory storage
        lenient().when(identityJpaRepo.findOne(any(Example.class))).thenAnswer(invocation -> {
            Example<AuthsomeUserIdentityEntity> example = invocation.getArgument(0);
            AuthsomeUserIdentityEntity probe = example.getProbe();

            return identityStorage.stream()
                    .filter(entity -> {
                        boolean matches = true;
                        if (probe.identityType != null) {
                            matches = entity.identityType == probe.identityType;
                        }
                        if (probe.identity != null && matches) {
                            matches = entity.identity.equals(probe.identity);
                        }
                        return matches;
                    })
                    .findFirst();
        });

        // Mock refresh token count
        lenient().when(refreshTokenJpaRepo.count(any(Example.class))).thenAnswer(invocation -> {
            Example<AuthsomeUserRefreshTokenEntity> example = invocation.getArgument(0);
            AuthsomeUserRefreshTokenEntity probe = example.getProbe();
            String userId = probe.user.id;

            return refreshTokenStorage.stream()
                    .filter(rt -> rt.user.id.equals(userId))
                    .count();
        });

        // Mock refresh token save
        lenient().when(refreshTokenJpaRepo.save(any(AuthsomeUserRefreshTokenEntity.class))).thenAnswer(invocation -> {
            AuthsomeUserRefreshTokenEntity entity = invocation.getArgument(0);
            if (entity.id == null) {
                entity.id = "rt_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
            }
            refreshTokenStorage.add(entity);
            return entity;
        });

        // Mock refresh token findById
        lenient().when(refreshTokenJpaRepo.findById(anyString())).thenAnswer(invocation -> {
            String id = invocation.getArgument(0);
            return refreshTokenStorage.stream()
                    .filter(rt -> rt.id.equals(id))
                    .findFirst();
        });

        // Mock refresh token deleteById
        lenient().doAnswer(invocation -> {
            String id = invocation.getArgument(0);
            refreshTokenStorage.removeIf(rt -> rt.id.equals(id));
            return null;
        }).when(refreshTokenJpaRepo).deleteById(anyString());

        // Mock refresh token deleteAllByUserId
        lenient().doAnswer(invocation -> {
            String userId = invocation.getArgument(0);
            refreshTokenStorage.removeIf(rt -> rt.user.id.equals(userId));
            return null;
        }).when(refreshTokenJpaRepo).deleteAllByUserId(anyString());

        // Mock JWT generation
        lenient().when(jwtService.generateToken(anyString(), any(), anyInt(), any(TimeUnit.class)))
                .thenAnswer(invocation -> "jwt_" + invocation.getArgument(0) + "_" + System.currentTimeMillis());
    }

    @Test
    @DisplayName("Should successfully sign in with valid credentials")
    void testSignIn_Success() throws Exception, AuthsomeUserWithIdentityNotFound, MaxActiveSessionsReached, AuthsomePasswordMismatch {
        // Arrange
        String userId = "user123";
        String username = "testuser";
        String rawPassword = "password123";
        String hashedPassword = passwordEncoder.encode(rawPassword);
        String identity = "test@example.com";
        IdentityType identityType = IdentityType.EMAIL;

        AuthsomeUserEntity user = new AuthsomeUserEntity(
                userId, username, hashedPassword,
                Instant.now().toEpochMilli(), Instant.now().toEpochMilli()
        );

        AuthsomeUserIdentityEntity userIdentity = new AuthsomeUserIdentityEntity(
                "identity123", user, identityType, identity,
                Instant.now().toEpochMilli(), Instant.now().toEpochMilli()
        );

        identityStorage.add(userIdentity);

        // Act
        SignInTokens tokens = authsomeService.signIn(identityType, identity, rawPassword);

        // Assert
        assertNotNull(tokens);
        assertNotNull(tokens.accessToken());
        assertNotNull(tokens.refreshToken());
        assertTrue(tokens.accessToken().startsWith("jwt_"));
        assertTrue(tokens.refreshToken().startsWith("rt_"));

        // Verify refresh token was saved
        assertEquals(1, refreshTokenStorage.size());
        assertEquals(userId, refreshTokenStorage.get(0).user.id);

        // Verify JWT was generated with correct parameters
        verify(jwtService).generateToken(
                eq(userId),
                argThat(claims -> claims.get("type").equals("authsome-user")),
                eq(30),
                eq(TimeUnit.MINUTES)
        );
    }

    @Test
    @DisplayName("Should throw exception when user with identity not found")
    void testSignIn_UserNotFound() {
        // Arrange
        String identity = "nonexistent@example.com";
        IdentityType identityType = IdentityType.EMAIL;
        String password = "password123";

        // Act & Assert
        assertThrows(AuthsomeUserWithIdentityNotFound.class, () ->
                authsomeService.signIn(identityType, identity, password)
        );

        // Verify no tokens were created
        assertEquals(0, refreshTokenStorage.size());
    }

    @Test
    @DisplayName("Should throw exception when password is incorrect")
    void testSignIn_WrongPassword() {
        // Arrange
        String userId = "user123";
        String username = "testuser";
        String correctPassword = "correctPassword";
        String wrongPassword = "wrongPassword";
        String hashedPassword = passwordEncoder.encode(correctPassword);
        String identity = "test@example.com";
        IdentityType identityType = IdentityType.EMAIL;

        AuthsomeUserEntity user = new AuthsomeUserEntity(
                userId, username, hashedPassword,
                Instant.now().toEpochMilli(), Instant.now().toEpochMilli()
        );

        AuthsomeUserIdentityEntity userIdentity = new AuthsomeUserIdentityEntity(
                "identity123", user, identityType, identity,
                Instant.now().toEpochMilli(), Instant.now().toEpochMilli()
        );

        identityStorage.add(userIdentity);

        // Act & Assert
        assertThrows(AuthsomePasswordMismatch.class, () ->
                authsomeService.signIn(identityType, identity, wrongPassword)
        );

        // Verify no tokens were created
        assertEquals(0, refreshTokenStorage.size());
    }

    @Test
    @DisplayName("Should throw exception when max active sessions reached")
    void testSignIn_MaxSessionsReached() {
        // Arrange
        String userId = "user123";
        String username = "testuser";
        String rawPassword = "password123";
        String hashedPassword = passwordEncoder.encode(rawPassword);
        String identity = "test@example.com";
        IdentityType identityType = IdentityType.EMAIL;

        AuthsomeUserEntity user = new AuthsomeUserEntity(
                userId, username, hashedPassword,
                Instant.now().toEpochMilli(), Instant.now().toEpochMilli()
        );

        AuthsomeUserIdentityEntity userIdentity = new AuthsomeUserIdentityEntity(
                "identity123", user, identityType, identity,
                Instant.now().toEpochMilli(), Instant.now().toEpochMilli()
        );

        identityStorage.add(userIdentity);

        // Create 3 existing refresh tokens (max limit)
        for (int i = 0; i < 3; i++) {
            refreshTokenStorage.add(new AuthsomeUserRefreshTokenEntity(
                    "rt_existing_" + i, user,
                    Instant.now().toEpochMilli(), Instant.now().toEpochMilli(),
                    30, TimeUnit.MINUTES, null
            ));
        }

        // Act & Assert
        assertThrows(MaxActiveSessionsReached.class, () ->
                authsomeService.signIn(identityType, identity, rawPassword)
        );

        // Verify no new token was created
        assertEquals(3, refreshTokenStorage.size());
    }

    @Test
    @DisplayName("Should successfully refresh token with valid refresh token")
    void testRefreshToken_Success() throws Exception, InvalidRefreshToken {
        // Arrange
        String userId = "user123";
        String username = "testuser";
        String oldRefreshTokenId = "rt_old_token";

        AuthsomeUserEntity user = new AuthsomeUserEntity(
                userId, username, "hashedPass",
                Instant.now().toEpochMilli(), Instant.now().toEpochMilli()
        );

        AuthsomeUserRefreshTokenEntity oldRefreshToken = new AuthsomeUserRefreshTokenEntity(
                oldRefreshTokenId, user,
                Instant.now().toEpochMilli(), Instant.now().toEpochMilli(),
                30, TimeUnit.MINUTES, null
        );

        refreshTokenStorage.add(oldRefreshToken);

        // Act
        SignInTokens tokens = authsomeService.refreshToken(oldRefreshTokenId);

        // Assert
        assertNotNull(tokens);
        assertNotNull(tokens.accessToken());
        assertNotNull(tokens.refreshToken());
        assertTrue(tokens.accessToken().startsWith("jwt_"));
        assertTrue(tokens.refreshToken().startsWith("rt_"));
        assertNotEquals(oldRefreshTokenId, tokens.refreshToken());

        // Verify old refresh token was deleted
        assertFalse(refreshTokenStorage.stream()
                .anyMatch(rt -> rt.id.equals(oldRefreshTokenId)));

        // Verify new refresh token was saved
        assertTrue(refreshTokenStorage.stream()
                .anyMatch(rt -> rt.id.equals(tokens.refreshToken())));

        // Should have exactly 1 token (new one, old deleted)
        assertEquals(1, refreshTokenStorage.size());
    }

    @Test
    @DisplayName("Should throw exception when refreshing with invalid token")
    void testRefreshToken_InvalidToken() {
        // Arrange
        String invalidRefreshToken = "rt_invalid_token";

        // Act & Assert
        assertThrows(InvalidRefreshToken.class, () ->
                authsomeService.refreshToken(invalidRefreshToken)
        );
    }

    @Test
    @DisplayName("Should successfully revoke specific refresh token")
    void testRevokeRefreshToken_Success() {
        // Arrange
        String userId = "user123";
        String refreshTokenId = "rt_token_to_revoke";

        AuthsomeUserEntity user = new AuthsomeUserEntity(
                userId, "testuser", "hashedPass",
                Instant.now().toEpochMilli(), Instant.now().toEpochMilli()
        );

        AuthsomeUserRefreshTokenEntity refreshToken = new AuthsomeUserRefreshTokenEntity(
                refreshTokenId, user,
                Instant.now().toEpochMilli(), Instant.now().toEpochMilli(),
                30, TimeUnit.MINUTES, null
        );

        refreshTokenStorage.add(refreshToken);
        assertEquals(1, refreshTokenStorage.size());

        // Act
        authsomeService.revokeRefreshToken(refreshTokenId);

        // Assert
        assertEquals(0, refreshTokenStorage.size());
        verify(refreshTokenJpaRepo).deleteById(refreshTokenId);
    }

    @Test
    @DisplayName("Should successfully revoke all refresh tokens of a user")
    void testRevokeAllRefreshTokensOfUser_Success() {
        // Arrange
        String userId = "user123";
        String otherUserId = "user456";

        AuthsomeUserEntity user1 = new AuthsomeUserEntity(
                userId, "testuser1", "hashedPass",
                Instant.now().toEpochMilli(), Instant.now().toEpochMilli()
        );

        AuthsomeUserEntity user2 = new AuthsomeUserEntity(
                otherUserId, "testuser2", "hashedPass",
                Instant.now().toEpochMilli(), Instant.now().toEpochMilli()
        );

        // Add 3 tokens for user1
        for (int i = 0; i < 3; i++) {
            refreshTokenStorage.add(new AuthsomeUserRefreshTokenEntity(
                    "rt_user1_" + i, user1,
                    Instant.now().toEpochMilli(), Instant.now().toEpochMilli(),
                    30, TimeUnit.MINUTES, null
            ));
        }

        // Add 2 tokens for user2
        for (int i = 0; i < 2; i++) {
            refreshTokenStorage.add(new AuthsomeUserRefreshTokenEntity(
                    "rt_user2_" + i, user2,
                    Instant.now().toEpochMilli(), Instant.now().toEpochMilli(),
                    30, TimeUnit.MINUTES, null
            ));
        }

        assertEquals(5, refreshTokenStorage.size());

        // Act
        authsomeService.revokeAllRefreshTokenOfUser(userId);

        // Assert
        // Only user2's tokens should remain
        assertEquals(2, refreshTokenStorage.size());
        assertTrue(refreshTokenStorage.stream()
                .allMatch(rt -> rt.user.id.equals(otherUserId)));
        verify(refreshTokenJpaRepo).deleteAllByUserId(userId);
    }

    @Test
    @DisplayName("Should allow sign in when under max session limit")
    void testSignIn_UnderSessionLimit() throws Exception, AuthsomeUserWithIdentityNotFound, MaxActiveSessionsReached, AuthsomePasswordMismatch {
        // Arrange
        String userId = "user123";
        String username = "testuser";
        String rawPassword = "password123";
        String hashedPassword = passwordEncoder.encode(rawPassword);
        String identity = "test@example.com";
        IdentityType identityType = IdentityType.EMAIL;

        AuthsomeUserEntity user = new AuthsomeUserEntity(
                userId, username, hashedPassword,
                Instant.now().toEpochMilli(), Instant.now().toEpochMilli()
        );

        AuthsomeUserIdentityEntity userIdentity = new AuthsomeUserIdentityEntity(
                "identity123", user, identityType, identity,
                Instant.now().toEpochMilli(), Instant.now().toEpochMilli()
        );

        identityStorage.add(userIdentity);

        // Create 2 existing tokens (under limit of 3)
        for (int i = 0; i < 2; i++) {
            refreshTokenStorage.add(new AuthsomeUserRefreshTokenEntity(
                    "rt_existing_" + i, user,
                    Instant.now().toEpochMilli(), Instant.now().toEpochMilli(),
                    30, TimeUnit.MINUTES, null
            ));
        }

        // Act
        SignInTokens tokens = authsomeService.signIn(identityType, identity, rawPassword);

        // Assert
        assertNotNull(tokens);
        assertEquals(3, refreshTokenStorage.size()); // 2 existing + 1 new
    }

    @Test
    @DisplayName("Should successfully chain multiple refresh token operations")
    void testRefreshToken_MultipleRefreshes() throws Exception, InvalidRefreshToken {
        // Arrange
        String userId = "user123";
        AuthsomeUserEntity user = new AuthsomeUserEntity(
                userId, "testuser", "hashedPass",
                Instant.now().toEpochMilli(), Instant.now().toEpochMilli()
        );

        AuthsomeUserRefreshTokenEntity initialToken = new AuthsomeUserRefreshTokenEntity(
                "rt_initial", user,
                Instant.now().toEpochMilli(), Instant.now().toEpochMilli(),
                30, TimeUnit.MINUTES, null
        );

        refreshTokenStorage.add(initialToken);
        assertEquals(1, refreshTokenStorage.size(), "Should start with 1 token");

        // Act - First refresh
        SignInTokens tokens1 = authsomeService.refreshToken("rt_initial");
        System.out.println("Tokens1 - Access: " + tokens1.accessToken() + ", Refresh: " + tokens1.refreshToken());
        assertEquals(1, refreshTokenStorage.size(), "Should have 1 token after first refresh");

        // Act - Second refresh
        SignInTokens tokens2 = authsomeService.refreshToken(tokens1.refreshToken());
        System.out.println("Tokens2 - Access: " + tokens2.accessToken() + ", Refresh: " + tokens2.refreshToken());
        assertEquals(1, refreshTokenStorage.size(), "Should have 1 token after second refresh");

        // Act - Third refresh
        SignInTokens tokens3 = authsomeService.refreshToken(tokens2.refreshToken());
        System.out.println("Tokens3 - Access: " + tokens3.accessToken() + ", Refresh: " + tokens3.refreshToken());

        // Assert
        assertNotNull(tokens3);
        assertEquals(1, refreshTokenStorage.size(), "Should have 1 token after third refresh");

        // Each refresh should produce unique tokens
        System.out.println("\nComparing tokens1 and tokens2:");
        System.out.println("Refresh tokens equal? " + tokens1.refreshToken().equals(tokens2.refreshToken()));
        System.out.println("Access tokens equal? " + tokens1.accessToken().equals(tokens2.accessToken()));

        assertNotEquals(tokens1.refreshToken(), tokens2.refreshToken(),
                "First and second refresh tokens should be different");
        assertNotEquals(tokens2.refreshToken(), tokens3.refreshToken(),
                "Second and third refresh tokens should be different");
        assertNotEquals(tokens1.accessToken(), tokens2.accessToken(),
                "First and second access tokens should be different");
        assertNotEquals(tokens2.accessToken(), tokens3.accessToken(),
                "Second and third access tokens should be different");
    }
}