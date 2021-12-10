package de.fault.localization.api.services.settings;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ConfigTest {

    @Test
    void readConfig() {
        assertNotNull(Config.get().getCcimsApi());
        assertEquals(Config.get(), Config.get());
    }
}