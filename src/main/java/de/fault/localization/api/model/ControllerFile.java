package de.fault.localization.api.model;

import lombok.Data;

import java.io.File;

/**
 * stores java file on local disk where endpoint is defined
 */
@Data
public class ControllerFile {
    private final Class<?> cls;
    private final File file;
    private final File baseDir;

    /**
     * @return relative path used for building the final link to the java file on vcs server
     */
    public String getRelativePath() {
        return this.baseDir.toURI().relativize(this.file.toURI()).getPath();
    }
}
