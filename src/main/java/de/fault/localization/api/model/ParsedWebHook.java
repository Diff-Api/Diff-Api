package de.fault.localization.api.model;

import de.fault.localization.api.model.constants.VCS;
import lombok.Data;

/**
 * information that is extracted from the json webhook
 */
@Data
public class ParsedWebHook {
    public static final String INIT_COMMIT = "0000000000000000000000000000000000000000";
    /**
     * for cloning the project (no auth)
     */
    private final String absoluteUrl, branch;
    private final VCS vcs;

    private final String commitIdNew, commitIdOld;

    public boolean isInitial() {
        return this.commitIdOld.equals(INIT_COMMIT);
    }

}
