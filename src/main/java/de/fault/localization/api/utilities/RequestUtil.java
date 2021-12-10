package de.fault.localization.api.utilities;

import javax.servlet.http.HttpServletRequest;

/**
 * contains methods for getting information about requesting user agent
 */
public class RequestUtil {

    private RequestUtil(){

    }

    /**
     * @return ip address of client
     */
    public static String getIP(final HttpServletRequest request) {
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }
}
