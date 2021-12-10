package de.fault.localization.api.utilities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.fault.localization.api.model.ApiParam;
import de.fault.localization.api.model.EntityField;
import lombok.val;
import org.reflections.ReflectionUtils;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.util.*;

/**
 * contains methods for analysing classes etc.
 */
public class ReflectionUtil {

    private ReflectionUtil(){

    }

    /**
     * @param field - the field
     * @return true if field is public
     */
    private static boolean isPublic(final Field field) {
        final int modifiers = field.getModifiers();
        return Modifier.isPublic(modifiers);
    }

    /**
     * @param method - the method
     * @return generic return type if available
     */
    public static Class<?> getGenericReturnType(final Method method) {
        try {
            final Type type = method.getGenericReturnType();
            if (type instanceof ParameterizedType) {
                final ParameterizedType parameterizedType = (ParameterizedType) type;
                final Type[] typeArguments = parameterizedType.getActualTypeArguments();
                for (final Type typeArgument : typeArguments) {
                    return (Class<?>) typeArgument;
                }
            }
        } catch (final Throwable ignored) {
            // no generic type found
        }
        return null;
    }

    /**
     * @param cls          - the class
     * @param genericClass - the generic class, may be null
     * @return pretty print
     */
    public static String printType(final Class<?> cls, final Class<?> genericClass) {
        String type = printClass(cls);
        if (genericClass != null) {
            type += "<" + printClass(genericClass) + ">";
        }
        return type;
    }


    /**
     * @param field - the field
     * @return generic return type if available
     */
    public static Class<?> getGenericType(final Field field) {
        final Type type = field.getGenericType();
        if (type instanceof ParameterizedType) {
            final ParameterizedType parameterizedType = (ParameterizedType) type;
            final Type[] typeArguments = parameterizedType.getActualTypeArguments();
            for (final Type typeArgument : typeArguments) {
                if (typeArgument instanceof Class) {
                    return (Class<?>) typeArgument;
                }
            }
        }
        return null;
    }

    /**
     * @param oldCls - older class
     * @param newCls - newer class
     * @return a list of all changed fields
     */
    public static Map<Class<?>, Class<?>> listRecursiveChangedPublicFields(final Class<?> oldCls, final Class<?> newCls) {
        final Map<Class<?>, Class<?>> newChangedModels = new LinkedHashMap<>();

        if (oldCls.isEnum() || newCls.isEnum()) {
            return newChangedModels;
        }

        final List<EntityField> oldPublicFields = getPublicFields(oldCls);
        final List<EntityField> newPublicFields = getPublicFields(newCls);

        if (oldPublicFields.isEmpty()) { // nothing to compare, end of recursion
            return newChangedModels;
        }

        for (final EntityField oldAttribute : oldPublicFields) {

            EntityField newAttribute = null;

            for (final EntityField newField : newPublicFields) {
                if (newField.getName().equals(oldAttribute.getName())) {
                    newAttribute = newField;
                    break;
                }
            }


                // check type
            if (newAttribute != null && !oldAttribute.equals(newAttribute)) {

                if (ComparerUtil.isEnumOrJava(oldAttribute.getType(), newAttribute.getType())) {
                    newChangedModels.put(oldAttribute.getType(), newAttribute.getType());
                }

                final Class<?> oldGenericAttributes = oldAttribute.getGenericType();
                final Class<?> newGenericAttributes = newAttribute.getGenericType();

                newChangedModels.putAll(listRecursiveChangedPublicFields(oldAttribute.getType(), newAttribute.getType()));

                if (oldGenericAttributes != null && newGenericAttributes != null) {
                    newChangedModels.putAll(listRecursiveChangedPublicFields(oldGenericAttributes, newGenericAttributes));
                }

                if (ComparerUtil.isEnumOrJava(oldGenericAttributes) && ComparerUtil.isEnumOrJava(newGenericAttributes)) {
                    newChangedModels.put(oldGenericAttributes, newGenericAttributes);
                }



            }

        }

        return newChangedModels;
    }

    /**
     * @param cls - the class
     * @return all fields of class as type {@link EntityField}
     */
    @SuppressWarnings("unchecked")
	static List<EntityField> getAllFields(final Class<?> cls) {
        final List<EntityField> fields = new ArrayList<>();

        for (final Field field : ReflectionUtils.getAllFields(cls)) {
            final Class<?> type = field.getType();
            final Class<?> genericType = getGenericType(field);
            final String name = field.getName();
            fields.add(new EntityField(name, type, genericType));
        }
        return fields;

    }

    /**
     * @param cls - the class
     * @return all public fields of class as type {@link EntityField}
     */
    @SuppressWarnings("unchecked")
	private static List<EntityField> getPublicFields(final Class<?> cls) {

        final List<EntityField> fields = new ArrayList<>();

        final List<PropertyDescriptor> getters = new ArrayList<>();

        try {
            getters.addAll(Arrays.asList(Introspector.getBeanInfo(cls).getPropertyDescriptors()));
        } catch (final IntrospectionException ignored) {
        }

        for (final Field field : ReflectionUtils.getAllFields(cls)) {

            if (field.getAnnotation(JsonIgnore.class) != null) {
                continue;
            }

            final Class<?> type = field.getType();

            final Class<?> genericType = getGenericType(field);

            if (!isPublic(field)) {
                // check if getter exists
                boolean getterExists = false;

                for (val getter : getters) {
                    if (getter.getName().equals(field.getName())) {
                        getterExists = true;
                        break;
                    }
                }

                if (!getterExists) {
                    continue; // -> next attribute
                }

            }

            String name = field.getName();

            final JsonProperty p = field.getAnnotation(JsonProperty.class);
            if (p != null) {
                name = p.value();
            }

            fields.add(new EntityField(name, type, genericType));

        }
        return fields;
    }

    /**
     * @param oldCls - the old class
     * @param newCls - the new class
     * @return true if {@code oldCls} and {@code newCls} do not differ
     */
    public static boolean isClassEquals(final Class<?> oldCls, final Class<?> newCls) {
        if (oldCls == null && newCls == null) {
            return true;
        }
        if (oldCls == null || newCls == null) {
            return false;
        }

        return oldCls.equals(newCls);
    }

    /**
     * @param cls - the class
     * @return true if primitive type (includes array)
     */
    static boolean isPrimitiveType(final Class<?> cls) {
        final String[] p = {"int", "string", "boolean", "short", "byte", "long", "double", "float"};


        for (val s : p) {
            if (s.equalsIgnoreCase(cls.getSimpleName()) || (s + "[]").equalsIgnoreCase(cls.getSimpleName())) {
                return true;
            }
        }
        return false;
    }

    public static String printClass(final Class<?> cls) {
        if (isPrimitiveType(cls) || ComparerUtil.isJava(cls)) {
            return cls.getSimpleName();
        }
        return cls.getName();
    }
}
