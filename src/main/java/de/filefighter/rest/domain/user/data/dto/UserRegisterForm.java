package de.filefighter.rest.domain.user.data.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserRegisterForm {
    private String username;
    private String password;
    private long[] roleIds;
}
