package de.filefighter.rest.domain.common;

public class Utils {

    public static boolean stringIsValid(String s){
        return !(null == s || s.isEmpty() || s.isBlank());
    }
}
