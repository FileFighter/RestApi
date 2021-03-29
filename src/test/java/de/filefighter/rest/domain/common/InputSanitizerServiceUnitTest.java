package de.filefighter.rest.domain.common;

import de.filefighter.rest.domain.common.exceptions.RequestDidntMeetFormalRequirementsException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InputSanitizerServiceUnitTest {

    private final InputSanitizerService inputSanitizerService = new InputSanitizerService();

    @Test
    void StringIsValid() {
        String string0 = "";
        String string1 = null;

        assertFalse(InputSanitizerService.stringIsValid(string0));
        assertFalse(InputSanitizerService.stringIsValid(string1));
    }

    @Test
    void sanitizeStringThrows() {
        String string0 = "";
        String string1 = null;

        RequestDidntMeetFormalRequirementsException ex = assertThrows(RequestDidntMeetFormalRequirementsException.class, () ->
                InputSanitizerService.sanitizeString(string0));
        assertEquals(RequestDidntMeetFormalRequirementsException.getErrorMessagePrefix()+" String was empty.", ex.getMessage());

        ex = assertThrows(RequestDidntMeetFormalRequirementsException.class, () ->
                InputSanitizerService.sanitizeString(string1));
        assertEquals(RequestDidntMeetFormalRequirementsException.getErrorMessagePrefix()+" String was empty.", ex.getMessage());
    }

    @Test
    void sanitizeStringWorks() {
        String string0 = "a a a b   b    bbb  bb";
        String string1 = "\n\rasd\n";
        String string0valid = "aaabbbbbbb";
        String string1valid = "asd";

        assertEquals(string0valid, InputSanitizerService.sanitizeString(string0));
        assertEquals(string1valid, InputSanitizerService.sanitizeString(string1));
    }

    @Test
    void sanitizeRequestHeaderThrows() {
        String header = "HEADER: ";
        String string0 = "";
        String string1 = null;
        String string2 = header + "";
        String string3 = header + " as a a s d d  d ";

        RequestDidntMeetFormalRequirementsException ex = assertThrows(RequestDidntMeetFormalRequirementsException.class, () ->
                inputSanitizerService.sanitizeRequestHeader(header, string0));
        assertEquals(RequestDidntMeetFormalRequirementsException.getErrorMessagePrefix()+" Header does not contain a valid String.", ex.getMessage());

        ex = assertThrows(RequestDidntMeetFormalRequirementsException.class, () ->
                inputSanitizerService.sanitizeRequestHeader(header, string1));
        assertEquals(RequestDidntMeetFormalRequirementsException.getErrorMessagePrefix()+" Header does not contain a valid String.", ex.getMessage());

        ex = assertThrows(RequestDidntMeetFormalRequirementsException.class, () ->
                inputSanitizerService.sanitizeRequestHeader(header, string2));
        assertEquals(RequestDidntMeetFormalRequirementsException.getErrorMessagePrefix()+" Header does not contain '" + header + "', or format is invalid.", ex.getMessage());

        ex = assertThrows(RequestDidntMeetFormalRequirementsException.class, () ->
                inputSanitizerService.sanitizeRequestHeader(header, string3));
        assertEquals(RequestDidntMeetFormalRequirementsException.getErrorMessagePrefix()+" Header does not contain '" + header + "', or format is invalid.", ex.getMessage());
    }


    @Test
    void sanitizeRequestHeaderWorks() {
        String header = "HEADER: ";
        String expected = "Baum";
        String authString = header + expected;


        String actual = inputSanitizerService.sanitizeRequestHeader(header, authString);
        assertEquals(expected, actual);
    }

    @Test
    void sanitizeTokenThrows() {
        String string0 = "";
        String string1 = null;

        RequestDidntMeetFormalRequirementsException ex = assertThrows(RequestDidntMeetFormalRequirementsException.class, () ->
                inputSanitizerService.sanitizeTokenValue(string0));
        assertEquals(RequestDidntMeetFormalRequirementsException.getErrorMessagePrefix()+" String was empty.", ex.getMessage());

        ex = assertThrows(RequestDidntMeetFormalRequirementsException.class, () ->
                inputSanitizerService.sanitizeTokenValue(string1));
        assertEquals(RequestDidntMeetFormalRequirementsException.getErrorMessagePrefix()+" String was empty.", ex.getMessage());

    }

    @Test
    void sanitizeTokenWorks() {
        String string0 = "a a a b   b    bbb  bb";
        String string1 = "\n\rasd\n";
        String string0valid = "aaabbbbbbb";
        String string1valid = "asd";

        assertEquals(string0valid, inputSanitizerService.sanitizeTokenValue(string0));
        assertEquals(string1valid, inputSanitizerService.sanitizeTokenValue(string1));
    }

}