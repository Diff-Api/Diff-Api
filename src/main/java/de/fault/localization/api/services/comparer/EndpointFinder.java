package de.fault.localization.api.services.comparer;

import de.fault.localization.api.model.ApiEndPoint;
import de.fault.localization.api.model.ApiMapping;
import de.fault.localization.api.model.ControllerFile;
import de.fault.localization.api.utilities.StringUtil;
import lombok.val;
import org.springframework.web.bind.annotation.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * this class provides methods for detecting endpoints
 */
public class EndpointFinder {

	private EndpointFinder(){

	}
	static List<ApiEndPoint> getEndpoints(final ControllerFile controllerFile) {
		final List<ApiEndPoint> ls = new LinkedList<>();

		for (final Method method : controllerFile.getCls().getMethods()) {
			for (final Annotation annotation : method.getAnnotations()) {
				if (isMapping(annotation)) {
					ls.add(new ApiEndPoint(method, controllerFile));
				}
			}
		}

		return ls;
	}

	private static boolean isMapping(final Annotation a) {
		return getMapping(a) != null;
	}

	private static List<ApiMapping> getPath(final Annotation[] b) {
		final List<ApiMapping> ls = new ArrayList<>();
		for (val a : b) {
			if (a instanceof RequestMapping) {
				final RequestMapping requestMapping = (RequestMapping) a;
				ls.add(new ApiMapping(requestMapping.path(), requestMapping.value()));
			} else if (a instanceof PostMapping) {
				final PostMapping requestMapping = (PostMapping) a;
				ls.add(new ApiMapping(requestMapping.path(), requestMapping.value()));
			} else if (a instanceof GetMapping) {
				final GetMapping requestMapping = (GetMapping) a;
				ls.add(new ApiMapping(requestMapping.path(), requestMapping.value()));
			} else if (a instanceof PutMapping) {
				final PutMapping requestMapping = (PutMapping) a;
				ls.add(new ApiMapping(requestMapping.path(), requestMapping.value()));
			} else if (a instanceof DeleteMapping) {
				final DeleteMapping requestMapping = (DeleteMapping) a;
				ls.add(new ApiMapping(requestMapping.path(), requestMapping.value()));
			} else if (a instanceof PatchMapping) {
				final PatchMapping requestMapping = (PatchMapping) a;
				ls.add(new ApiMapping(requestMapping.path(), requestMapping.value()));
			}

		}
		return ls;
	}

	@SuppressWarnings("unchecked")
	private static Class<? extends RequestMapping> getMapping(final Annotation a) {

		if (a.annotationType().equals(RequestMapping.class)) {
			return (Class<? extends RequestMapping>) a.annotationType();
		}

		for (final Annotation it : a.annotationType().getDeclaredAnnotations()) {
			if (it.annotationType().equals(RequestMapping.class)) {
				return (Class<? extends RequestMapping>) a.annotationType();
			}

		}
		return null;
	}

	public static String getPathName(final Method method, final Class<?> controller) {

		String finalPath = "";

		final List<ApiMapping> controllerMapping = getPath(controller.getAnnotations());

		if (!controllerMapping.isEmpty()) {
			val map = controllerMapping.get(0);
			finalPath += String.join(", ", map.getPath());
		}
		final List<ApiMapping> methodMapping = getPath(method.getAnnotations());

		if (!methodMapping.isEmpty()) {
			val map = methodMapping.get(0);

			if (!map.getPath().isEmpty()) {
				finalPath = StringUtil.joinChar(finalPath, "/", String.join(", ", map.getPath()));
			}
		}
		return StringUtil.appendStartOptional("/", finalPath);

	}

}
