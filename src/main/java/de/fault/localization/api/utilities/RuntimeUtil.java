package de.fault.localization.api.utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * contains methods for executing commands with default shell
 */
public class RuntimeUtil {

    private RuntimeUtil(){

    }

    /**
     * @param cmd - command to execute
     * @return output
     * @throws IOException - if io related error occurred, e.g. the program specified was not found or encoding issues
     */
    public static String getOutput(String cmd) throws IOException {

        // for the unlucky ones
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            cmd = "cmd /c" + cmd;
        }

        final Process proc = Runtime.getRuntime().exec(cmd);

        final BufferedReader outReader = new BufferedReader(new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8));
        final BufferedReader errReader = new BufferedReader(new InputStreamReader(proc.getErrorStream(), StandardCharsets.UTF_8));

        final StringBuilder sb = new StringBuilder();

        String line;

        while ((line = outReader.readLine()) != null) {
            sb.append(line).append("\n");
        }

        while ((line = errReader.readLine()) != null) {
            sb.append(line).append("\n");
        }

        return sb.toString();
    }
}
