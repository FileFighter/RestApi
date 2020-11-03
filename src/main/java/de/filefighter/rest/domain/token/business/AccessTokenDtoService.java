package de.filefighter.rest.domain.token.business;

import de.filefighter.rest.domain.common.DtoServiceInterface;
import de.filefighter.rest.domain.token.data.dto.AccessToken;
import de.filefighter.rest.domain.token.data.persistance.AccessTokenEntity;
import de.filefighter.rest.domain.token.data.persistance.AccessTokenRepository;
import de.filefighter.rest.domain.token.exceptions.AccessTokenNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AccessTokenDtoService implements DtoServiceInterface<AccessToken, AccessTokenEntity> {

    private final AccessTokenRepository accessTokenRepository;

    public AccessTokenDtoService(AccessTokenRepository accessTokenRepository) {
        this.accessTokenRepository = accessTokenRepository;
    }

    @Override
    public AccessToken createDto(AccessTokenEntity entity) {
        return AccessToken
                .builder()
                .token(entity.getValue())
                .userId(entity.getUserId())
                .validUntil(entity.getValidUntil())
                .build();
    }

    @Override
    public AccessTokenEntity findEntity(AccessToken dto) {
        AccessTokenEntity accessTokenEntity = accessTokenRepository.findByValue(dto.getToken());
        if (null == accessTokenEntity)
            throw new AccessTokenNotFoundException("AccessTokenEntity does not exist for AccessToken: "+ dto);

        return accessTokenEntity;
    }
}
