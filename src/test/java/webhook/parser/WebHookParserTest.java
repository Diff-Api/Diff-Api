package webhook.parser;

import de.fault.localization.api.utilities.WebHookParser;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WebHookParserTest {

    @Test
    void testGitlab() throws Exception {
        final URL resource = WebHookParserTest.class.getResource("gitlab.json");
        final String content = FileUtils.readFileToString(new File(resource.toURI()), StandardCharsets.UTF_8);
        val jsonHook = WebHookParser.parseFrom(content);
        assertEquals(jsonHook.getAbsoluteUrl(), "https://gitlab.com/te21/test");
    }

    @Test
    void testGithub() throws Exception {
        final URL resource = WebHookParserTest.class.getResource("github.json");
        final String content = FileUtils.readFileToString(new File(resource.toURI()), StandardCharsets.UTF_8);
        val jsonHook = WebHookParser.parseFrom(content);
        assertEquals(jsonHook.getAbsoluteUrl(), "https://github.com/DSimsek000/the-english-hans");
    }

}
