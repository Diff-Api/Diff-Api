package de.fault.localization.api.services.settings;

import de.fault.localization.api.services.monitor.yaml.RepositoryProperties;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * loads configuration file
 */
public class Config {
    public static final File CONFIG_FILE = new File("config.yaml");

    private final static RepositoryProperties config;

    private Config(){

    }

    /*
     * initializes config
     */
    static {
        final Yaml yaml = new Yaml(new Constructor(RepositoryProperties.class));
        final InputStream inputStream;
        try {
            inputStream = new FileInputStream(CONFIG_FILE);
            config = yaml.load(inputStream);
        } catch (final FileNotFoundException e) {
            throw new RuntimeException("error loading config properties");
        }
    }

    /**
     * @return cached config
     */
    public static RepositoryProperties get() {
        return config;
    }
}
