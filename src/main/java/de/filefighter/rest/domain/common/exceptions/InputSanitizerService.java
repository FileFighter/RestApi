package de.filefighter.rest.domain.common.exceptions;

import org.springframework.stereotype.Service;

@Service
public class InputSanitizerService {

    public static boolean stringIsValid(String s) {
        return !(null == s || s.isEmpty() || s.isBlank());
    }

    /**
     *
     * Sanitizes a String, so it can be used.
     * @param string String that needs to be sanitized.
     * @return string without whitespaces and without illegal characters.
     * @throws RequestDidntMeetFormalRequirementsException when string was empty.
     */
    public static String sanitizeString(String string) {
        if(!InputSanitizerService.stringIsValid(string))
            throw new RequestDidntMeetFormalRequirementsException("String was empty.");
        return string.replaceAll("\\s", "");
    }

    public String sanitizeRequestHeader(String header, String testString) {
        if (!(stringIsValid(testString) && stringIsValid(header)))
            throw new RequestDidntMeetFormalRequirementsException("Header does not contain a valid String.");

        if (!testString.matches("^" + header + "[^\\s](.*)$"))
            throw new RequestDidntMeetFormalRequirementsException("Header does not contain '" + header + "', or format is invalid.");
        String[] split = testString.split(header);
        return split[1];
    }

    public String sanitizeTokenValue(String tokenValue){
        return InputSanitizerService.sanitizeString(tokenValue);
    }
}
