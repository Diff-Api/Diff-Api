package de.fault.localization.api.services.monitor.json;

import com.google.gson.annotations.SerializedName;

/**
 * This class is used to convert the results from the GitHub API from JSON into
 * an object.
 */
public class GithubApiResultBranch {
	public String name;
	public Commit commit;
	@SerializedName(value = "_links")
	public Links links;

	public class Commit {
		public String url;
		public String sha;
	}

	public class Links {
		public String html;
	}
}
