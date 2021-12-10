package de.fault.localization.api.services.monitor;

import com.google.gson.Gson;
import de.fault.localization.api.model.constants.VCS;
import de.fault.localization.api.services.monitor.json.GithubApiResultBranch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class GitHubMonitor extends RepositoryMonitor {

	/** argument order: user, repository, branch */
	private static final String API_URL = "https://api.github.com/repos/%s/%s/branches/%s";

	/**
	 * Monitor used to observe a repository on GitHub
	 * 
	 * @param user   User name of the repository user
	 * @param repo   Repository name which is to be monitored
	 * @param branch Repository branch which is to be monitored
	 */
	public GitHubMonitor(final String user, final String repo, final String branch) {
		super(user, repo, branch, VCS.GITHUB, API_URL);
	}

	/**
	 * @see RepositoryMonitor#getHashOfLastCommit(String)
	 */
	@Override
	String getHashOfLastCommit(final String url) throws IOException {
		final InputStream is = new URL(url).openStream();
		final GithubApiResultBranch branch;
		try (final BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
			branch = new Gson().fromJson(rd, GithubApiResultBranch.class);
		}

		if (branch == null) {
			throw new IllegalArgumentException("URL cannot be parsed!");
		}

		this.webUrl = branch.links.html;

		return branch.commit.sha;
	}
}
