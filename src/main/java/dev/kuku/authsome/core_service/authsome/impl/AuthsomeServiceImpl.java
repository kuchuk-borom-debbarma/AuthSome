package dev.kuku.authsome.core_service.authsome.impl;

import dev.kuku.authsome.core_service.authsome.api.AuthsomeService;
import dev.kuku.authsome.core_service.authsome.api.dto.AuthsomeUserToFetch;
import dev.kuku.authsome.core_service.authsome.api.dto.SignInTokens;
import dev.kuku.authsome.core_service.authsome.api.exceptions.*;
import dev.kuku.authsome.core_service.authsome.impl.entity.AuthsomeUserEntity;
import dev.kuku.authsome.core_service.authsome.impl.entity.AuthsomeUserIdentityEntity;
import dev.kuku.authsome.core_service.authsome.impl.entity.AuthsomeUserRefreshTokenEntity;
import dev.kuku.authsome.core_service.project.api.dto.IdentityType;
import dev.kuku.authsome.util_service.jwt.api.JwtService;
import dev.kuku.authsome.util_service.jwt.api.dto.TokenData;
import dev.kuku.authsome.util_service.jwt.api.exception.ExpiredJwtToken;
import dev.kuku.authsome.util_service.jwt.api.exception.InvalidJwtToken;
import dev.kuku.authsome.util_service.notifier.api.NotifierService;
import dev.kuku.authsome.util_service.otp.api.OtpService;
import dev.kuku.authsome.util_service.otp.api.dto.OtpToFetch;
import dev.kuku.vfl.api.annotation.SubBlock;
import dev.kuku.vfl.api.annotation.VFLAnnotation;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthsomeServiceImpl implements AuthsomeService {
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthsomeUserJpaRepo userJpaRepo;
    private final AuthsomeUserIdentityJpaRepo identityJpaRepo;
    private final AuthsomeRefreshTokenJpaRepo refreshTokenJpaRepo;
    private final JwtService jwtService;
    private final OtpService otpService;
    private final NotifierService notifierService;
    private final VFLAnnotation log;

    @Value("${authsome.user.signup.otp.type}")
    private String otpType = "NUMERIC";

    @Value("${authsome.user.signup.otp.length}")
    private int otpLength = 5;

    @Value("${authsome.user.signup.otp.minNumAlphanumeric}")
    private int otpMinNumAlphanumeric = 2;

    @Value("${authsome.user.signup.otp.minCharAlphanumeric}")
    private int OtpMinCharAlphanumeric = 3;

    @Value("${authsome.user.signup.otp.expiry}")
    private int otpExpiry = 5;

    @Value("${authsome.user.signup.otp.expiryUnit}")
    private String otpExpiryUnitString = "MINUTES";
    private TimeUnit otpExpiryUnit;

    @Value("${authsome.user.signin.jwt_expiry}")
    private int authsomeSignInJwtExpiry = 5;
    @Value("${authsome.user.signin.jwt_expiry_unit}")
    private String authsomeSignInJwtExpiryUnitRaw = "MINUTES";
    private TimeUnit authsomeSignInJwtExpiryUnit = TimeUnit.MINUTES;
    @Value("${authsome.user.max_sessions}")
    private int maxActiveSession = 3;

    @PostConstruct
    private void initTimeUnit() {
        otpExpiryUnit = TimeUnit.valueOf(otpExpiryUnitString);
        authsomeSignInJwtExpiryUnit = TimeUnit.valueOf(authsomeSignInJwtExpiryUnitRaw);
    }

    @Override
    @Transactional
    @SubBlock
    public String startSignupProcess(IdentityType identityType, String identity, String username, String password) throws AuthsomeUsernameAlreadyInUse, AuthsomeIdentityAlreadyInUse, InvalidOtpTypeException {
        log.info("startSignupProcess({}, {}, {}, {})", identityType, identity, username, password.substring(5));

        //TODO password basic requirements
        validateUsernameAvailability(username);
        validateIdentityAvailability(identityType, identity);
        String otp = switch (otpType) {
            case "NUMERIC" -> String.valueOf(otpService.generateNumericOtp(otpLength));
            case "ALPHANUMERIC" ->
                    otpService.generateAlphaNumericOtp(otpLength, OtpMinCharAlphanumeric, otpMinNumAlphanumeric);
            case "ALPHABETIC" -> otpService.generateAlphabeticOtp(otpLength);
            default -> throw new InvalidOtpTypeException(otpType);
        };
        log.info("otp: {}", otp);
        OtpToFetch saved = otpService.saveOtpWithCustomData(otp,
                Map.of(
                        "username", username,
                        "identityType", identityType,
                        "identityValue", identity,
                        "password", passwordEncoder.encode(password)
                ),
                otpExpiry,
                otpExpiryUnit);
        log.info("saved: {}", saved);
        String token = jwtService.generateToken(saved.id, null, otpExpiry, otpExpiryUnit);
        log.info("token: {}...", token.substring(0, 5));
        notifierService.sendNotificationToIdentity(identityType, identity, "Authsome : OTP for signing up", "Your OTP for signing up for Authsome is " + otp + ". Expires in " + otpExpiry + " " + otpExpiryUnit.name());
        return token;
    }

    @Override
    @Transactional
    @SubBlock
    public void completeSignupProcess(String token, String otp) throws InvalidJwtToken, ExpiredJwtToken, OtpMismatchException, AuthsomeUsernameAlreadyInUse, AuthsomeIdentityAlreadyInUse, OtpNotFoundInDatabase {
        log.info("completeSignupProcess({}..., {}..)", token.substring(0, 5), otp.substring(0, 3));
        //Extract otp record id which is stored as subject, retrieve it, validate OTP
        TokenData tokenData = jwtService.extractToken(token);
        if (!StringUtils.hasText(tokenData.subject())) {
            log.error("tokenData.subject is missing");
            throw new InvalidJwtToken();
        }
        OtpToFetch fetchedOtp = otpService.getOtpById(tokenData.subject());
        if (fetchedOtp == null) {
            throw new OtpNotFoundInDatabase();
        }
        if (!fetchedOtp.otp.equals(otp)) {
            log.error("OTP {}... did not match {}...", otp.substring(0, 3), fetchedOtp.otp.substring(0, 3));
            throw new OtpMismatchException();
        }
        //extract the user data from otp custom data, save it in users db, optional: delete otp rows or let cron job handle it
        String username = fetchedOtp.customData.get("username").toString();
        String password = fetchedOtp.customData.get("password").toString();
        String identity = fetchedOtp.customData.get("identityValue").toString();
        IdentityType identityType = IdentityType.valueOf(fetchedOtp.customData.get("identityType").toString());

        validateUsernameAvailability(username);
        validateIdentityAvailability(identityType, identity);

        var savedUser = userJpaRepo.save(new AuthsomeUserEntity(null, username, password, Instant.now().toEpochMilli(), Instant.now().toEpochMilli()));
        identityJpaRepo.save(new AuthsomeUserIdentityEntity(null, savedUser, identityType, identity, Instant.now().toEpochMilli(), Instant.now().toEpochMilli()));
        //OPTION : We can change the schema of OTP to be more specific and hold the identity type and identity value separate and delete ALL records which match this requirement
        otpService.deleteById(fetchedOtp.id);
    }

    @Override
    @SubBlock
    @Transactional
    public SignInTokens signIn(IdentityType identityType, String identityValue, String password) throws AuthsomeUserWithIdentityNotFound, AuthsomePasswordMismatch, MaxActiveSessionsReached {
        log.info("signIn({}, {}, {}...)", identityType, identityValue, password.substring(2, 6));
        //1. Find the user from identity
        var filters = Example.of(new AuthsomeUserIdentityEntity(
                null, null, identityType, identityValue, null, null
        ));
        Optional<AuthsomeUserIdentityEntity> userIdentityOptional = identityJpaRepo.findOne(filters);
        if (userIdentityOptional.isEmpty()) {
            log.error("user with identity not found");
            throw new AuthsomeUserWithIdentityNotFound(identityType, identityValue);
        }
        AuthsomeUserIdentityEntity userIdentity = userIdentityOptional.get();
        AuthsomeUserEntity user = userIdentity.user;
        //Check if maxSessions reached
        var userFilterRule = new AuthsomeUserEntity();
        userFilterRule.id = user.id;
        var refreshFilterRule = new AuthsomeUserRefreshTokenEntity();
        refreshFilterRule.user = userFilterRule;
        long count = refreshTokenJpaRepo.count(Example.of(refreshFilterRule));
        if (count >= maxActiveSession) {
            throw new MaxActiveSessionsReached(user.id, maxActiveSession);
        }
        if (!passwordEncoder.matches(password, user.hashedPassword)) {
            log.error("password does not match");
            throw new AuthsomePasswordMismatch(identityType, identityValue);
        }
        String accessToken = generateUserAccessToken(user.id);
        AuthsomeUserRefreshTokenEntity savedRefreshToken = generateUserRefreshToken(user.id, null);
        String refreshToken = savedRefreshToken.id;
        return new SignInTokens(accessToken, refreshToken);
    }

    @Override
    @SubBlock
    public SignInTokens refreshToken(String refreshToken) throws InvalidRefreshToken {
        log.info("refreshToken({}...)", refreshToken.substring(0, 5));
        //1. Fetch the refresh token
        AuthsomeUserRefreshTokenEntity currentRt = refreshTokenJpaRepo.findById(refreshToken).orElse(null);
        if (currentRt == null) {
            throw new InvalidRefreshToken(refreshToken);
        }
        AuthsomeUserEntity authsomeUser = currentRt.user;
        //2. Generate access token for the user
        String accessToken = generateUserAccessToken(authsomeUser.id);
        //3. Generate new refresh token
        AuthsomeUserRefreshTokenEntity newRefreshToken = generateUserRefreshToken(authsomeUser.id, null);
        //4. Delete current RT
        refreshTokenJpaRepo.deleteById(currentRt.id);
        return new SignInTokens(accessToken, newRefreshToken.id);
    }

    @Override
    @SubBlock
    @Transactional
    public void revokeRefreshToken(String refreshToken) {
        log.info("revokeRefreshToken({}...)", refreshToken.substring(0, 5));
        refreshTokenJpaRepo.deleteById(refreshToken);
    }

    @Override
    @SubBlock
    @Transactional
    public void revokeAllRefreshTokenOfUser(String userId) {
        log.info("revokeAllRefreshTokenOfUser({}...)", userId);
        refreshTokenJpaRepo.deleteAllByUserId(userId);
    }

    @SubBlock
    @Override
    public AuthsomeUserToFetch getAuthsomeUser(String id, String username) throws BadRequestException {
        log.info("getAuthsomeUser({}, {})", id, username);
        if (!StringUtils.hasText(id) && !StringUtils.hasText(username)) {
            throw new BadRequestException("Both ID and Username can't be null!");
        }
        var filter = Example.of(
                new AuthsomeUserEntity(id, username, null, null, null)
        );
        var user = userJpaRepo.findOne(filter).orElse(null);
        if (user == null) {
            return null;
        }
        return new AuthsomeUserToFetch(user.id, user.username);
    }

    /**
     * Validates that the username is not already taken.
     *
     * @param username the username to validate
     * @throws AuthsomeUsernameAlreadyInUse if the username already exists
     */
    @SubBlock
    private void validateUsernameAvailability(String username) throws AuthsomeUsernameAlreadyInUse {
        var filter = Example.of(new AuthsomeUserEntity(null, username, null, null, null));
        if (userJpaRepo.exists(filter)) {
            log.error("username {} already in use. Aborting signup process", username);
            throw new AuthsomeUsernameAlreadyInUse(username);
        }
    }

    /**
     * Validates that the identity (email/phone) is not already claimed.
     *
     * @param identityType the type of identity (EMAIL, PHONE, etc.)
     * @param identity     the identity value to validate
     * @throws AuthsomeIdentityAlreadyInUse if the identity already exists
     */
    @SubBlock
    private void validateIdentityAvailability(IdentityType identityType, String identity) throws AuthsomeIdentityAlreadyInUse {
        var identityFilter = Example.of(new AuthsomeUserIdentityEntity(null, null, identityType, identity, null, null));
        if (identityJpaRepo.exists(identityFilter)) {
            log.error("Identity already in use {} -> {}", identityType, identity);
            throw new AuthsomeIdentityAlreadyInUse(identityType, identity);
        }
    }

    @SubBlock
    private String generateUserAccessToken(String userId) {
        return jwtService.generateToken(userId,
                Map.of(
                        "type", "authsome-user"
                ),
                authsomeSignInJwtExpiry,
                authsomeSignInJwtExpiryUnit);
    }

    /**
     * Save new refresh token for the user and metadata and return it
     *
     * @param userId   id of the user to generate for
     * @param metadata metadata to store
     * @return record saved in database
     */
    @SubBlock
    private AuthsomeUserRefreshTokenEntity generateUserRefreshToken(String userId, Map<String, Object> metadata) {
        log.info("generateUserRefreshToken({})", userId);
        return refreshTokenJpaRepo.save(new AuthsomeUserRefreshTokenEntity(
                null,
                new AuthsomeUserEntity(userId, null, null, null, null),
                Instant.now().toEpochMilli(),
                Instant.now().toEpochMilli(),
                authsomeSignInJwtExpiry,
                authsomeSignInJwtExpiryUnit,
                metadata
        ));
    }
}