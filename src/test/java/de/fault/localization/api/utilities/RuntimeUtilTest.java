package de.fault.localization.api.utilities;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RuntimeUtilTest {

    @Test
    void test() throws IOException {
        assertTrue(StringUtils.isNotBlank(RuntimeUtil.getOutput("whoami")));
    }

}