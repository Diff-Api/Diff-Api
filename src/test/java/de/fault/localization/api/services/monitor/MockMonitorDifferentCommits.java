package de.fault.localization.api.services.monitor;

import java.io.IOException;
import java.util.UUID;

import de.fault.localization.api.model.constants.VCS;

public class MockMonitorDifferentCommits extends RepositoryMonitor {

	MockMonitorDifferentCommits(String user, String repo, String branch, VCS vcs, String apiUrl) {
		super(user, repo, branch, vcs, apiUrl);
	}

	@Override
	String getHashOfLastCommit(String url) throws IOException {
		return UUID.randomUUID().toString();
	}
}
