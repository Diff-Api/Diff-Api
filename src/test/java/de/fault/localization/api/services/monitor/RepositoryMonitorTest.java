package de.fault.localization.api.services.monitor;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import de.fault.localization.api.model.constants.VCS;

public class RepositoryMonitorTest {

	private final static String API_URL = "www.dummy.com/%s/%s/%s";

	@Nested
	class HasNewCommit {

		@Test
		@DisplayName("If there is no new commit, false should be returned")
		void sameCommitReturnsFalse() {
			var uut = new MockMonitorSameCommit("user", "repo", "branch", VCS.GITHUB, API_URL);
			uut.hasNewCommit();

			assertFalse(uut.hasNewCommit());
		}

		@Test
		@DisplayName("If there is a new commit, true should be returned")
		void differentcommitReturnsTrue() {
			var uut = new MockMonitorDifferentCommits("user", "repo", "branch", VCS.GITHUB, API_URL);
			uut.hasNewCommit();

			assertTrue(uut.hasNewCommit());
		}

		@Test
		@DisplayName("if there was no commit before, fals should be returned")
		void firstCommitReturnsFalse() {
			var uut = new MockMonitorDifferentCommits("user", "repo", "branch", VCS.GITHUB, API_URL);

			assertFalse(uut.hasNewCommit());
		}
	}
}
