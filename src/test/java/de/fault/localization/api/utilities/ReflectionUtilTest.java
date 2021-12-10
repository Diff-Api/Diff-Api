package de.fault.localization.api.utilities;

import de.fault.localization.api.controller.GitStorageController;
import de.fault.localization.api.services.comparer.EndpointFinder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReflectionUtilTest {

    @Test
    void test() throws Exception {

        final Class<?> controller = GitStorageController.class;

        final String s = EndpointFinder.getPathName(controller.getDeclaredMethod("list"), controller);
        assertEquals("/api/repository/list", s);

    }

}