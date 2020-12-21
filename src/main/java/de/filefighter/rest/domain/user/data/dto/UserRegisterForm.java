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
    @Builder.Default
    private long[] groupIds = new long[0];

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
