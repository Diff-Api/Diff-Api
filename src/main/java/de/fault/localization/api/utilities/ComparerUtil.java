package de.fault.localization.api.utilities;

import de.fault.localization.api.model.EntityField;
import de.fault.localization.api.services.comparer.ProjectMarkdownBuilder;
import lombok.val;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * contains methods for comparing models, classes etc.
 */
public class ComparerUtil {

	private ComparerUtil(){

	}

	/**
	 * @param method - method to analyse
	 * @return all headers of the method
	 */
	public static List<RequestHeader> getHeaders(final Method method) {
		final List<RequestHeader> headers = new LinkedList<>();
		if (method.getAnnotation(RequestHeader.class) != null) {
			final RequestHeader an = method.getAnnotation(RequestHeader.class);
			headers.add(an);

		}
		return headers;
	}

	/**
	 * @param method - method to analyse
	 * @return all verbs of the method
	 */
	public static List<RequestMethod> getRequestMethods(final Method method) {
		final List<RequestMethod> methods = new LinkedList<>();
		if (method.getAnnotation(GetMapping.class) != null) {
			methods.add(RequestMethod.GET);
		} else if (method.getAnnotation(PostMapping.class) != null) {
			methods.add(RequestMethod.POST);
		} else if (method.getAnnotation(PutMapping.class) != null) {
			methods.add(RequestMethod.PUT);
		} else if (method.getAnnotation(DeleteMapping.class) != null) {
			methods.add(RequestMethod.DELETE);
		} else if (method.getAnnotation(PatchMapping.class) != null) {
			methods.add(RequestMethod.PATCH);
		} else if (method.getAnnotation(RequestMapping.class) != null) {
			final RequestMapping an = method.getAnnotation(RequestMapping.class);
			for (final RequestMethod i : an.method()) {
				if (!methods.contains(i)) {
					methods.add(i);
				}
			}
		}
		return methods;
	}

	/**
	 * @return markdown string representing differences between {@code classOld} and
	 *         {@code classNew}
	 */
	public static String compareClassMarkdown(final Class<?> classOld, final Class<?> classNew) {
		final StringBuilder builtMarkDown = new StringBuilder();

		final List<EntityField> attributes1 = ReflectionUtil.getAllFields(classOld);
		final List<EntityField> attributes2 = ReflectionUtil.getAllFields(classNew);

		// dont compare enums
		if (isSameEnum(classOld, classNew)) {
			return null;
		}

		// compare all fields recursively
		for (final EntityField oldAttribute : attributes1) {

			EntityField matchingNewAttribute = null;

			for (final EntityField newField : attributes2) {
				if (newField.getName().equals(oldAttribute.getName())) {
					matchingNewAttribute = newField;
					break;
				}
			}

			if (matchingNewAttribute != null) {

				if (isSameEnum(oldAttribute.getType(), matchingNewAttribute.getType())) {
					continue;
				}
				if (!oldAttribute.equals(matchingNewAttribute)) {
					final String oldPrint = oldAttribute.print();
					final String newPrint = matchingNewAttribute.print();
					if (!oldPrint.equals(newPrint)) {
						// type differs
						builtMarkDown.append("! " + oldPrint + " -> " + newPrint + ProjectMarkdownBuilder.NL);
					}
				}

				attributes2.remove(matchingNewAttribute);
			} else {
				builtMarkDown.append("- " + oldAttribute.print() + ProjectMarkdownBuilder.NL);
			}
		}

		for (final EntityField newField : attributes2) {

			if (newField.getType().isEnum()) {
				continue;
			}

			builtMarkDown.append("+ " + newField.print() + ProjectMarkdownBuilder.NL);
		}

		return builtMarkDown.toString();
	}

	public static String getMarkdownAnchor(final String text) {
		return text.toLowerCase(Locale.ROOT).replace(" ", "-");
	}

	/**
	 * @param classes - classes to check
	 * @return false if any class is enum or part of a java library
	 */
	public static boolean isEnumOrJava(final Class<?>... classes) {
		for (val cls : classes) {
			final boolean track = cls != null && !isJava(cls) && !ReflectionUtil.isPrimitiveType(cls) && !cls.isEnum();
			if (!track) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @return true if {@code cls} is part of a java library
	 */
	static boolean isJava(final Class<?> cls) {
		return cls.getName().startsWith("java") || cls.getName().startsWith("sun");
	}

	/**
	 * @return true if {@code c1} and {@code c2} are enums that do not differ
	 */
	private static boolean isSameEnum(final Class<?> c1, final Class<?> c2) {
		return c1 != null && c2 != null && c1.getName().equals(c2.getName()) && c1.isEnum() && c2.isEnum();
	}

}
