package com.ericsson.deviceaccess.serviceschema.codegenerator;

/**
 *
 * @author delma
 */
public enum StringHelper {

    INSTANCE;

    public static String capitalize(String string) {
        StringBuilder sb = new StringBuilder(string);
        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        return sb.toString();
    }

    public static String setEndPunctuation(String description) {
        if (description.endsWith(".")) {
            return description;
        }
        return description + ".";
    }

    public static String getType(String type) {
        if (type.toLowerCase().startsWith("int")) {
            return "int";
        } else if (type.toLowerCase().startsWith("float")) {
            return "float";
        } else {
            return "String";
        }
    }
}
