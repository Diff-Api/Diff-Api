package de.fault.localization.api.services.comparer;

import de.fault.localization.api.model.ApiEndPoint;
import de.fault.localization.api.model.ApiParam;
import de.fault.localization.api.model.ParsedWebHook;
import de.fault.localization.api.utilities.ComparerUtil;
import de.fault.localization.api.utilities.ListUtil;
import de.fault.localization.api.utilities.ReflectionUtil;
import de.fault.localization.api.utilities.StringUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.reflect.Parameter;
import java.util.*;

import static de.fault.localization.api.services.comparer.ProjectMarkdownBuilder.NL;

/**
 * this class compares different versions of one endpoints and returns the differences as markdown
 */
@RequiredArgsConstructor
public class ApiEndPointDiff {

    public static final String MD_DIFF = "```diff";
    private final ApiEndPoint newEndPoint, oldEndPoint;

    @Getter
    private boolean differs = false;

    private final StringBuilder markDown = new StringBuilder();

    /**
     * collects pairs of classes that do exist across different versions
     */
    @Getter
    private final ModelCollector comparableEntities = new ModelCollector();

    String getDiffAsMarkdown(final Map<Class<?>, Class<?>> changedEntities, final ParsedWebHook parsedWebHook) {
        if (!this.markDown.toString().isEmpty()) {
            return this.markDown.toString();
        }

        this.markDown.append("### " + this.newEndPoint.getPath() + NL);

        // path
        final String oldPath = this.oldEndPoint.getPath();
        final String newPath = this.newEndPoint.getPath();

        // check http methods
        final List<RequestMethod> oldVerbs = this.oldEndPoint.getVerbs();
        final List<RequestMethod> newVerbs = this.newEndPoint.getVerbs();

        String oldLink = null, newLink = null;
        try {
            newLink = StringUtil.joinChar(parsedWebHook.getAbsoluteUrl(), "/", "blob/") + parsedWebHook.getCommitIdNew() + "/" + this.newEndPoint.getLink();
            oldLink = StringUtil.joinChar(parsedWebHook.getAbsoluteUrl(), "/", "blob/") + parsedWebHook.getCommitIdOld() + "/" + this.oldEndPoint.getLink();
        } catch (final Exception ignored) {
        }

        if (oldPath.equals(newPath) && ListUtil.isUnorderedEqual(oldVerbs, newVerbs)) {
            this.markDown.append(this.oldEndPoint.printMarkdown(false, oldLink));
        } else {
            this.differs = true;
            this.markDown.append(this.oldEndPoint.printMarkdown(true, oldLink));
            this.markDown.append(this.newEndPoint.printMarkdown(false, newLink));
        }
        
        checkHttpHeaders();

        checkRequestParameters();

        // check return value
        this.compareResultEndPoints(this.oldEndPoint, this.newEndPoint);

        listReferencedModels(changedEntities);

        this.markDown.append("___" + NL);

        return this.markDown.toString();
    }

	private void listReferencedModels(final Map<Class<?>, Class<?>> changedEntities) {
		val foundEntities = this.comparableEntities.listEntities();
        
        if (changedEntities != null) // -> not initializing
        {
            foundEntities.removeIf(s -> !changedEntities.containsKey(s) && !changedEntities.containsValue(s));
        }

        if (!foundEntities.isEmpty()) {
            this.markDown.append("#### Referenced Models" + NL);
            this.differs = true;
            final Set<String> list = new LinkedHashSet<>();
            // remove different classes with same package names
            for (val cls : foundEntities) {
                list.add("- [" + ReflectionUtil.printClass(cls) + "](#" + ComparerUtil.getMarkdownAnchor(cls.getSimpleName()) + ")");
            }
            for (val s : list) {
                this.markDown.append(s + NL);
            }

        }
	}

	private void checkRequestParameters() {
		final List<ApiParam> oldParameters = this.getParameter(this.oldEndPoint.getParameters());
        final List<ApiParam> newParameters = this.getParameter(this.newEndPoint.getParameters());

        this.compareParameters(oldParameters, newParameters);
	}

	private void checkHttpHeaders() {
		val oldHeaders = this.oldEndPoint.getHeaders();
        val newHeaders = this.newEndPoint.getHeaders();
        if (!ListUtil.isUnorderedEqual(oldHeaders, newHeaders)) {
            this.differs = true;
            this.compareHeaders(oldHeaders, newHeaders);
        }
	}

