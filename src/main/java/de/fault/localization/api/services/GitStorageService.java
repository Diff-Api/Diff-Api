package de.fault.localization.api.services;

import de.fault.localization.api.model.GitRepository;
import de.fault.localization.api.repository.GitStorageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * This service represents the cloned repositories in the database
 */
@Service
public class GitStorageService {
    private final GitStorageRepository enterpriseRepository;

    @Autowired
    public GitStorageService(final GitStorageRepository enterpriseRepository) {
        this.enterpriseRepository = enterpriseRepository;
    }

    /**
     * if repository exists no operations on database are executed
     *
     * @param repositoryUrl    - repository url
     * @param localStorageName - name of corresponding folder where project is supposed to be cloned
     * @return - matching repo or new repo if not persisted yet
     */
    public GitRepository createOrReturnRepo(final String repositoryUrl, final String localStorageName) {
        final GitRepository foundEntry = this.enterpriseRepository.findByGitUrl(repositoryUrl);
        if (foundEntry != null) {
            return foundEntry;
        }
        return this.enterpriseRepository.save(new GitRepository(-1, repositoryUrl, localStorageName));
    }

    /**
     * @return list of all persisted repositories
     */
    public List<GitRepository> listAll() {
        return this.enterpriseRepository.findAll();
    }

}
