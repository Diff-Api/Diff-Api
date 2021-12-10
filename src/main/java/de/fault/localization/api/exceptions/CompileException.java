package de.fault.localization.api.exceptions;

import java.io.IOException;

/**
 * exception being thrown if build fails and the project was not yet built
 */
public class CompileException extends IOException {

    private static final long serialVersionUID = 1L;

    public CompileException(final String string) {
        super(string);
    }

    public CompileException(final Throwable e) {
        super(e);
    }

}
