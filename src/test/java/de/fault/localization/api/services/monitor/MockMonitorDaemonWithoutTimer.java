package de.fault.localization.api.services.monitor;

import de.fault.localization.api.services.hook.GitWebHookHandler;

public class MockMonitorDaemonWithoutTimer extends MonitorDaemon {

	public RepositoryMonitor monitor;
	public long interval;

	public MockMonitorDaemonWithoutTimer(GitWebHookHandler gitWebHookHandler) {
		super(gitWebHookHandler);
	}

	@Override
	void startTimer(RepositoryMonitor monitor, long interval) {
		this.monitor = monitor;
		this.interval = interval;
	}
}
