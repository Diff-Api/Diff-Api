package de.fault.localization.api.model;

import de.fault.localization.api.utilities.ReflectionUtil;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * represents one field of a model
 */
@Data
@AllArgsConstructor
public class EntityField {
    private String name;
    private Class<?> type, genericType;

    public String print() {
        return this.name + ": " + ReflectionUtil.printType(this.type, this.genericType);
    }
}
