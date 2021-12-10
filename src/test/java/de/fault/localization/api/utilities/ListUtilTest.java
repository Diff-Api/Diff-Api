package de.fault.localization.api.utilities;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ListUtilTest {

    @Test
    void test() {
        val ls1 = new ArrayList<String>();
        ls1.add("a");
        ls1.add("b");
        val ls2 = new ArrayList<String>();
        ls2.add("b");
        ls2.add("a");
        assertTrue(ListUtil.isUnorderedEqual(ls1, ls2));
    }
}