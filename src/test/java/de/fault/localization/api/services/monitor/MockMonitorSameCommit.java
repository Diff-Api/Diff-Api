package de.fault.localization.api.services.monitor;

import java.io.IOException;

import de.fault.localization.api.model.constants.VCS;

public class MockMonitorSameCommit extends RepositoryMonitor {

	MockMonitorSameCommit(String user, String repo, String branch, VCS vcs, String apiUrl) {
		super(user, repo, branch, vcs, apiUrl);
	}

	@Override
	String getHashOfLastCommit(String url) throws IOException {
		return "f90a5005-f9cb-4510-a1c9-b0a48cc27d73";
	}
}
