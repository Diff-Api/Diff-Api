package de.fault.localization.api.services.monitor;

import de.fault.localization.api.model.constants.VCS;
import lombok.extern.java.Log;

import java.io.IOException;

@Log
public abstract class RepositoryMonitor {

	final String user, repo, branch, apiUrl;
	final VCS vcs;
	String recentCommit, previousCommit, webUrl;

	/**
	 * Monitor used to observe a repository
	 * 
	 * @param user   User name of the repository user
	 * @param repo   Repository name
	 * @param branch Repository branch
	 * @param vcs    Used Version Control System
	 * @param apiUrl URL used to make API calls, specified by subclasses
	 */
	RepositoryMonitor(final String user, final String repo, final String branch, final VCS vcs, final String apiUrl) {
		this.user = user;
		this.repo = repo;
		this.branch = branch;
		this.vcs = vcs;
		this.apiUrl = apiUrl;
	}

	/**
	 * Calls the {@code url} and checks for the Hash of the last id
	 * 
	 * @param url URL to the commit API of the repository
	 * @return Hash of the last commit
	 * @throws IOException Will be thrown if no connection to the URL can be
	 *                     established
	 */
	abstract String getHashOfLastCommit(String url) throws IOException;

	/**
	 * Calls the API ({\{@link #apiUrl} and checks if a new commit was created
	 * 
	 * @return whether a new commit was created
	 */
	public boolean hasNewCommit() {
		final String url = String.format(this.apiUrl, this.user, this.repo, this.branch);
		final String newCommit;
		try {
			newCommit = this.getHashOfLastCommit(url);
		} catch (final Exception e) {
			log.severe("Error while monitoring repo: " + url + "! " + e.getLocalizedMessage());
			return false;
		}

		if (this.recentCommit == null) {
			this.recentCommit = newCommit;
			return false;
		}

		if (newCommit.equals(this.recentCommit)) {
			return false;
		}

		this.previousCommit = this.recentCommit;
		this.recentCommit = newCommit;
		return true;
	}
}
