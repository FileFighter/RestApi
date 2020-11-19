package de.filefighter.rest.domain.common;

import de.filefighter.rest.rest.exceptions.RequestDidntMeetFormalRequirementsException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Utils {

    private Utils() {
        // Prevent Instantiation
    }

    public static boolean stringIsValid(String s) {
        return !(null == s || s.isEmpty() || s.isBlank());
    }

    public static String validateAuthorizationHeader(String header, String testString) {
        if (!stringIsValid(testString))
            throw new RequestDidntMeetFormalRequirementsException("Header does not contain a valid String.");

        if (!testString.matches("^" + header + "[^\\s](.*)$"))
            throw new RequestDidntMeetFormalRequirementsException("Header does not contain '" + header + "', or format is invalid.");
        String[] split = testString.split(header);
        return split[1];
    }

    public static List<String> getLinesFromFile(File file) {
        ArrayList<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            while (br.ready()) {
                lines.add(br.readLine().replace(" ",""));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }
}
