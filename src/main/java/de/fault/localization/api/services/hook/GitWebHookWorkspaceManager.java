package de.fault.localization.api.services.hook;

import java.io.File;

/**
 * this class manages builds
 */
class GitWebHookWorkspaceManager {

	private GitWebHookWorkspaceManager(){

	}

	private final static File directory = new File("builds");

	private final static String OLD_BUILD_NAME = "old";

	private final static String NEW_BUILD_NAME = "new";

	static File getBuildDirectory() {
		directory.mkdirs();
		return directory;
	}

	static String getNewBuildName() {
		return NEW_BUILD_NAME;
	}

	static String getOldBuildName() {
		return OLD_BUILD_NAME;
	}

}
