package de.filefighter.rest.domain.common;

import de.filefighter.rest.domain.common.exceptions.RequestDidntMeetFormalRequirementsException;
import de.filefighter.rest.domain.filesystem.data.dto.upload.FileSystemUpload;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class InputSanitizerService {

    public static boolean stringIsValid(String s) {
        return !(null == s || s.isEmpty() || s.isBlank());
    }

    /**
     * Sanitizes a String, so it can be used.
     *
     * @param string String that needs to be sanitized.
     * @return string without whitespaces and without illegal characters.
     * @throws RequestDidntMeetFormalRequirementsException when string was empty.
     */
    public String sanitizeString(String string) {
        if (!InputSanitizerService.stringIsValid(string))
            throw new RequestDidntMeetFormalRequirementsException("String was empty.");
        return string.replaceAll("\\s", "");
    }

    public String sanitizePath(String path) {
        if (!pathIsValid(path))
            throw new RequestDidntMeetFormalRequirementsException("Path was not valid.");

        return sanitizeString(path);
    }

    // TODO assure that the path and name are valid
    public FileSystemUpload sanitizeUpload(FileSystemUpload fileSystemUpload) {
        fileSystemUpload.setPath(sanitizePath(fileSystemUpload.getPath()));
        fileSystemUpload.setName(sanitizeString(fileSystemUpload.getName()));
        return fileSystemUpload;
    }

    public String sanitizeRequestHeader(String header, String testString) {
        if (!(stringIsValid(testString) && stringIsValid(header)))
            throw new RequestDidntMeetFormalRequirementsException("Header does not contain a valid String.");

        if (!testString.matches("^" + header + "[^\\s](.*)$"))
            throw new RequestDidntMeetFormalRequirementsException("Header does not contain '" + header + "', or format is invalid.");
        String[] split = testString.split(header);
        return split[1];
    }

    public boolean pathIsValid(String path) {
        String validString = sanitizeString(path);

        Pattern pattern = Pattern.compile("[~#@*+:!?&%<>|\"^\\\\]");
        Matcher matcher = pattern.matcher(validString);

        boolean stringContainsDoubleSlash = validString.contains("//");

        return !(matcher.find() || stringContainsDoubleSlash);
    }

    public String sanitizeTokenValue(String tokenValue) {
        return this.sanitizeString(tokenValue);
    }
}
