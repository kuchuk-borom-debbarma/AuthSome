package dev.kuku.authsome.core_services;

import dev.kuku.authsome.core_service.authsome.api.dto.SignInTokens;
import dev.kuku.authsome.core_service.authsome.api.exceptions.*;
import dev.kuku.authsome.core_service.authsome.impl.AuthsomeServiceImpl;
import dev.kuku.authsome.core_service.authsome.impl.AuthsomeUserIdentityJpaRepo;
import dev.kuku.authsome.core_service.authsome.impl.AuthsomeUserJpaRepo;
import dev.kuku.authsome.core_service.authsome.impl.entity.AuthsomeUserEntity;
import dev.kuku.authsome.core_service.authsome.impl.entity.AuthsomeUserIdentityEntity;
import dev.kuku.authsome.core_service.project.api.dto.IdentityType;
import dev.kuku.authsome.util_service.jwt.api.JwtService;
import dev.kuku.authsome.util_service.jwt.api.dto.TokenData;
import dev.kuku.authsome.util_service.jwt.api.exception.ExpiredJwtToken;
import dev.kuku.authsome.util_service.jwt.api.exception.InvalidJwtToken;
import dev.kuku.authsome.util_service.notifier.api.NotifierService;
import dev.kuku.authsome.util_service.otp.api.OtpService;
import dev.kuku.authsome.util_service.otp.api.dto.OtpToFetch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Example;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthsomeServiceImplSignupTest {

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private AuthsomeUserJpaRepo userJpaRepo;

    @Mock
    private AuthsomeUserIdentityJpaRepo identityJpaRepo;

    @Mock
    private JwtService jwtService;

    @Mock
    private OtpService otpService;

    @Mock
    private NotifierService notifierService;

    @InjectMocks
    private AuthsomeServiceImpl authsomeService;

    @BeforeEach
    void setUp() {
        // Set up configuration values using ReflectionTestUtils
        ReflectionTestUtils.setField(authsomeService, "otpType", "NUMERIC");
        ReflectionTestUtils.setField(authsomeService, "otpLength", 6);
        ReflectionTestUtils.setField(authsomeService, "otpMinNumAlphanumeric", 2);
        ReflectionTestUtils.setField(authsomeService, "OtpMinCharAlphanumeric", 3);
        ReflectionTestUtils.setField(authsomeService, "otpExpiry", 5);
        ReflectionTestUtils.setField(authsomeService, "otpExpiryUnitString", "MINUTES");
        ReflectionTestUtils.setField(authsomeService, "OtpExpiryUnit", TimeUnit.MINUTES);
    }

    @Test
    void startSignupProcess_Success_NumericOtp() throws AuthsomeUsernameAlreadyInUse, InvalidOtpTypeException, AuthsomeIdentityAlreadyInUse {
        // Arrange
        String username = "testuser";
        String password = "password123";
        String identity = "test@example.com";
        IdentityType identityType = IdentityType.EMAIL;
        int generatedOtp = 123456;
        String otpId = "otp-id-123";
        String token = "jwt-token-123";

        when(userJpaRepo.exists(any(Example.class))).thenReturn(false);
        when(identityJpaRepo.exists(any(Example.class))).thenReturn(false);
        when(otpService.generateNumericOtp(6)).thenReturn(generatedOtp);
        when(passwordEncoder.encode(password)).thenReturn("hashed-password");
        when(otpService.saveOtpWithCustomData(anyString(), anyMap(), eq(5), eq(TimeUnit.MINUTES)))
                .thenReturn(new OtpToFetch(otpId, String.valueOf(generatedOtp), Map.of()));
        when(jwtService.generateToken(eq(otpId), isNull(), eq(5), eq(TimeUnit.MINUTES)))
                .thenReturn(token);

        // Act
        String result = authsomeService.startSignupProcess(identityType, identity, username, password);

        // Assert
        assertNotNull(result);
        assertEquals(token, result);
        verify(userJpaRepo).exists(any(Example.class));
        verify(identityJpaRepo).exists(any(Example.class));
        verify(otpService).generateNumericOtp(6);
        verify(otpService).saveOtpWithCustomData(eq(String.valueOf(generatedOtp)), argThat(map ->
                map.get("username").equals(username) &&
                map.get("identityType").equals(identityType) &&
                map.get("identityValue").equals(identity) &&
                map.get("password").equals("hashed-password")
        ), eq(5), eq(TimeUnit.MINUTES));
        verify(notifierService).sendNotificationToIdentity(
                eq(identityType),
                eq(identity),
                anyString(),
                contains(String.valueOf(generatedOtp))
        );
    }

    @Test
    void startSignupProcess_AlphanumericOtp_Success() throws AuthsomeUsernameAlreadyInUse, InvalidOtpTypeException, AuthsomeIdentityAlreadyInUse {
        // Arrange
        ReflectionTestUtils.setField(authsomeService, "otpType", "ALPHANUMERIC");
        String username = "testuser";
        String password = "password123";
        String identity = "test@example.com";
        IdentityType identityType = IdentityType.EMAIL;
        String generatedOtp = "AB12CD";
        String otpId = "otp-id-123";
        String token = "jwt-token-123";

        when(userJpaRepo.exists(any(Example.class))).thenReturn(false);
        when(identityJpaRepo.exists(any(Example.class))).thenReturn(false);
        when(otpService.generateAlphaNumericOtp(6, 3, 2)).thenReturn(generatedOtp);
        when(passwordEncoder.encode(password)).thenReturn("hashed-password");
        when(otpService.saveOtpWithCustomData(anyString(), anyMap(), eq(5), eq(TimeUnit.MINUTES)))
                .thenReturn(new OtpToFetch(otpId, generatedOtp, Map.of()));
        when(jwtService.generateToken(eq(otpId), isNull(), eq(5), eq(TimeUnit.MINUTES)))
                .thenReturn(token);

        // Act
        String result = authsomeService.startSignupProcess(identityType, identity, username, password);

        // Assert
        assertNotNull(result);
        assertEquals(token, result);
        verify(otpService).generateAlphaNumericOtp(6, 3, 2);
    }

    @Test
    void startSignupProcess_AlphabeticOtp_Success() throws AuthsomeUsernameAlreadyInUse, InvalidOtpTypeException, AuthsomeIdentityAlreadyInUse {
        // Arrange
        ReflectionTestUtils.setField(authsomeService, "otpType", "ALPHABETIC");
        String username = "testuser";
        String password = "password123";
        String identity = "test@example.com";
        IdentityType identityType = IdentityType.EMAIL;
        String generatedOtp = "ABCDEF";
        String otpId = "otp-id-123";
        String token = "jwt-token-123";

        when(userJpaRepo.exists(any(Example.class))).thenReturn(false);
        when(identityJpaRepo.exists(any(Example.class))).thenReturn(false);
        when(otpService.generateAlphabeticOtp(6)).thenReturn(generatedOtp);
        when(passwordEncoder.encode(password)).thenReturn("hashed-password");
        when(otpService.saveOtpWithCustomData(anyString(), anyMap(), eq(5), eq(TimeUnit.MINUTES)))
                .thenReturn(new OtpToFetch(otpId, generatedOtp, Map.of()));
        when(jwtService.generateToken(eq(otpId), isNull(), eq(5), eq(TimeUnit.MINUTES)))
                .thenReturn(token);

        // Act
        String result = authsomeService.startSignupProcess(identityType, identity, username, password);

        // Assert
        assertNotNull(result);
        verify(otpService).generateAlphabeticOtp(6);
    }

    @Test
    void startSignupProcess_UsernameAlreadyInUse_ThrowsException() {
        // Arrange
        String username = "existinguser";
        String password = "password123";
        String identity = "test@example.com";
        IdentityType identityType = IdentityType.EMAIL;

        when(userJpaRepo.exists(any(Example.class))).thenReturn(true);

        // Act & Assert
        assertThrows(AuthsomeUsernameAlreadyInUse.class, () ->
                authsomeService.startSignupProcess(identityType, identity, username, password)
        );
        verify(userJpaRepo).exists(any(Example.class));
        verify(identityJpaRepo, never()).exists(any());
        verify(otpService, never()).generateNumericOtp(anyInt());
    }

    @Test
    void startSignupProcess_IdentityAlreadyInUse_ThrowsException() {
        // Arrange
        String username = "testuser";
        String password = "password123";
        String identity = "existing@example.com";
        IdentityType identityType = IdentityType.EMAIL;

        when(userJpaRepo.exists(any(Example.class))).thenReturn(false);
        when(identityJpaRepo.exists(any(Example.class))).thenReturn(true);

        // Act & Assert
        assertThrows(AuthsomeIdentityAlreadyInUse.class, () ->
                authsomeService.startSignupProcess(identityType, identity, username, password)
        );
        verify(userJpaRepo).exists(any(Example.class));
        verify(identityJpaRepo).exists(any(Example.class));
        verify(otpService, never()).generateNumericOtp(anyInt());
    }

    @Test
    void startSignupProcess_InvalidOtpType_ThrowsException() {
        // Arrange
        ReflectionTestUtils.setField(authsomeService, "otpType", "INVALID");
        String username = "testuser";
        String password = "password123";
        String identity = "test@example.com";
        IdentityType identityType = IdentityType.EMAIL;

        when(userJpaRepo.exists(any(Example.class))).thenReturn(false);
        when(identityJpaRepo.exists(any(Example.class))).thenReturn(false);

        // Act & Assert
        assertThrows(InvalidOtpTypeException.class, () ->
                authsomeService.startSignupProcess(identityType, identity, username, password)
        );
    }

    @Test
    void completeSignupProcess_Success() throws InvalidJwtToken, ExpiredJwtToken, AuthsomeUsernameAlreadyInUse, OtpNotFoundInDatabase, OtpMismatchException, AuthsomeIdentityAlreadyInUse {
        // Arrange
        String token = "jwt-token-123";
        String otp = "123456";
        String otpId = "otp-id-123";
        String username = "testuser";
        String hashedPassword = "hashed-password";
        String identity = "test@example.com";
        IdentityType identityType = IdentityType.EMAIL;
        String userId = "user-id-123";

        TokenData tokenData = new TokenData(otpId, null);
        OtpToFetch otpToFetch = new OtpToFetch(
                otpId,
                otp,
                Map.of(
                        "username", username,
                        "password", hashedPassword,
                        "identityValue", identity,
                        "identityType", identityType.name()
                )
        );
        AuthsomeUserEntity savedUser = new AuthsomeUserEntity(
                userId, username, hashedPassword, Instant.now().toEpochMilli(), Instant.now().toEpochMilli()
        );

        when(jwtService.extractToken(token)).thenReturn(tokenData);
        when(otpService.getOtpById(otpId)).thenReturn(otpToFetch);
        when(userJpaRepo.exists(any(Example.class))).thenReturn(false);
        when(identityJpaRepo.exists(any(Example.class))).thenReturn(false);
        when(userJpaRepo.save(any(AuthsomeUserEntity.class))).thenReturn(savedUser);
        when(identityJpaRepo.save(any(AuthsomeUserIdentityEntity.class)))
                .thenReturn(new AuthsomeUserIdentityEntity());

        // Act
        authsomeService.completeSignupProcess(token, otp);

        // Assert
        verify(jwtService).extractToken(token);
        verify(otpService).getOtpById(otpId);
        verify(userJpaRepo).exists(any(Example.class));
        verify(identityJpaRepo).exists(any(Example.class));
        verify(userJpaRepo).save(argThat(user ->
                user.username.equals(username) &&
                user.hashedPassword.equals(hashedPassword)
        ));
        verify(identityJpaRepo).save(argThat(userIdentity ->
                userIdentity.user.equals(savedUser) &&
                userIdentity.identityType.equals(identityType) &&
                userIdentity.identity.equals(identity)
        ));
        verify(otpService).deleteById(otpId);
    }

    @Test
    void completeSignupProcess_InvalidToken_ThrowsException() throws InvalidJwtToken, ExpiredJwtToken {
        // Arrange
        String token = "invalid-token";
        String otp = "123456";

        when(jwtService.extractToken(token)).thenThrow(new InvalidJwtToken());

        // Act & Assert
        assertThrows(InvalidJwtToken.class, () ->
                authsomeService.completeSignupProcess(token, otp)
        );
        verify(jwtService).extractToken(token);
        verify(otpService, never()).getOtpById(anyString());
    }

    @Test
    void completeSignupProcess_ExpiredToken_ThrowsException() throws InvalidJwtToken, ExpiredJwtToken {
        // Arrange
        String token = "expired-token";
        String otp = "123456";

        when(jwtService.extractToken(token)).thenThrow(new ExpiredJwtToken());

        // Act & Assert
        assertThrows(ExpiredJwtToken.class, () ->
                authsomeService.completeSignupProcess(token, otp)
        );
        verify(jwtService).extractToken(token);
        verify(otpService, never()).getOtpById(anyString());
    }

    @Test
    void completeSignupProcess_MissingSubject_ThrowsException() throws InvalidJwtToken, ExpiredJwtToken {
        // Arrange
        String token = "jwt-token-123";
        String otp = "123456";

        TokenData tokenData = new TokenData(null, null);
        when(jwtService.extractToken(token)).thenReturn(tokenData);

        // Act & Assert
        assertThrows(InvalidJwtToken.class, () ->
                authsomeService.completeSignupProcess(token, otp)
        );
        verify(jwtService).extractToken(token);
        verify(otpService, never()).getOtpById(anyString());
    }

    @Test
    void completeSignupProcess_OtpNotFound_ThrowsException() throws InvalidJwtToken, ExpiredJwtToken {
        // Arrange
        String token = "jwt-token-123";
        String otp = "123456";
        String otpId = "otp-id-123";

        TokenData tokenData = new TokenData(otpId, null);
        when(jwtService.extractToken(token)).thenReturn(tokenData);
        when(otpService.getOtpById(otpId)).thenReturn(null);

        // Act & Assert
        assertThrows(OtpNotFoundInDatabase.class, () ->
                authsomeService.completeSignupProcess(token, otp)
        );
        verify(otpService).getOtpById(otpId);
        verify(userJpaRepo, never()).save(any());
    }

    @Test
    void completeSignupProcess_OtpMismatch_ThrowsException() throws InvalidJwtToken, ExpiredJwtToken {
        // Arrange
        String token = "jwt-token-123";
        String otp = "123456";
        String wrongOtp = "654321";
        String otpId = "otp-id-123";

        TokenData tokenData = new TokenData(otpId, null);
        OtpToFetch otpToFetch = new OtpToFetch(otpId, wrongOtp, Map.of());

        when(jwtService.extractToken(token)).thenReturn(tokenData);
        when(otpService.getOtpById(otpId)).thenReturn(otpToFetch);

        // Act & Assert
        assertThrows(OtpMismatchException.class, () ->
                authsomeService.completeSignupProcess(token, otp)
        );
        verify(otpService).getOtpById(otpId);
        verify(userJpaRepo, never()).save(any());
    }

    @Test
    void completeSignupProcess_UsernameAlreadyInUse_ThrowsException() throws InvalidJwtToken, ExpiredJwtToken {
        // Arrange
        String token = "jwt-token-123";
        String otp = "123456";
        String otpId = "otp-id-123";
        String username = "existinguser";

        TokenData tokenData = new TokenData(otpId, null);
        OtpToFetch otpToFetch = new OtpToFetch(
                otpId,
                otp,
                Map.of(
                        "username", username,
                        "password", "hashed-password",
                        "identityValue", "test@example.com",
                        "identityType", IdentityType.EMAIL.name()
                )
        );

        when(jwtService.extractToken(token)).thenReturn(tokenData);
        when(otpService.getOtpById(otpId)).thenReturn(otpToFetch);
        when(userJpaRepo.exists(any(Example.class))).thenReturn(true);

        // Act & Assert
        assertThrows(AuthsomeUsernameAlreadyInUse.class, () ->
                authsomeService.completeSignupProcess(token, otp)
        );
        verify(userJpaRepo, never()).save(any());
    }
}