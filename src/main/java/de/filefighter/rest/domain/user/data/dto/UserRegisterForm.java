package de.filefighter.rest.domain.user.data.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Arrays;

@Data
@Builder
public class UserRegisterForm {
    private String username;
    private String password;
    private String confirmationPassword;
    private long[] groupIds;

    @Override
    public String toString() {
        return "UserRegisterForm{" +
                "username='" + username + '\'' +
                ", password='" + "****" + '\'' +
                ", confirmationPassword='" + "****" + '\'' +
                ", groupIds=" + Arrays.toString(groupIds) +
                '}';
    }
}
