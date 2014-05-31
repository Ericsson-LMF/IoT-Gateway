package com.ericsson.deviceaccess.serviceschema.codegenerator;

/**
 *
 * @author delma
 */
public enum StringHelper {

    /**
     * Singleton
     */
    INSTANCE;
    
    /**
     * Makes first char of the string to upper case
     * @param string String to be capitalized
     * @return capitalized string
     */
    public static String capitalize(String string) {
        StringBuilder sb = new StringBuilder(string);
        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        return sb.toString();
    }

    /**
     * Ensures that there is punctuation at end of string
     * @param string string to ensure punctuation
     * @return ensured string
     */
    public static String setEndPunctuation(String string) {
        if (string.endsWith(".")) {
            return string;
        }
        return string + ".";
    }

    /**
     * gets type from string
     * @param string string to get type from
     * @return type as string
     */
    public static String getType(String string) {
        if (string.toLowerCase().startsWith("int")) {
            return "int";
        } else if (string.toLowerCase().startsWith("float")) {
            return "float";
        } else {
            return "String";
        }
    }
}
