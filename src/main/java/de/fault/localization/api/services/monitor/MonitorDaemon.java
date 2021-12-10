package de.fault.localization.api.services.monitor;

import de.fault.localization.api.exceptions.WebHookHandlerException;
import de.fault.localization.api.model.ParsedWebHook;
import de.fault.localization.api.services.hook.GitWebHookHandler;
import de.fault.localization.api.services.monitor.yaml.RepositoryProperties.Repo;
import de.fault.localization.api.services.settings.Config;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.google.common.annotations.VisibleForTesting;

public class MonitorDaemon {

	private static final long DEFAULT_MONITORING_INTERVAL = 30 * 1000;
	private final GitWebHookHandler gitWebHookHandler;

	/**
	 * Reads the configuration file and automatically monitors all specified
	 * repositories. When a new commit is detected on a repository, the compare
	 * process is started.
	 * 
	 * @param gitWebHookHandler Used to perform the comparison
	 */
	public MonitorDaemon(final GitWebHookHandler gitWebHookHandler) {
		this.gitWebHookHandler = gitWebHookHandler;
		final List<Repo> props = Config.get().getRepositories();
		if (props != null) {
			props.forEach(this::startMonitoring);
		}
	}

	/**
	 * Creates an corresponding monitor for the repository which is started by
	 * {@link #startTimer(RepositoryMonitor, long)} and repeated at the interval
	 * specified in {@code repo}
	 * 
	 * @param repo Repository to be monitored
	 */
	@VisibleForTesting
	void startMonitoring(final Repo repo) {
		final RepositoryMonitor monitor;
		long intervall = DEFAULT_MONITORING_INTERVAL;
		if (repo.getMonitoringInterval() != null) {
			intervall = repo.getMonitoringInterval();
		}

		switch (repo.getType()) {
		case GITHUB:
			monitor = new GitHubMonitor(repo.getUser(), repo.getRepo(), repo.getBranch());
			break;
		case GITLAB:
			monitor = new GitLabMonitor(repo.getBaseUrl(), repo.getRepoId(), repo.getBranch());
			break;
		default:
			throw new RuntimeException("not implemented yet");
		}

		this.startTimer(monitor, intervall);
	}

	/**
	 * Starts a timer that looks via the monitor if there is a new commit, if
	 * necessary the comparison process {@link #startComparison(RepositoryMonitor)}
	 * is triggered. This action is repeated at the specified {@code interval}.
	 * 
	 * @param monitor  Monitor which is used to look for a new commit
	 * @param interval Interval in which is checked for changes (in milliseconds)
	 */
	@VisibleForTesting
	void startTimer(final RepositoryMonitor monitor, final long interval) {
		final Timer uploadCheckerTimer = new Timer();
		uploadCheckerTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				if (monitor.hasNewCommit()) {
					MonitorDaemon.this.startComparison(monitor);
				}
			}
		}, 0, interval);
	}

	/**
	 * Starts the comparison process
	 * 
	 * @param monitor Contains the repository and commit information for the
	 *                comparison
	 */
	public void startComparison(final RepositoryMonitor monitor) {

		final ParsedWebHook repoInfo = new ParsedWebHook(monitor.webUrl, monitor.branch, monitor.vcs,
				monitor.recentCommit, monitor.previousCommit);

		try {
			this.gitWebHookHandler.handleRequest(repoInfo);
		} catch (final WebHookHandlerException e) {
			e.printStackTrace();
		}
	}

}
