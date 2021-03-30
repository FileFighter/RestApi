package de.filefighter.rest.domain.common;

import de.filefighter.rest.domain.common.exceptions.RequestDidntMeetFormalRequirementsException;
import de.filefighter.rest.domain.filesystem.data.dto.FileSystemUpload;
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
                inputSanitizerService.sanitizeString(string0));
        assertEquals(RequestDidntMeetFormalRequirementsException.getErrorMessagePrefix() + " String was empty.", ex.getMessage());

        ex = assertThrows(RequestDidntMeetFormalRequirementsException.class, () ->
                inputSanitizerService.sanitizeString(string1));
        assertEquals(RequestDidntMeetFormalRequirementsException.getErrorMessagePrefix() + " String was empty.", ex.getMessage());
    }

    @Test
    void sanitizeStringWorks() {
        String string0 = "a a a b   b    bbb  bb";
        String string1 = "\n\rasd\n";
        String string0valid = "aaabbbbbbb";
        String string1valid = "asd";

        assertEquals(string0valid, inputSanitizerService.sanitizeString(string0));
        assertEquals(string1valid, inputSanitizerService.sanitizeString(string1));
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
        assertEquals(RequestDidntMeetFormalRequirementsException.getErrorMessagePrefix() + " Header does not contain a valid String.", ex.getMessage());

        ex = assertThrows(RequestDidntMeetFormalRequirementsException.class, () ->
                inputSanitizerService.sanitizeRequestHeader(header, string1));
        assertEquals(RequestDidntMeetFormalRequirementsException.getErrorMessagePrefix() + " Header does not contain a valid String.", ex.getMessage());

        ex = assertThrows(RequestDidntMeetFormalRequirementsException.class, () ->
                inputSanitizerService.sanitizeRequestHeader(header, string2));
        assertEquals(RequestDidntMeetFormalRequirementsException.getErrorMessagePrefix() + " Header does not contain '" + header + "', or format is invalid.", ex.getMessage());

        ex = assertThrows(RequestDidntMeetFormalRequirementsException.class, () ->
                inputSanitizerService.sanitizeRequestHeader(header, string3));
        assertEquals(RequestDidntMeetFormalRequirementsException.getErrorMessagePrefix() + " Header does not contain '" + header + "', or format is invalid.", ex.getMessage());
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
        assertEquals(RequestDidntMeetFormalRequirementsException.getErrorMessagePrefix() + " String was empty.", ex.getMessage());

        ex = assertThrows(RequestDidntMeetFormalRequirementsException.class, () ->
                inputSanitizerService.sanitizeTokenValue(string1));
        assertEquals(RequestDidntMeetFormalRequirementsException.getErrorMessagePrefix() + " String was empty.", ex.getMessage());

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

    @Test
    void sanitizePathThrows() {
        String working0 = "/ foo / bar /      ";
        String working1 = "foo/bar/foobar";
        String working2 = "foo/bar/foo-bar.txt";
        String working3 = "foo/bar/foo_bar_BAUM_ASDASD.txt";
        String nonWorking0 = "//foo/bar";
        String nonWorking1 = "\\/foo/bar";
        String nonWorking2 = "/~foo/bar";
        String nonWorking3 = "/*()foo/bar";
        String nonWorking4 = "/?foo/bar";

        assertEquals("/foo/bar/", inputSanitizerService.sanitizePath(working0));
        assertEquals("foo/bar/foobar", inputSanitizerService.sanitizePath(working1));
        assertEquals("foo/bar/foo-bar.txt", inputSanitizerService.sanitizePath(working2));
        assertEquals("foo/bar/foo_bar_BAUM_ASDASD.txt", inputSanitizerService.sanitizePath(working3));

        RequestDidntMeetFormalRequirementsException ex = assertThrows(RequestDidntMeetFormalRequirementsException.class, () ->
                inputSanitizerService.sanitizePath(nonWorking0));
        assertEquals(RequestDidntMeetFormalRequirementsException.getErrorMessagePrefix() + " Path was not valid.", ex.getMessage());

        ex = assertThrows(RequestDidntMeetFormalRequirementsException.class, () ->
                inputSanitizerService.sanitizePath(nonWorking1));
        assertEquals(RequestDidntMeetFormalRequirementsException.getErrorMessagePrefix() + " Path was not valid.", ex.getMessage());

        ex = assertThrows(RequestDidntMeetFormalRequirementsException.class, () ->
                inputSanitizerService.sanitizePath(nonWorking2));
        assertEquals(RequestDidntMeetFormalRequirementsException.getErrorMessagePrefix() + " Path was not valid.", ex.getMessage());

        ex = assertThrows(RequestDidntMeetFormalRequirementsException.class, () ->
                inputSanitizerService.sanitizePath(nonWorking3));
        assertEquals(RequestDidntMeetFormalRequirementsException.getErrorMessagePrefix() + " Path was not valid.", ex.getMessage());

        ex = assertThrows(RequestDidntMeetFormalRequirementsException.class, () ->
                inputSanitizerService.sanitizePath(nonWorking4));
        assertEquals(RequestDidntMeetFormalRequirementsException.getErrorMessagePrefix() + " Path was not valid.", ex.getMessage());
    }

    @Test
    void sanitizeUploadWorks() {
        String workingPath = "/ foo / bar /      ";
        String workingName = "baum.txt";

        FileSystemUpload upload = FileSystemUpload.builder().build();

        RequestDidntMeetFormalRequirementsException ex = assertThrows(RequestDidntMeetFormalRequirementsException.class, () ->
                inputSanitizerService.sanitizeUpload(upload));
        assertEquals(RequestDidntMeetFormalRequirementsException.getErrorMessagePrefix() + " String was empty.", ex.getMessage());

        FileSystemUpload workingUpload = FileSystemUpload.builder().name(workingName).path(workingPath).build();
        assertDoesNotThrow(() -> inputSanitizerService.sanitizeUpload(workingUpload));
    }
}