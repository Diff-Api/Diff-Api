package de.fault.localization.api.utilities;

import de.fault.localization.api.exceptions.CompileException;

import java.io.File;
import java.io.IOException;

/**
 * contains methods for compiling projects
 */
public class ProjectCompiler {

    private ProjectCompiler(){

    }

    private static final String MAVEN_COMPILE_ERROR = "BUILD FAILURE";

    /**
     * @param dir - compile directory
     * @throws CompileException - io related error occurs (no permissions, maven not in build path) or compile finished with error
     */
    public static String compile(final File dir) throws IOException {

        try {
            // do compile with maven but no tests
            final String output = RuntimeUtil.getOutput("mvn -parameter -Dmaven.test.skip=true -f " + dir.getAbsolutePath() + " compile");

            // do not consider exit values, since these may indicate issues not related to compiling
            if (output.contains(MAVEN_COMPILE_ERROR)) {
                throw new CompileException(output);
            }
            return output;
        } catch (final Exception e) {
            throw new CompileException(e);
        }

    }

}
