package de.fault.localization.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * starts the application
 */
@EnableSwagger2
@SpringBootApplication
public class Application {

    public static void main(final String[] argv) {
        SpringApplication.run(Application.class, argv);
    }

}
