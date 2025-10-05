package dev.kuku.auth_some.core_service.authsome.internal;

import dev.kuku.auth_some.core_service.authsome.api.AuthsomeService;
import dev.kuku.auth_some.core_service.authsome.api.dto.AuthsomeUserToFetch;
import dev.kuku.auth_some.core_service.authsome.api.dto.SignInTokens;
import dev.kuku.auth_some.core_service.authsome.api.exceptions.AuthsomeIdentityAlreadyInUse;
import dev.kuku.auth_some.core_service.authsome.api.exceptions.AuthsomeUsernameAlreadyInUse;
import dev.kuku.auth_some.core_service.authsome.api.exceptions.OtpMismatchException;
import dev.kuku.auth_some.core_service.authsome.internal.entity.AuthsomeUserEntity;
import dev.kuku.auth_some.core_service.authsome.internal.entity.AuthsomeUserIdentityEntity;
import dev.kuku.auth_some.core_service.project.api.dto.IdentityType;
import dev.kuku.auth_some.util_service.jwt.api.JwtService;
import dev.kuku.auth_some.util_service.jwt.api.dto.TokenData;
import dev.kuku.auth_some.util_service.jwt.api.exception.ExpiredJwtToken;
import dev.kuku.auth_some.util_service.jwt.api.exception.InvalidJwtToken;
import dev.kuku.auth_some.util_service.notifier.api.NotifierService;
import dev.kuku.auth_some.util_service.otp.api.OtpService;
import dev.kuku.auth_some.util_service.otp.api.dto.OtpToFetch;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Example;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthsomeServiceImpl implements AuthsomeService {
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthsomeUserJpaRepo userJpaRepo;
    private final AuthsomeUserIdentityJpaRepo identityJpaRepo;
    private final JwtService jwtService;
    private final OtpService otpService;
    private final NotifierService notifierService;

    @Override
    @Transactional
    public String startSignupProcess(IdentityType identityType, String identity, String username, String password) throws AuthsomeUsernameAlreadyInUse, AuthsomeIdentityAlreadyInUse {
        //TODO password basic requirements
        validateUsernameAvailability(username);
        validateIdentityAvailability(identityType, identity);

        log.trace("startSignupProcess({}, {}, {}, {})", identityType, identity, username, password.substring(5));
        int otp = otpService.generateNumericOtp(5);
        log.debug("otp: {}", otp);
        OtpToFetch saved = otpService.saveOtpWithCustomData(String.valueOf(otp),
                Map.of(
                        "username", username,
                        "identityType", identityType,
                        "identityValue", identity,
                        "password", passwordEncoder.encode(password)
                ),
                5,
                TimeUnit.MINUTES);
        log.debug("saved: {}", saved);
        String token = jwtService.generateToken(saved.id, null, 5, TimeUnit.MINUTES);
        log.debug("token: {}...", token.substring(0, 5));
        notifierService.sendNotificationToIdentity(identityType, identity, "Authsome : OTP for signing up", "Your OTP for signing up for Authsome is " + otp + ". Expires in " + 5 + " " + TimeUnit.MINUTES.name());
        return token;
    }

    @Override
    @Transactional
    public void completeSignupProcess(String token, String otp) throws InvalidJwtToken, ExpiredJwtToken, OtpMismatchException, AuthsomeUsernameAlreadyInUse, AuthsomeIdentityAlreadyInUse {
        log.trace("completeSignupProcess({}..., {}..)", token.substring(0, 5), otp.substring(0, 3));
        TokenData tokenData = jwtService.extractToken(token);
        if (!StringUtils.hasText(tokenData.subject())) {
            log.error("tokenData.subject is missing");
            throw new InvalidJwtToken();
        }
        OtpToFetch fetchedOtp = otpService.getOtpById(tokenData.subject());
        if (fetchedOtp == null) {
            //TODO proper exception
            throw new RuntimeException("OTP not found in database");
        }
        if (!fetchedOtp.otp.equals(otp)) {
            log.error("OTP {}... did not match {}...", otp.substring(0, 3), fetchedOtp.otp.substring(0, 3));
            throw new OtpMismatchException();
        }

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
    public SignInTokens signIn(IdentityType identityType, String identityValue, String password) {
        return null;
    }

    @Override
    public AuthsomeUserToFetch getAuthsomeUser(String id, String username) {
        return null;
    }

    /**
     * Validates that the username is not already taken.
     *
     * @param username the username to validate
     * @throws AuthsomeUsernameAlreadyInUse if the username already exists
     */
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
     * @param identity the identity value to validate
     * @throws AuthsomeIdentityAlreadyInUse if the identity already exists
     */
    private void validateIdentityAvailability(IdentityType identityType, String identity) throws AuthsomeIdentityAlreadyInUse {
        var identityFilter = Example.of(new AuthsomeUserIdentityEntity(null, null, identityType, identity, null, null));
        if (identityJpaRepo.exists(identityFilter)) {
            log.error("Identity already in use {} -> {}", identityType, identity);
            throw new AuthsomeIdentityAlreadyInUse(identityType, identity);
        }
    }
}