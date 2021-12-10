package de.fault.localization.api.services.monitor.json;

/**
 * This class is used to convert the results from the GitLab API from JSON into
 * an object.
 */
public class GitlabApiResultBranch {
	public String name;
	public Commit commit;
	public String web_url;

	public class Commit {
		public String short_id;
		public String id;
	}
}
