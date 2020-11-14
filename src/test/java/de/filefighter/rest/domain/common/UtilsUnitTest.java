package de.filefighter.rest.domain.common;

import de.filefighter.rest.rest.exceptions.RequestDidntMeetFormalRequirementsException;
import org.junit.jupiter.api.Test;

import static de.filefighter.rest.configuration.RestConfiguration.AUTHORIZATION_BASIC_PREFIX;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings(value = "ConstantConditions")
class UtilsUnitTest {

    @Test
    void stringIsValid() {
        String string = "string";
        boolean actual = Utils.stringIsValid(string);
        assertTrue(actual);
    }

    @Test
    void stringIsNull() {
        String string = null;
        boolean actual = Utils.stringIsValid(string);
        assertFalse(actual);
    }

    @Test
    void stringIsEmpty() {
        String string = "";
        boolean actual = Utils.stringIsValid(string);
        assertFalse(actual);
    }

    @Test
    void stringIsBlank() {
        String string = "";
        boolean actual = Utils.stringIsValid(string);
        assertFalse(actual);
    }

    @Test
    void validateHeaderThrows() {
        String dummyHeaderPrefix = "UGABUGA: ";

        String notValid = "";
        String validButDoesntMatch = "something";
        String noContent = dummyHeaderPrefix + "";

        assertThrows(RequestDidntMeetFormalRequirementsException.class, () ->
                Utils.validateAuthorizationHeader(dummyHeaderPrefix, notValid)
        );
        assertThrows(RequestDidntMeetFormalRequirementsException.class, () ->
                Utils.validateAuthorizationHeader(dummyHeaderPrefix, validButDoesntMatch)
        );
        assertThrows(RequestDidntMeetFormalRequirementsException.class, () ->
                Utils.validateAuthorizationHeader(dummyHeaderPrefix, noContent)
        );
    }

    @Test
    void validateHeaderWorks() {
        String dummyHeaderPrefix = "UGABUGA: ";
        String expected = "baum";
        String valid = dummyHeaderPrefix.concat(expected);

        String actual = Utils.validateAuthorizationHeader(dummyHeaderPrefix, valid);

        assertEquals(expected, actual);
    }
}