package de.fault.localization.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.fault.localization.api.model.GitRepository;

/**
 * This repository contains needed query methods for the Repository table
 */
@Repository
public interface GitStorageRepository extends JpaRepository<GitRepository, Integer> {
	GitRepository findByGitUrl(final String gitUrl);
}