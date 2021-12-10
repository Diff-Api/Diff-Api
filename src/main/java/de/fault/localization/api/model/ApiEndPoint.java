package de.fault.localization.api.model;

import de.fault.localization.api.services.comparer.ApiEndPointDiff;
import de.fault.localization.api.services.comparer.EndpointFinder;
import de.fault.localization.api.utilities.ComparerUtil;
import de.fault.localization.api.utilities.FileUtil;
import de.fault.localization.api.utilities.ReflectionUtil;
import lombok.Getter;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.stream.Collectors;

import static de.fault.localization.api.services.comparer.ProjectMarkdownBuilder.NL;

/**
 * class represents comparable attributes of api endpoints
 */
@Getter
public class ApiEndPoint {

    // used for linking to exact file + line number
    private final Method method;
    private final ControllerFile controllerFile;

    // absolute path
    private final String path;
    private final List<RequestMethod> verbs;
    private final List<RequestHeader> headers;
    private final Parameter[] parameters;
    private final Class<?> genericReturnType, returnType;

    /**
     * @param method         - method
     * @param controllerFile - file in which endpoint is defined
     */
    public ApiEndPoint(final Method method, final ControllerFile controllerFile) {
        this.method = method;
        this.verbs = ComparerUtil.getRequestMethods(method);
        this.path = EndpointFinder.getPathName(method, method.getDeclaringClass());
        this.parameters = method.getParameters();

        // header from endpoint
        this.headers = ComparerUtil.getHeaders(method);
        this.controllerFile = controllerFile;

        final Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (final Annotation[] annotations : parameterAnnotations) {
            for (final Annotation annotation : annotations) {
                if (annotation instanceof RequestHeader) {
                    final RequestHeader requestHeader = (RequestHeader) annotation;
                    this.headers.add(requestHeader);
                }
            }
        }

        this.genericReturnType = ReflectionUtil.getGenericReturnType(method);
        this.returnType = method.getReturnType();
    }

    /**
     * @return pretty printed return type
     */
    public String printReturnType() {
        String r = ReflectionUtil.printClass(this.returnType);
        if (this.genericReturnType != null) {
            r += "<" + ReflectionUtil.printClass(this.genericReturnType) + ">";
        }
        return r;
    }

    public ApiEndPointDiff diff(final ApiEndPoint old) {
        return new ApiEndPointDiff(this, old);
    }

    /**
     * pretty print end point data
     *
     * @param isDeleted - true if endpoint was deleted
     * @param link      - link for file
     * @return endpoint data as mark down
     */
    public String printMarkdown(final boolean isDeleted, final String link) {
        String pathMd = this.getPath();

        if (link != null) {
            pathMd = "[" + pathMd + "](" + link + ")";
        }

        if (isDeleted) {
            return String.format("> ~~`%s`~~ %s %s", this.getVerbs().stream().map(Enum::name).collect(Collectors.joining(", ")), pathMd, NL);
        } else {
            return String.format("> `%s` %s %s", this.getVerbs().stream().map(Enum::name).collect(Collectors.joining(", ")), pathMd, NL);
        }
    }

    /**
     * @return vcs link
     */
    public String getLink() {
        String url = this.getControllerFile().getRelativePath();
        try {
            // try getting the line number
            final String search = " " + this.method.getName() + "(";
            final int line = FileUtil.getLineNumber(this.getControllerFile().getFile(), search);
            if (line != -1) {
                url += "#L" + line;
            }
        } catch (final IOException ignored) {
        }
        return url;
    }
}
