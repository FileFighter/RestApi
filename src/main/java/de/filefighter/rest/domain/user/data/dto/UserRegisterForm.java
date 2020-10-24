package de.filefighter.rest.domain.user.data.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(buildMethodName = "create", builderClassName = "UserRegistrationFormBuilder")
public class UserRegisterForm {
    private String username;
    private String password;
    private long[] roleIds;
}
