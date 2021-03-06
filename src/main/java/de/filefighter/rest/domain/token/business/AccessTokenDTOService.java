package de.filefighter.rest.domain.token.business;

import de.filefighter.rest.domain.common.DTOServiceInterface;
import de.filefighter.rest.domain.token.data.dto.AccessToken;
import de.filefighter.rest.domain.token.data.persistence.AccessTokenEntity;
import de.filefighter.rest.domain.token.data.persistence.AccessTokenRepository;
import de.filefighter.rest.domain.token.exceptions.AccessTokenNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AccessTokenDTOService implements DTOServiceInterface<AccessToken, AccessTokenEntity> {

    private final AccessTokenRepository accessTokenRepository;

    public AccessTokenDTOService(AccessTokenRepository accessTokenRepository) {
        this.accessTokenRepository = accessTokenRepository;
    }

    @Override
    public AccessToken createDto(AccessTokenEntity entity) {
        return AccessToken
                .builder()
                .tokenValue(entity.getValue())
                .userId(entity.getUserId())
                .validUntil(entity.getValidUntil())
                .build();
    }

    @Override
    public AccessTokenEntity findEntity(AccessToken dto) {
        AccessTokenEntity accessTokenEntity = accessTokenRepository.findByUserIdAndValue(dto.getUserId(), dto.getTokenValue());
        if (null == accessTokenEntity)
            throw new AccessTokenNotFoundException(dto.getUserId());

        return accessTokenEntity;
    }
}
