/*
 * Approaching Pi, Inc.
 * User: Tea Curran
 * Date: Sep 22, 2002
 * Time: 6:32:33 PM
 */
package com.approachingpi.servlet;

import com.approachingpi.store.Defines;
import com.approachingpi.store.Store;
import com.approachingpi.user.User;
import com.approachingpi.util.MessageBean;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PiServlet extends HttpServlet {
    private Log log = LogFactory.getLog(PiServlet.class);

    public static final String ATTRIBUTE_ERRORBEAN      = "errorBean";
    public static final String ATTRIBUTE_MESSAGEBEAN    = "messageBean";
    public static final String ATTRIBUTE_USER           = "user";
    public static final String ATTRIBUTE_ALT            = "altAttribute";
    public static final String ATTRIBUTE_SESSION        = "piSession";
    public static final String ATTRIBUTE_STORE          = "piStore";

    public static final String STORE_DEFAULT            = "DIS";

    Defines defines;

    protected void initPiServlet(HttpServletRequest req, HttpServletResponse res) {
        //getErrorBean(req);
        //getMessageBean(req);
        //getUser(req);
        //getAltAttribute(req);
        if (defines == null || PiServlet.getReqBoolean(req,"updatedefines")) {
            log.debug("reloading defines.");
            String filePath = getServletContext().getRealPath("/");
            if (filePath != null) {
                // trim off the ".\"
                if (filePath.substring(filePath.length() - 2, filePath.length()).equals(".\\")) {
                    filePath = filePath.substring(0, filePath.length() - 2);
                }
            }
            log.debug("defines filepath:" + filePath);
            defines = new Defines(filePath);
        }
    }

    protected boolean checkPermission(HttpServletRequest req, HttpServletResponse res, String permissionRequired, Connection con) throws IOException, ServletException {
        return checkPermission(req, res, permissionRequired,con,"main");
    }

    protected boolean checkPermission(HttpServletRequest req, HttpServletResponse res, String permissionRequired, Connection con, String store) throws IOException, ServletException {
        boolean retVal = true;

        User user = getSession(req,res,con).getUser();

        if (user.getId() == 0) {
	        retVal = false;
        } else if (!user.hasPermission(permissionRequired)) {
            //errorBean.addMessage(Defines.ERROR_NO_ACCESS);
            //errorBean.setIsFatal(true);
            retVal = false;
        }
        return retVal;
    }

    /*
    protected static void closeConnection(Connection con) {
        try {
            con.close();
            con = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    */

    public void forwardRequest(HttpServletRequest req, HttpServletResponse res, String address) throws IOException, ServletException {
        getServletContext().getRequestDispatcher(address).forward(req, res);
    }

    public AltAttribute getAltAttribute(HttpServletRequest req) {
        AltAttribute altAttribute = new AltAttribute();
        try {
            altAttribute = (AltAttribute)req.getAttribute(PiServlet.ATTRIBUTE_ALT);
        } catch (Exception e) {
            req.setAttribute(PiServlet.ATTRIBUTE_ALT,altAttribute);
        }
        if (altAttribute == null) {
            altAttribute = new AltAttribute();
            req.setAttribute(PiServlet.ATTRIBUTE_ALT,altAttribute);
        }
        return altAttribute;
    }

    public String getAppProperty(String property) {
    	return getDefines().getProperty(property);
   	}

    public Defines getDefines() {
        if (defines == null) {
            defines = new Defines();
        }
        return defines;
    }

    public Session getSession(HttpServletRequest req, HttpServletResponse res, Connection con) {
        Session session = new Session();
        Store store = this.getStore(req);
        String cookieName = "session_" + store.getAbbreviation();

        session = (Session)req.getAttribute(ATTRIBUTE_SESSION);

        if (session != null && session.getId() > 0) {
            return session;
        }

        //if (session != null) {
        //    System.out.println("SessionId 1:" + session.getId());
        //}

        // if the session is already bound to this request, don't do anything further
        if (session != null && session.getId() > 0) {
            return session;
        }

        // attempt to get the session from the session
        // this is for people who accept session cookies, but not persistant cookies
        //session = (Session)req.getSession().getAttribute(cookieName);

        if (session == null || session.getId() == 0) {
            Cookie cookies[] = req.getCookies();

            // check for the session id and string in the cookie
            if (cookies != null) {
                for (int x=0; x<cookies.length; x++) {
                    Cookie thisCookie = cookies[x];
                    //System.out.println("Cookie:" + thisCookie.getName() + "=" + thisCookie.getValue());

                    if (thisCookie.getName().equalsIgnoreCase(cookieName)) {
                        String[] sessionValue = thisCookie.getValue().split("-");
                        //System.out.println("Length:" + sessionValue.length);
                        if (sessionValue.length == 2) {
                            session = new Session();
                            try {
                                session.setId(Integer.parseInt(sessionValue[0]));
                            } catch (Exception e) {}
                            session.setSessionCode(sessionValue[1]);
                        }
                    }
                }
            }
        }
        //System.out.println("SessionId 3:" + session.getId() + ":" + session.getSessionCode() + ":" + session.getStore());

        // load the store from the database
        try {
            store.loadFromDbByAbbreviation(con);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (session != null) {
            session.setStore(store);
            try {
                session.loadFromDb(con);
                session.getUser().loadFromDb(con);
            } catch (Exception e) {
                e.printStackTrace();
                session.setId(0);
                session.setSessionCode("");
            }
        } else {
            session = new Session();
            session.setStore(store);
        }

        //System.out.println("SessionId 4:" + session.getId());

        try {
            session.saveToDb(con);
        } catch (Exception e) {
            e.printStackTrace();
        }

        req.setAttribute(ATTRIBUTE_SESSION, session);
        req.getSession().setAttribute(cookieName, session);

	    Cookie sessionCookie = new Cookie(cookieName, session.getId() + "-" + session.getSessionCode());
	    sessionCookie.setPath("/");
	    sessionCookie.setMaxAge(Defines.SESSION_COOKIE_AGE);
	    res.addCookie(sessionCookie);

        return session;
    }

    public Store getStore(HttpServletRequest req) {
        Store store = null;
        try {
            store = new Store((String)req.getAttribute(PiServlet.ATTRIBUTE_STORE));
        } catch (Exception e) {
	        store = null;
        }
        if (store == null || store.getAbbreviation().length() == 0) {
            store = new Store(PiServlet.STORE_DEFAULT);
        }
        return store;
    }

    public MessageBean getErrorBean(HttpServletRequest req) {
        MessageBean errorBean = new MessageBean();
        try {
            errorBean = (MessageBean)req.getAttribute(PiServlet.ATTRIBUTE_ERRORBEAN);
        } catch (Exception e) {
            req.setAttribute(PiServlet.ATTRIBUTE_ERRORBEAN,errorBean);
        }
        if (errorBean == null) {
            errorBean = new MessageBean();
            req.setAttribute(PiServlet.ATTRIBUTE_ERRORBEAN,errorBean);
        }
        return errorBean;
    }

    public String getWebRootPath() {
        String filePath = getServletContext().getRealPath("/");
        if (filePath != null) {
            // trim off the ".\"
            if (filePath.substring(filePath.length() - 2, filePath.length()).equals(".\\")) {
                filePath = filePath.substring(0, filePath.length() - 2);
            }
            if (!filePath.substring(filePath.length() - 1, filePath.length()).equals(File.separator)) {
                filePath += File.separator;
            }
        }
        return filePath;
    }

	public MessageBean getMessageBean(HttpServletRequest req) {
        MessageBean messageBean = new MessageBean();
        try {
            messageBean = (MessageBean)req.getAttribute(PiServlet.ATTRIBUTE_MESSAGEBEAN);
        } catch (Exception e) {
            req.setAttribute(PiServlet.ATTRIBUTE_MESSAGEBEAN,messageBean);
        }
        if (messageBean == null) {
            messageBean = new MessageBean();
            req.setAttribute(PiServlet.ATTRIBUTE_MESSAGEBEAN,messageBean);
        }
        return messageBean;
    }


    public static boolean getReqBoolean(HttpServletRequest req, String parameter) {
        return getReqBoolean(req,parameter,false);
    }
    public static boolean getReqBoolean(HttpServletRequest req, String parameter, boolean defaultValue) {
        String parameterValue = (req.getParameter(parameter) == null) ? "" : req.getParameter(parameter);
        boolean result = defaultValue;
        if (parameterValue.equalsIgnoreCase("1") ||
            parameterValue.equalsIgnoreCase("true") ||
            parameterValue.equalsIgnoreCase("yes") ||
            parameterValue.equalsIgnoreCase("on")) {
            result = true;
        }
        return result;
    }
	/***
	 * Returns a string with the request parameter html escaped.
	 * This is used in forms that need to populate fields with
	 * values just submitted via the form.
	 * @param req
	 * @param parameter
	 * @return
	 */
	public static String getReqEsc(HttpServletRequest req, String parameter) {
		String returnValue = getReqString(req,parameter);

		returnValue = returnValue.replaceAll("<", "&lt;");
		returnValue = returnValue.replaceAll(">", "&gt;");
		returnValue = returnValue.replaceAll("\"", "&quot;");
		return returnValue;
	}
    public static int getReqInt(HttpServletRequest req, String parameter) {
        return PiServlet.getReqInt(req,parameter,0);
    }
    public static int getReqInt(HttpServletRequest req, String parameter, int defaultValue) {
        int parameterValue = defaultValue;
        try {
            parameterValue = Integer.parseInt(req.getParameter(parameter));
        } catch (Exception e) {
            parameterValue = defaultValue;
        }
        return parameterValue;
    }
    public static String getReqString(HttpServletRequest req, String parameter) {
        return PiServlet.getReqString(req,parameter,"");
    }
    public static String getReqString(HttpServletRequest req, String parameter, String defaultValue) {
        return (req.getParameter(parameter) == null) ? defaultValue : req.getParameter(parameter).trim();
    }

    public int getAppServerSessionId(HttpServletRequest req) {
        int sessionId = 0;
        try {
            sessionId = ((Integer)req.getSession().getAttribute("sessionId")).intValue();
        } catch (Exception e) {
            sessionId = 0;
        }
        return sessionId;
    }

    public Connection openConnection() {
        return openConnection(defines.getProperty("datasource"));
    }

    public static Connection openConnection(String dataSource) {
        Context ctx = null;
        Context envCtx = null;
        DataSource ds = null;
        Connection con = null;

        try {
            ctx = new InitialContext();
            ds = (DataSource) ctx.lookup(dataSource);
            //System.out.println("DS=" + dataSource);
            //System.out.println("DS=" + (envCtx.lookup(dataSource)).getClass().getPackage() + " - " + (envCtx.lookup(dataSource)).getClass().getName());
            //ds = (DataSource)envCtx.lookup(dataSource);
        } catch (Exception e) {
            e.printStackTrace();
        }


        // open the connection
        try {
            con = ds.getConnection();
        } catch (Exception e) {
            System.err.println("Error Getting Connection : " + e.toString() + "-" + e.getMessage());
            e.printStackTrace();
        }
        return con;
    }

    public static Connection openConnection(HttpServletRequest req) {
        return openConnection(req,"Default");
    }

    public static Connection openConnection(HttpServletRequest req, String dataSource) {
        Context ctx = null;
        Context envCtx = null;
        DataSource ds = null;
        Connection con = null;

        try {
            ctx = new InitialContext();
            envCtx = (Context) ctx.lookup("java:comp/env");
            ds = (DataSource)envCtx.lookup(dataSource);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //MessageBean errorBean = getErrorBean(req);

        // open the connection
        try {
            con = ds.getConnection();
        } catch (Exception e) {
            //errorBean.addMessage("Error Getting Connection : " + e.toString() + "-" + e.getMessage());
            System.out.println("Error Getting Connection : " + e.toString() + "-" + e.getMessage());
        }
        return con;
    }


}
