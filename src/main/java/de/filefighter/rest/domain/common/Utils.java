package de.filefighter.rest.domain.common;

import de.filefighter.rest.rest.exceptions.RequestDidntMeetFormalRequirementsException;

public class Utils {

    public static boolean stringIsValid(String s){
        return !(null == s || s.isEmpty() || s.isBlank());
    }

    public static String validateAuthorizationHeader(String header, String testString){
        if(!stringIsValid(testString))
            throw new RequestDidntMeetFormalRequirementsException("Header does not contain a valid String.");

        if (!testString.matches("^" + header + "[^\\s](.*)$"))
            throw new RequestDidntMeetFormalRequirementsException("Header does not contain '" + header + "', or format is invalid.");
        String[] split = testString.split(header);
        return split[1];
    }
}
