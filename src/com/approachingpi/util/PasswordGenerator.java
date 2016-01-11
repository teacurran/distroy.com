package com.approachingpi.util;

/**
 * Approaching Pi, Inc.
 * User: Tea Curran
 * Date: Mar 29, 2005
 * Time: 1:17:33 AM
 * Desc:
 */
public class PasswordGenerator {
    private static final char[] LOWER_ALPHA_CHARS = ("abcdefghijklmnopqrstuvwxyz").toCharArray();
    private static final char[] UPPER_ALPHA_CHARS = ("ABCDEFGHIJKLMNOPQRSTUVWXYZ").toCharArray();
    private static final char[] NUMERIC_CHARS = ("0123456789").toCharArray();

    public static String generate(String pattern) {
        if (pattern == null || pattern.length() == 0) {
            return "";
        }
        char[] patternChars = pattern.toCharArray();
        StringBuffer retVal = new StringBuffer(pattern.length());
        java.util.Random rndGen = new java.util.Random();
        for (int i=0; i<patternChars.length; i++) {
            if (patternChars[i] == '#') {
                retVal.append(NUMERIC_CHARS[Math.abs(rndGen.nextInt()) % NUMERIC_CHARS.length]);
            } else if (patternChars[i] == 'L') {
                retVal.append(LOWER_ALPHA_CHARS[Math.abs(rndGen.nextInt()) % NUMERIC_CHARS.length]);
            } else if (patternChars[i] == 'U') {
                retVal.append(UPPER_ALPHA_CHARS[Math.abs(rndGen.nextInt()) % NUMERIC_CHARS.length]);
            }
        }
        return retVal.toString();
    }
}
