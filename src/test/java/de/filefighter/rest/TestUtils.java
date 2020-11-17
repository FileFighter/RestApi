package de.filefighter.rest;

import java.util.Arrays;

public class TestUtils {

    public static String serializeUserRequest(String confirmationPassword, int[] groupIds, String password, String username) {
        StringBuilder jsonString = new StringBuilder("{");

        if (confirmationPassword != null) {
            jsonString.append("\"confirmationPassword\": \"").append(confirmationPassword).append("\",");
        }
        if (groupIds != null && groupIds.length > 0) {
            jsonString.append("\"groupIds\": ").append(Arrays.toString(groupIds)).append(",");
        }
        if (password != null) {
            jsonString.append("\"password\": \"").append(password).append(username != null?"\",":"");
        }
        if (username != null) {
            jsonString.append("\"username\": \"").append(username).append("\"");
        }

        jsonString.append("}");

        return jsonString.toString();
    }
}
