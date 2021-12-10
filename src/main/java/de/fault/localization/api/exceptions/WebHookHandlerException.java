package de.fault.localization.api.exceptions;

/**
 * exception being thrown on general errors related to parsing/ handling webhooks
 */
public class WebHookHandlerException extends Exception {
    private static final long serialVersionUID = -2441640665819086292L;

    public WebHookHandlerException(final String msg, final Object... args) {
        super(String.format(msg, args));
    }

    public WebHookHandlerException(final Exception e) {
        super(e);
    }
}
