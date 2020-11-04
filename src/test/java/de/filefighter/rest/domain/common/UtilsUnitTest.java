package de.filefighter.rest.domain.common;

import org.junit.jupiter.api.Test;

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
}