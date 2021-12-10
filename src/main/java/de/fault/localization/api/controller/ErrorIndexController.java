package de.fault.localization.api.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

/**
 * fallback controller
 */
@ApiIgnore
@RestController
public class ErrorIndexController implements ErrorController {

    private static final String PATH = "/error";

    @RequestMapping(value = ErrorIndexController.PATH)
    public ResponseEntity<String> error() {
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @Override
    public String getErrorPath() {
        return ErrorIndexController.PATH;
    }

}
