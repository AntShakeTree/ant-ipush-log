package com.ant.ipush.asyn;

//import org.apache.logging.log4j.util.Strings;

public final class Integers {

    private static final int BITS_PER_INT = 32;

    private Integers() {
    }

    /**
     * Parses the string argument as a signed decimal integer.
     *
     * @param s            a {@code String} containing the {@code int} representation to parse, may be {@code null} or {@code ""}
     * @param defaultValue the return value, use {@code defaultValue} if {@code s} is {@code null} or {@code ""}
     * @return the integer value represented by the argument in decimal.
     * @throws NumberFormatException if the string does not contain a parsable integer.
     */
    public static int parseInt(final String s, final int defaultValue) {


        return s == null || "".equals(s) ? defaultValue : Integer.parseInt(s);
    }

    /**
     * Parses the string argument as a signed decimal integer.
     *
     * @param s a {@code String} containing the {@code int} representation to parse, may be {@code null} or {@code ""}
     * @return the integer value represented by the argument in decimal.
     * @throws NumberFormatException if the string does not contain a parsable integer.
     */
    public static int parseInt(final String s) {
        return parseInt(s, 0);
    }

    /**
     * Calculate the next power of 2, greater than or equal to x.
     * <p>
     * From Hacker's Delight, Chapter 3, Harry S. Warren Jr.
     *
     * @param x Value to round up
     * @return The next power of 2 from x inclusive
     */
    public static int ceilingNextPowerOfTwo(final int x) {
        return 1 << (BITS_PER_INT - Integer.numberOfLeadingZeros(x - 1));
    }
}