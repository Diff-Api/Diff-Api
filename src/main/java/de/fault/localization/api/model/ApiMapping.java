package de.fault.localization.api.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * represents path mappings of one endpoint
 */
@Data
public class ApiMapping {
    private final List<String> path;

    public ApiMapping(final String[] a, final String[] b) {
        this.path = new ArrayList<>();
        this.path.addAll(Arrays.asList(a));
        this.path.addAll(Arrays.asList(b));
    }
}
