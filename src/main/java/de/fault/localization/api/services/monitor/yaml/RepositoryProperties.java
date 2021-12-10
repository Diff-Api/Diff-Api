package de.fault.localization.api.services.monitor.yaml;

import de.fault.localization.api.model.constants.VCS;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * This class is used to read the repositories from the configuration files
 */
@Data
public class RepositoryProperties {
	private List<Repo> repositories;

	private String ccimsApi;

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Repo {
		private VCS type;
		private String user;
		private String repo;
		private String branch;
		private String repoId;
		private String baseUrl;
		private Long monitoringInterval;
	}
}