    private void compareResultEndPoints(final ApiEndPoint oldEndPoint, final ApiEndPoint newEndPoint) {

        final Class<?> oldGenericReturn = oldEndPoint.getGenericReturnType();
        final Class<?> newGenericReturn = newEndPoint.getGenericReturnType();

        final Class<?> oldReturn = oldEndPoint.getReturnType();
        final Class<?> newReturn = newEndPoint.getReturnType();

        if (ReflectionUtil.isClassEquals(oldReturn, newReturn) && ReflectionUtil.isClassEquals(oldGenericReturn, newGenericReturn)) {
            return;
        }

        final Class<?> oldCls;
        final Class<?> newCls;

        this.comparableEntities.addOldEntity(oldReturn);
        this.comparableEntities.addOldEntity(oldGenericReturn);
        this.comparableEntities.addNewEntity(newReturn);
        this.comparableEntities.addNewEntity(newGenericReturn);

        if (!ReflectionUtil.isClassEquals(oldGenericReturn, newGenericReturn)) {
            oldCls = oldGenericReturn;
            newCls = newGenericReturn;
        } else {
            oldCls = oldReturn;
            newCls = newReturn;
        }

        this.differs = true;
        this.markDown.append("#### Return" + NL);

        this.markDown.append(MD_DIFF + NL);
        if (oldCls.getName().equals(newCls.getName())) {
            // same class -> was modified
            this.markDown.append("! " + oldEndPoint.printReturnType() + NL);
        } else {
            this.markDown.append("! " + oldEndPoint.printReturnType() + " -> " + newEndPoint.printReturnType() + NL);
        }
        this.markDown.append("```" + NL);

    }

    private void compareParameters(final List<ApiParam> oldParams, final List<ApiParam> newParams) {

        if (ListUtil.isUnorderedEqual(oldParams, newParams)) {
            return;
        }

        // check used models
        for (val param : oldParams) {
            this.comparableEntities.addOldEntity(param.getType());
            this.comparableEntities.addOldEntity(param.getGenericClass());
        }
        for (val param : newParams) {
            this.comparableEntities.addNewEntity(param.getType());
            this.comparableEntities.addNewEntity(param.getGenericClass());
        }

        final StringBuilder diff = new StringBuilder();

        for (final ApiParam oldParam : oldParams) {

            ApiParam newParam = null;

            for (final ApiParam newField : newParams) {
                if (newField.getName().equals(oldParam.getName())) {
                    newParam = newField;
                    break;
                }
            }

            if (newParam != null) {
                // check type
                if (!oldParam.printType().equals(newParam.printType())) {
                    diff.append("! " + oldParam.printType() + " -> " + newParam.printType() + NL);
                }
                newParams.remove(newParam);
            } else {
                diff.append("- " + oldParam.printType() + NL);
            }

        }

        for (final ApiParam apiParam : newParams) {
            diff.append("+ " + apiParam.printType() + NL);
        }


        if (!diff.toString().isEmpty()) {
            this.differs = true;
            this.markDown.append("#### Parameters" + NL);
            this.markDown.append(MD_DIFF + NL);
            this.markDown.append(diff);
            this.markDown.append("```" + NL);
        }

    }

    private void compareHeaders(final List<RequestHeader> oldHeaders, final List<RequestHeader> newHeaders) {

        this.markDown.append("#### Headers" + NL);
        this.markDown.append(MD_DIFF + NL);

        for (val oldHeader : oldHeaders) {

            RequestHeader newHeader = null;

            for (val newHeaderField : newHeaders) {
                if (newHeaderField.name().equals(oldHeader.name())) {
                    newHeader = newHeaderField;
                    break;
                }
            }

            if (newHeader != null) {
                // check type
                if (!oldHeader.equals(newHeader)) {
                    this.markDown.append("! " + oldHeader.name() + " -> " + newHeader.name() + NL);
                }
                newHeaders.remove(newHeader);
            } else {
                this.markDown.append("- " + oldHeader.name() + NL);
            }

        }

        for (val apiHeader : newHeaders) {
            this.markDown.append("+ " + apiHeader.name() + NL);
        }

        this.markDown.append("```" + NL);

    }

    private List<ApiParam> getParameter(final Parameter[] oldReturn) {
        final List<ApiParam> ls = new ArrayList<>();

        for (final Parameter param : oldReturn) {

            boolean isBody = false;

            boolean required = false;
            final RequestParam p1 = param.getAnnotation(RequestParam.class);
            if (p1 != null) {
                required = p1.required();
            }
            final RequestBody p2 = param.getAnnotation(RequestBody.class);
            if (p2 != null) {
                required = p2.required();
                isBody = true;
            }

            ls.add(new ApiParam(param, required, isBody));

        }

        return ls;
    }

}
