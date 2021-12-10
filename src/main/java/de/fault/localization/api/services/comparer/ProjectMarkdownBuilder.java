package de.fault.localization.api.services.comparer;

import de.fault.localization.api.model.ApiEndPoint;
import de.fault.localization.api.model.ControllerFile;
import de.fault.localization.api.model.ParsedWebHook;
import de.fault.localization.api.utilities.ComparerUtil;
import de.fault.localization.api.utilities.ListUtil;
import de.fault.localization.api.utilities.ReflectionUtil;
import de.fault.localization.api.utilities.StringUtil;
import lombok.val;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * this class is central and returns the differences of two applications as a markdown issue
 */
public class ProjectMarkdownBuilder {

    // line separator
    public final static String NL = "\n";

    private final List<ControllerFile> oldControllers;
    private final List<ControllerFile> newControllers;

    // the webhook is used to reference to the corresponding files on the repository
    private final ParsedWebHook parsedWebHook;

    private final StringBuilder builtMarkDown = new StringBuilder();

	private ModelCollector referencedEntities;

	private Map<Class<?>, Class<?>> changedModels;

    public ProjectMarkdownBuilder(final List<ControllerFile> oldControllers, final List<ControllerFile> newControllers, final ParsedWebHook parsedWebHook) {
        this.oldControllers = oldControllers;
        this.newControllers = newControllers;
        this.parsedWebHook = parsedWebHook;
    }

    /**
     * @return markdown being built, the result is being cached
     */
    public String toMarkDown() {
        if (!this.builtMarkDown.toString().isEmpty()) {
            return this.builtMarkDown.toString();
        }

        val oldEndpoints = new ArrayList<ApiEndPoint>();
        val newEndpoints = new ArrayList<ApiEndPoint>();

        final Map<ApiEndPoint, ApiEndPoint> modifiedEndpoints = new LinkedHashMap<>();
        final List<ApiEndPoint> deletedEndpoints = new ArrayList<>();

        for (val controller : this.oldControllers) {
            oldEndpoints.addAll(EndpointFinder.getEndpoints(controller));
        }
        for (val controller : this.newControllers) {
            newEndpoints.addAll(EndpointFinder.getEndpoints(controller));
        }

        for (val oldEndpoint : oldEndpoints) {

            ApiEndPoint correspondingNewEndPoint = null;

            for (val newEndPoint : newEndpoints) {
                // considered equal, if verb and path is same
                if (ListUtil.isUnorderedEqual(oldEndpoint.getVerbs(), newEndPoint.getVerbs()) && oldEndpoint.getPath().equals(newEndPoint.getPath())) {
                    correspondingNewEndPoint = newEndPoint;
                    break;
                }
            }
            if (correspondingNewEndPoint == null) {
                // -> endpoint was deleted
                deletedEndpoints.add(oldEndpoint);
            } else {
                modifiedEndpoints.put(oldEndpoint, correspondingNewEndPoint);
            }

            newEndpoints.removeIf(newEndPoint -> ListUtil.isUnorderedEqual(oldEndpoint.getVerbs(), newEndPoint.getVerbs()) && oldEndpoint.getPath().equals(newEndPoint.getPath()));

        }

        if (!deletedEndpoints.isEmpty()) {
            this.builtMarkDown.append("## Deleted Endpoints" + NL);
            for (val deletedEndpoint : deletedEndpoints) {

                String oldLink = null;
                try {
                    oldLink = StringUtil.joinChar(this.parsedWebHook.getAbsoluteUrl(), "/", "blob/") + this.parsedWebHook.getCommitIdOld() + "/" + deletedEndpoint.getLink();
                } catch (final Exception ignored) {
                }

                this.builtMarkDown.append(deletedEndpoint.printMarkdown(true, oldLink));
            }
        }

        referencedEntities = new ModelCollector();
        
        getReferencedModels(modifiedEndpoints);

        // filter out unchanged models
        changedModels = referencedEntities.getChangedModels();

        compareUpdatedEndpoints(modifiedEndpoints);

        listNewEndpoints(newEndpoints);

        listChangedModels();

        return this.builtMarkDown.toString();
    }

	private void listChangedModels() {
		if (!changedModels.isEmpty()) {

            this.builtMarkDown.append("## Updated Models" + NL);

            for (val entry : changedModels.entrySet()) {

                val oldCls = entry.getKey();
                val newCls = entry.getValue();

                final String diff = ComparerUtil.compareClassMarkdown(oldCls, newCls);

                assert !StringUtils.isEmpty(diff);

                // simpleName not sufficient, class names may overlap
                this.builtMarkDown.append("### " + ReflectionUtil.printClass(newCls) + NL);
                this.builtMarkDown.append("```diff" + NL);
                this.builtMarkDown.append(diff);
                this.builtMarkDown.append("```" + NL);

            }

        }
	}

	private void listNewEndpoints(final ArrayList<ApiEndPoint> newEndpoints) {
		if (!newEndpoints.isEmpty()) {
            this.builtMarkDown.append("## New Endpoints" + NL);
            for (val newEndPoint : newEndpoints) {

                String newLink = null;
                try {
                    newLink = StringUtil.joinChar(this.parsedWebHook.getAbsoluteUrl(), "/", "blob/") + this.parsedWebHook.getCommitIdNew() + "/" + newEndPoint.getLink();
                } catch (final Exception ignored) {
                }

                this.builtMarkDown.append(newEndPoint.printMarkdown(false, newLink));
            }
        }
	}

	private void compareUpdatedEndpoints(final Map<ApiEndPoint, ApiEndPoint> modifiedEndpoints) {
		if (!modifiedEndpoints.isEmpty()) {
            this.builtMarkDown.append("## Updated Endpoints" + NL);
            for (val endpointPair : modifiedEndpoints.entrySet()) {
                val old = endpointPair.getKey();
                val newOne = endpointPair.getValue();

                val apiEndPointDiff = newOne.diff(old);

                final String markdown = apiEndPointDiff.getDiffAsMarkdown(changedModels, this.parsedWebHook);
                if (!apiEndPointDiff.isDiffers()) {
                    continue;
                }
                this.builtMarkDown.append(markdown);
                referencedEntities.join(apiEndPointDiff.getComparableEntities());

            }
        }
	}

	private void getReferencedModels(final Map<ApiEndPoint, ApiEndPoint> modifiedEndpoints) {
		for (val endpointPair : modifiedEndpoints.entrySet()) {
            val old = endpointPair.getKey();
            val newOne = endpointPair.getValue();
            val diff = newOne.diff(old);
            referencedEntities.join(diff.getComparableEntities());
            diff.getDiffAsMarkdown(null, this.parsedWebHook);
            referencedEntities.join(diff.getComparableEntities());
        }
	}


}
