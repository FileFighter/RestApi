package de.filefighter.rest.domain.token.business;

import org.springframework.stereotype.Service;

@Service
public class AccessTokenBusinessService {
    public static final long ACCESS_TOKEN_DURATION_IN_SECONDS = 3600L;
}