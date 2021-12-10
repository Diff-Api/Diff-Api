package de.fault.localization.api.model;

import de.fault.localization.api.utilities.ReflectionUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.val;

import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * represents parameters of one endpoint
 */
@Data
@AllArgsConstructor
public class ApiParam {

    private String name;
    private Class<?> type;
    private Type genericType;
    private boolean required, isBody;

    public ApiParam(final Parameter param, final boolean required, final boolean isBody) {
        this.name = param.getName();
        this.type = param.getType();
        this.genericType = param.getParameterizedType();
        this.required = required;
        this.isBody = isBody;
    }

    /**
     * @return generic type if available, else null
     */
    public Class<?> getGenericClass() {
        if (this.genericType instanceof ParameterizedType) {
            final ParameterizedType parameterizedType = (ParameterizedType) this.genericType;
            return (Class<?>) (parameterizedType).getActualTypeArguments()[0];
        }
        return null;
    }

    /**
     * @return pretty print type, also considers generic types
     */
    public String printType() {
        String type = ReflectionUtil.printClass(this.type);
        val genericClass = this.getGenericClass();
        if (genericClass != null) {
            type += "<" + ReflectionUtil.printClass(genericClass) + ">";
        }
        return this.name + ": " + type + (this.required ? " required" : "");
    }
}
