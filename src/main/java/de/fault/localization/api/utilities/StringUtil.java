package de.fault.localization.api.utilities;

/**
 * contains methods for dealing with strings
 */
public class StringUtil {

    private StringUtil(){

    }

    /**
     * @param prefix - the prefix to prepend
     * @param text   - the text
     * @return {@code prefix} + {@code text} or only {@code text} if it starts with {@code prefix}
     */
    public static String appendStartOptional(final String prefix, final String text) {
        if (!text.startsWith(prefix)) {
            return prefix + text;
        }
        return text;
    }

    /**
     * concatenates {@code first} and {@code second} together, and makes sure that they are separated by {@code join}
     *
     * @param first  - the first
     * @param join   - the text between
     * @param second - the second
     * @return first + join + second if {@code first} does not end with {@code join} and {@code second} does not start with {@code join}, else {@code first}+{@code second}
     */
    public static String joinChar(String first, final String join, String second) {
        if (first.endsWith(join)) {
            first = first.substring(0, first.length() - join.length());
        }
        if (second.startsWith(join)) {
            second = second.substring(join.length());
        }
        return first + join + second;

    }
}
