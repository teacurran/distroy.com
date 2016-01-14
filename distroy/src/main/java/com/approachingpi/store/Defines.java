/*
 * Approaching Pi, Inc.
 * User: Tea Curran
 * Date: Aug 4, 2002
 * Time: 2:44:56 AM
 */
package com.approachingpi.store;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Defines extends Properties {
    private Log log = LogFactory.getLog(Defines.class);

    public static final String INF_FOLDER           = "WEB-INF";

    public static final String DATA_SOURCE          = "jdbc/MainDs";                        // default data source, application only uses one
    public static final String DEFAULT_ERROR_PAGE   = "/jsp/error.jsp";                     // error page for any terminal error that makes it so we cannot display the current page
    public static final String ADMIN_ERROR_PAGE     = "/jsp/admin/error.jsp";               // error page for any terminal error that makes it so we cannot display the current page for admin section
    public static final String ADMIN_EXPIRED_PAGE   = "/jsp/admin/session_expired.jsp";     // the page the user will get if their session expires

    // Error strings the user sees
    public static final String ERROR_NOT_LOGGED_IN  = "Your session has expired or you have been logged out.";
    public static final String ERROR_NO_ACCESS      = "You do not have permission to access this resource.";

    public static final int SESSION_LOGIN_EXPIRE    = 86400;    // 24 hours
    public static final int SESSION_COOKIE_AGE      = 2592000;  // 30 days

    public static final int DEFAULT_PASSWORD_EXPIRE = 90;   // in Days

    //Properties properties;


    public Defines() {
        reload("");
    }

    public Defines(String path) {
        reload(path);
    }

    public void reload(String path) {
        if (path == null) {
            path = "";
        }

		String fileUri;
		File propertyFile;
		FileInputStream inputStream;

		if (new File(path).isDirectory()) {
			if (path.length() >= 1) {
				if (!path.substring(path.length() - 1, path.length()).equals(File.separator)) {
						path += File.separator;
				}
			}
			fileUri = path + INF_FOLDER + File.separator + "defines.properties";
		} else {
			fileUri = path;
		}

        log.debug("defines uri:" + fileUri);

        try {
            propertyFile = new File(fileUri);

            inputStream = new FileInputStream(propertyFile);

            this.load(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getProperty(String property) {
        if (!containsKey(property)) {
            return "";
        }
        return super.getProperty(property);
    }

    public int getPropertyInt(String property) {
        int retval = 0;
        try {
            retval = Integer.parseInt(super.getProperty(property));
        } catch (Exception e) {}
        return retval;
    }
}
