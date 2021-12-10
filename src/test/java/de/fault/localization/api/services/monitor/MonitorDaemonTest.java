package de.fault.localization.api.services.monitor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import de.fault.localization.api.model.constants.VCS;
import de.fault.localization.api.services.monitor.yaml.RepositoryProperties.Repo;

public class MonitorDaemonTest {

	private Repo getRepo() {
		return new Repo(null, "user", "repo", "branch", "repoId", "baeUrl", 1000L);
	}

	@Nested
	class StartMonitoring {

		@Test
		@DisplayName("GitHubMonitor should be used for GitHub repo")
		void gitHubMonitor() {
			var uut = new MockMonitorDaemonWithoutTimer(null);
			var repo = getRepo();
			repo.setType(VCS.GITHUB);

			uut.startMonitoring(repo);

			var expectedMonitorClass = GitHubMonitor.class;
			var actualMonitorClass = uut.monitor.getClass();
			assertEquals(expectedMonitorClass, actualMonitorClass);
		}

		@Test
		@DisplayName("GitLabMonitor should be used for GitLab repo")
		void gitLabMonitor() {
			var uut = new MockMonitorDaemonWithoutTimer(null);
			var repo = getRepo();
			repo.setType(VCS.GITLAB);

			uut.startMonitoring(repo);

			var expectedMonitorClass = GitLabMonitor.class;
			var actualMonitorClass = uut.monitor.getClass();
			assertEquals(expectedMonitorClass, actualMonitorClass);
		}
	}
}
