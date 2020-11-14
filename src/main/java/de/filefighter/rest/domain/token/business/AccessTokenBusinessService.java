package de.filefighter.rest.domain.token.business;

import de.filefighter.rest.domain.token.data.dto.AccessToken;
import de.filefighter.rest.domain.token.data.persistance.AccessTokenEntity;
import de.filefighter.rest.domain.token.data.persistance.AccessTokenRepository;
import de.filefighter.rest.domain.token.exceptions.AccessTokenNotFoundException;
import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.exceptions.UserNotAuthenticatedException;
import de.filefighter.rest.rest.exceptions.RequestDidntMeetFormalRequirementsException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

import static de.filefighter.rest.configuration.RestConfiguration.AUTHORIZATION_BASIC_PREFIX;
import static de.filefighter.rest.configuration.RestConfiguration.AUTHORIZATION_BEARER_PREFIX;
import static de.filefighter.rest.domain.common.Utils.stringIsValid;
import static de.filefighter.rest.domain.common.Utils.validateAuthorizationHeader;

@Service
public class AccessTokenBusinessService {

    private final AccessTokenRepository accessTokenRepository;
    private final AccessTokenDtoService accessTokenDtoService;

    public static final long ACCESS_TOKEN_DURATION_IN_SECONDS = 3600L;
    public static final long ACCESS_TOKEN_SAFETY_MARGIN = 5L;

    public AccessTokenBusinessService(AccessTokenRepository accessTokenRepository, AccessTokenDtoService accessTokenDtoService) {
        this.accessTokenRepository = accessTokenRepository;
        this.accessTokenDtoService = accessTokenDtoService;
    }

    public AccessToken getValidAccessTokenForUser(User user) {
        AccessTokenEntity accessTokenEntity = accessTokenRepository.findByUserId(user.getId());
        long currentTimeSeconds = Instant.now().getEpochSecond();

        if (null == accessTokenEntity) {
            accessTokenEntity = AccessTokenEntity
                    .builder()
                    .validUntil(currentTimeSeconds + ACCESS_TOKEN_DURATION_IN_SECONDS)
                    .value(this.generateRandomTokenValue())
                    .userId(user.getId())
                    .build();
            accessTokenEntity = accessTokenRepository.save(accessTokenEntity);
        } else {
            if (currentTimeSeconds + ACCESS_TOKEN_SAFETY_MARGIN > accessTokenEntity.getValidUntil()) {
                accessTokenRepository.delete(accessTokenEntity);
                accessTokenEntity = AccessTokenEntity
                        .builder()
                        .validUntil(currentTimeSeconds + ACCESS_TOKEN_DURATION_IN_SECONDS)
                        .value(this.generateRandomTokenValue())
                        .userId(user.getId())
                        .build();
                accessTokenEntity = accessTokenRepository.save(accessTokenEntity);
            }
        }

        return accessTokenDtoService.createDto(accessTokenEntity);
    }

    public AccessToken findAccessTokenByValueAndUserId(String accessTokenValue, long userId) {
        if (!stringIsValid(accessTokenValue))
            throw new RequestDidntMeetFormalRequirementsException("Value of AccessToken was not valid.");

        AccessTokenEntity accessTokenEntity = accessTokenRepository.findByUserIdAndValue(userId, accessTokenValue);
        if (null == accessTokenEntity)
            throw new UserNotAuthenticatedException(userId);

        return accessTokenDtoService.createDto(accessTokenEntity);
    }

    public AccessToken findAccessTokenByValue(String accessTokenValue) {
        if (!stringIsValid(accessTokenValue))
            throw new RequestDidntMeetFormalRequirementsException("Value of AccessToken was not valid.");

        AccessTokenEntity accessTokenEntity = accessTokenRepository.findByValue(accessTokenValue);
        if (null == accessTokenEntity)
            throw new UserNotAuthenticatedException("AccessToken not found.");

        return accessTokenDtoService.createDto(accessTokenEntity);
    }


    public AccessToken validateAccessTokenValue(String accessTokenValue) {
        String cleanValue = validateAuthorizationHeader(AUTHORIZATION_BEARER_PREFIX, accessTokenValue);
        AccessTokenEntity accessTokenEntity = accessTokenRepository.findByValue(cleanValue);
        if (null == accessTokenEntity)
            throw new UserNotAuthenticatedException("AccessToken not found.");

        return accessTokenDtoService.createDto(accessTokenEntity);
    }

    public String generateRandomTokenValue() {
        return UUID.randomUUID().toString();
    }

    public long getAccessTokenCount() {
        return accessTokenRepository.count();
    }
}
