package de.fault.localization.api.services.monitor;

import com.google.gson.Gson;
import de.fault.localization.api.model.constants.VCS;
import de.fault.localization.api.services.monitor.json.GitlabApiResultBranch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class GitLabMonitor extends RepositoryMonitor {

	/** argument order: URL, id (repositories are numbered), branch */
	private static final String API_URL = "%s/api/v4/projects/%s/repository/branches/%s";

	/**
	 * Monitor used to observe a repository on GitLab
	 * 
	 * @param url    URL to the GitLab instance
	 * @param id     Repository id which is to be monitored
	 * @param branch Repository branch which is to be monitored
	 */
	public GitLabMonitor(final String url, final String id, final String branch) {
		super(url, id, branch, VCS.GITLAB, API_URL);
	}

	/**
	 * @see RepositoryMonitor#getHashOfLastCommit(String)
	 */
	@Override
	String getHashOfLastCommit(final String url) throws IOException {
		final InputStream is = new URL(url).openStream();
		final GitlabApiResultBranch branch;
		try (final BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
			branch = new Gson().fromJson(rd, GitlabApiResultBranch.class);
		}

		if (branch == null) {
			throw new IllegalArgumentException("URL cannot be parsed!");
		}

		this.webUrl = branch.web_url;

		final String[] splitUrl = this.webUrl.split("gitlab.local");
		if (splitUrl.length == 2) {
			this.webUrl = this.user + splitUrl[1];
		}

		return branch.commit.id;
	}
}
