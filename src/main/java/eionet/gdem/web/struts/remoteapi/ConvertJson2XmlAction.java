/*
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
 * The Original Code is XMLCONV - Conversion and QA Service
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Enriko Käsper
 */

package eionet.gdem.web.struts.remoteapi;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import eionet.gdem.GDEMException;
import eionet.gdem.qa.functions.Json;
import eionet.gdem.utils.Utils;

/**
 * Struts action class for converting json URL or content to XML format.
 *
 * @author Enriko Käsper
 */
public class ConvertJson2XmlAction extends Action {
    /** */
    private static final Log LOGGER = LogFactory.getLog(ConvertJson2XmlAction.class);

    /** parameter name for passing json content or URL */
    protected static final String JSON_PARAM_NAME = "json";

    /**
     * The purpose of this action is to execute <code>Json</code> static methods to convert json string or URL to XML format.
     * The method expects either url or json parameters.
     */
    @Override
    public ActionForward execute(ActionMapping map, ActionForm actionForm, HttpServletRequest request,
            HttpServletResponse httpServletResponse) throws ServletException {

        String jsonParam = null;

        Map params = request.getParameterMap();
        try {
            // parse request parameters
            if (params.containsKey(JSON_PARAM_NAME)) {
                jsonParam = (String) ((Object[]) params.get(JSON_PARAM_NAME))[0];
            }
            if (Utils.isNullStr(jsonParam)) {
                throw new GDEMException("Missing request parameter: " + JSON_PARAM_NAME);
            }
            String xml = "";
            if (jsonParam.startsWith("http:")) {
                xml = Json.jsonRequest2xmlString(jsonParam);
            } else {
                xml = Json.jsonString2xml(jsonParam);
            }
            // set response properties
            httpServletResponse.setContentType("text/xml");
            httpServletResponse.setCharacterEncoding("UTF-8");
            httpServletResponse.setContentLength(xml.length());

            // write data into response
            httpServletResponse.getOutputStream().write(xml.getBytes());
        } catch (GDEMException ge) {
            ge.printStackTrace();
            LOGGER.error("Unable to convert JSON to XML. " + ge.toString());
            throw new ServletException(ge);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Unable to convert JSON to XML. ");
            throw new ServletException(e);
        } finally {
            try {
                httpServletResponse.getOutputStream().close();
                httpServletResponse.getOutputStream().flush();
            } catch (IOException ioe) {
                ioe.printStackTrace();
                throw new ServletException(ioe);
            }
        }

        // Do nothing, then response is already sent.
        return null;
    }
}