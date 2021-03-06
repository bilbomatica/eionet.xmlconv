/**
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is "EINRC-7 / GDEM project".
 *
 * The Initial Developer of the Original Code is TietoEnator.
 * The Original Code code was developed for the European
 * Environment Agency (EEA) under the IDA/EINRC framework contract.
 *
 * Copyright (C) 2000-2004 by European Environment Agency.  All
 * Rights Reserved.
 *
 * Original Code: Kaido Laine (TietoEnator)
 */

package eionet.gdem.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

import eionet.acl.AccessControlListIF;
import eionet.acl.AccessController;
import eionet.acl.AppUser;

import edu.yale.its.tp.cas.client.filter.CASFilter;
import eionet.gdem.GDEMException;
import eionet.gdem.web.struts.login.AfterCASLoginAction;

/**
 * This is a class containing some utility methods for keeping security.
 *
 * @author Enriko Käsper
 * @author George Sofianos
 */
public final class SecurityUtil {

    /**
     * Private constructor
     */
    private SecurityUtil() {
        // do nothing
    }
    /**
     * Returns current user, or 'null', if the current session does not have user attached to it.
     *
     * @param request Request
     * @param attrName Attribute name
     */
    public static AppUser getUser(HttpServletRequest request, String attrName) {

        HttpSession session = request.getSession();
        AppUser user = session == null ? null : (AppUser) session.getAttribute(attrName);

        if (user == null) {
            String casUserName = (String) session.getAttribute(CASFilter.CAS_FILTER_USER);
            if (casUserName != null) {
                user = new CASUser(casUserName);
                session.setAttribute(attrName, user);
                session.setAttribute("user", user.getUserName());
            }
        } else if (user instanceof CASUser) {
            String casUserName = (String) session.getAttribute(CASFilter.CAS_FILTER_USER);
            if (casUserName == null) {
                user = null;
                session.removeAttribute(attrName);
                session.removeAttribute("user");
            } else if (!casUserName.equals(user.getUserName())) {
                user = new CASUser(casUserName);
                session.setAttribute(attrName, user);
                session.setAttribute("user", user.getUserName());
            }
        }

        if (user != null)
            return user;
        else
            return null;
    }

    /**
     * Checks if user has permissions
     * @param usr User
     * @param aclPath ACL Path
     * @param prm Permissions
     * @return True if user has permissions.
     * @throws Exception If an error occurs.
     */
    public static boolean hasPerm(String usr, String aclPath, String prm) throws Exception {
        if (!aclPath.startsWith("/"))
            return false;

        boolean has = false;
        AccessControlListIF acl = null;
        int i = aclPath.indexOf("/", 1);
        while (i != -1 && !has) {
            String subPath = aclPath.substring(0, i);
            try {
                acl = AccessController.getAcl(subPath);
            } catch (Exception e) {
                acl = null;
            }

            if (acl != null)
                has = acl.checkPermission(usr, prm);

            i = aclPath.indexOf("/", i + 1);
        }

        if (!has) {
            try {
                acl = AccessController.getAcl(aclPath);
            } catch (Exception e) {
                acl = null;
            }

            if (acl != null)
                has = acl.checkPermission(usr, prm);
        }

        return has;
    }

    /**
     * Returns login URL
     * @param request Request
     * @return login URL
     * @throws GDEMException If an error occurs.
     */
    public static String getLoginURL(HttpServletRequest request) throws GDEMException {

        String urlWithContextPath = getUrlWithContextPath(request);
        String result = "login";

        String afterLoginUrl = getRealRequestURL(request);
        // store the current page in the session to be able to come back after login
        if (afterLoginUrl != null && !afterLoginUrl.contains("login"))
            request.getSession().setAttribute(AfterCASLoginAction.AFTER_LOGIN_ATTR_NAME, afterLoginUrl);

        String casLoginUrl = request.getSession().getServletContext().getInitParameter(CASFilter.LOGIN_INIT_PARAM);
        if (casLoginUrl != null) {

            StringBuffer loginUrl = new StringBuffer(casLoginUrl);
            loginUrl.append("?service=");
            try {
                // + request.getScheme() + "://" + SERVER_NAME + request.getContextPath() + "/login";
                loginUrl.append(URLEncoder.encode(urlWithContextPath + "/do/afterLogin", "UTF-8"));
                result = loginUrl.toString();
            } catch (UnsupportedEncodingException e) {
                throw new GDEMException(e.toString(), e);
            }
        } else {
            // got to local login page
            result = urlWithContextPath + "/do/login";
        }

        return result;
    }

    /**
     * Returns logout URL
     * @param request Request
     * @return Logoug URL
     * @throws GDEMException If an error occurs.
     */
    public static String getLogoutURL(HttpServletRequest request) throws GDEMException {

        String result = "start";

        String casLoginUrl = request.getSession().getServletContext().getInitParameter(CASFilter.LOGIN_INIT_PARAM);
        if (casLoginUrl != null) {

            StringBuffer buf = new StringBuffer(casLoginUrl.replaceFirst("/login", "/logout"));
            try {
                buf.append("?url=").append(URLEncoder.encode(getUrlWithContextPath(request), "UTF-8"));
                result = buf.toString();
            } catch (UnsupportedEncodingException e) {
                throw new GDEMException(e.toString(), e);
            }
        }
        // goto start page
        return result;
    }

    /**
     * Returns URL with context path
     * @param request Request
     * @return URL with context path
     */
    public static String getUrlWithContextPath(HttpServletRequest request) {

        if (request == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();

        StringBuffer url = new StringBuffer(scheme);
        url.append("://").append(serverName);

        // if not http:80 or https:443, then add the port number
        if (!(scheme.equalsIgnoreCase("http") && serverPort == 80) && !(scheme.equalsIgnoreCase("https") && serverPort == 443)
                && serverPort > 0) {

            url.append(":");
            url.append(String.valueOf(serverPort));
        }

        url.append(request.getContextPath());
        return url.toString();
    }

    /**
     * Returns Real request URL
     * @param request Request
     * @return Real request URL
     */
    public static String getRealRequestURL(HttpServletRequest request) {

        HttpServletRequest tmpRequest = request;
        while (tmpRequest instanceof HttpServletRequestWrapper) {
            tmpRequest = (HttpServletRequest) ((HttpServletRequestWrapper) tmpRequest).getRequest();
        }
        StringBuffer url = tmpRequest.getRequestURL();

        if (tmpRequest.getQueryString() != null)
            url.append("?").append(tmpRequest.getQueryString());

        return url.toString();
    }
}

/**
 * CAS User class.
 * @author Unknown
 */
class CASUser extends AppUser {

    /**
     * Constructor
     * @param userName Username
     */
    public CASUser(String userName) {
        this.authenticatedUserName = userName;
    }

    private String authenticatedUserName;

    public String getUserName() {
        return authenticatedUserName;
    }
}
