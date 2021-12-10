package de.fault.localization.api.utilities;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileUtilTest {

    @Test
    void test() throws IOException {
        final File file = new File("/tmp/out");
        assertEquals(FileUtil.relativePath(new File("/tmp"), file), "out");
        FileUtil.write(file, "abc\ndef\n123\n456");
        assertEquals(FileUtil.getLineNumber(file, "123"), 3);
    }
}