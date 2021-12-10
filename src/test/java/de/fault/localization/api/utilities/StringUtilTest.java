package de.fault.localization.api.utilities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StringUtilTest {

    @Test
    void test() {
        assertEquals(StringUtil.appendStartOptional("abc", "abcdef"), "abcdef");
        assertEquals(StringUtil.appendStartOptional("abc", "def"), "abcdef");

        assertEquals(StringUtil.joinChar("abc", "&", "def"), "abc&def");
        assertEquals(StringUtil.joinChar("abc&", "&", "def"), "abc&def");
        assertEquals(StringUtil.joinChar("abc", "&", "&def"), "abc&def");
    }

}