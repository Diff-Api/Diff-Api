package de.fault.localization.api.services.hook;

import de.fault.localization.api.exceptions.WebHookHandlerException;
import de.fault.localization.api.external.CCIMSApi;
import de.fault.localization.api.model.GitRepository;
import de.fault.localization.api.model.ParsedWebHook;
import de.fault.localization.api.services.GitStorageService;
import de.fault.localization.api.services.comparer.ControllerFinder;
import de.fault.localization.api.services.comparer.ProjectMarkdownBuilder;
import lombok.extern.java.Log;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;

import java.io.File;
import java.io.IOException;

/**
 * this class parses all incoming web hooks
 */
@Log
public class GitWebHookHandler {

    private final GitStorageService service;

    GitWebHookHandler(final GitStorageService service) {
        this.service = service;
    }

    /**
     * @param parsedWebHook - the webhook
     * @throws WebHookHandlerException - if there was an error building the project or the webhook was invalid
     */
    public void handleRequest(final ParsedWebHook parsedWebHook) throws WebHookHandlerException {

        // make sure repo is registered in database & fetch id
        final String repositoryUrl = parsedWebHook.getAbsoluteUrl();
        final String branch = parsedWebHook.getBranch();
        final GitRepository repository = this.service.createOrReturnRepo(repositoryUrl,
                branch + "_" + System.currentTimeMillis());

        final String localFolderName = repository.getLocalStorageName();

        // create folder
        final File folder = new File(GitWebHookWorkspaceManager.getBuildDirectory(), localFolderName);
        folder.mkdirs();

        if (!folder.isDirectory()) {
            throw new WebHookHandlerException("folder %s was not created", folder.getAbsolutePath());
        }

        // which versions are available
        final File oldBuildDirectory = new File(folder, GitWebHookWorkspaceManager.getOldBuildName());
        final File newBuildDirectory = new File(folder, GitWebHookWorkspaceManager.getNewBuildName());

        try {

            if (!oldBuildDirectory.isDirectory()) {

                log.info("not yet built");

                if (!parsedWebHook.isInitial()) {
                    // clone older commit to new
                    val git = Git.cloneRepository().setURI(repositoryUrl).setBranch(branch)
                            .setCloneSubmodules(false).setDirectory(newBuildDirectory).call();
                    git.checkout().setName(parsedWebHook.getCommitIdOld()).call();
                } else {
                    log.info("initial commit, I will do nothing");
                    return;
                }

            } else {
                if (!newBuildDirectory.isDirectory()) {
                    throw new WebHookHandlerException("new directory was not found");
                }
            }

            // delete old
            FileUtils.deleteDirectory(oldBuildDirectory);
            FileUtils.moveDirectory(newBuildDirectory, oldBuildDirectory);

            log.info("cloning new " + repositoryUrl);

            // clone newest into new
            Git.cloneRepository().setURI(repositoryUrl).setBranch(branch)
                    .setCloneSubmodules(false).setDirectory(newBuildDirectory).call();

            log.info("cloned branch " + branch + " to " + newBuildDirectory.getAbsolutePath());
            final String markdown = this.getMarkdownDiff(oldBuildDirectory, newBuildDirectory, parsedWebHook);

            createIssueInGropius(parsedWebHook, repositoryUrl, markdown);
        } catch (final Exception e) {
            throw new WebHookHandlerException(e);
        }

    }

    private void createIssueInGropius(ParsedWebHook parsedWebHook, String repositoryUrl, String markdown) {
        try {
            // components.filter = url
            String componentId = CCIMSApi.getComponentId(repositoryUrl);

            if (componentId == null) {
                final String[] spl = repositoryUrl.split("/");
                componentId = CCIMSApi.createComponent(repositoryUrl, spl[spl.length - 1]);
            }

            final String title = "Incompatibility introduced in commit " + parsedWebHook.getCommitIdNew().substring(0, 5) + "..";

            final String issueId = CCIMSApi.createIssue(title, markdown, componentId);
            log.info("created ticket with id " + issueId);
        } catch (final Exception e) {
            log.severe("there was an error contacting ccims api, make sure configuration is valid");
            e.printStackTrace();
        }
    }

    /**
     * triggers the comparison process
     *
     * @param oldBuild      - the old build directory
     * @param newBuild      - the new build directory
     * @param parsedWebHook - the webhook
     * @return differences as markdown
     * @throws IOException if there is an io related error
     */
    private String getMarkdownDiff(final File oldBuild, final File newBuild, final ParsedWebHook parsedWebHook) throws IOException {

        log.info("building both versions");

        val oldControllers = ControllerFinder.getControllers(oldBuild);
        val newControllers = ControllerFinder.getControllers(newBuild);

        log.info("start comparison");

        final ProjectMarkdownBuilder projectMarkdownBuilder = new ProjectMarkdownBuilder(oldControllers, newControllers, parsedWebHook);

        log.info("comparison finished");

        return projectMarkdownBuilder.toMarkDown();

    }


}
