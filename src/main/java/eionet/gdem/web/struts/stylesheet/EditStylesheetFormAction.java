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
 * The Original Code is Web Dashboards Service
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency (EEA).  Portions created by European Dynamics (ED) company are
 * Copyright (C) by European Environment Agency.  All Rights Reserved.
 *
 * Contributors(s):
 *    Original code: Istvan Alfeldi (ED)
 */

package eionet.gdem.web.struts.stylesheet;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import eionet.gdem.dcm.business.SchemaManager;
import eionet.gdem.dcm.business.StylesheetManager;
import eionet.gdem.dto.Schema;
import eionet.gdem.dto.Stylesheet;
import eionet.gdem.exceptions.DCMException;
import eionet.gdem.utils.Utils;

public class EditStylesheetFormAction extends Action {

    /** */
    private static final Log LOGGER = LogFactory.getLog(EditStylesheetFormAction.class);

    @Override
    public ActionForward execute(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) {

        ActionMessages errors = new ActionMessages();

        StylesheetForm form = (StylesheetForm) actionForm;
        String stylesheetId = httpServletRequest.getParameter("stylesheetId");

        if (stylesheetId == null || stylesheetId.equals("")) {
            stylesheetId = (String) httpServletRequest.getAttribute("stylesheetId");
        }

        ConvTypeHolder ctHolder = new ConvTypeHolder();

        try {
            StylesheetManager st = new StylesheetManager();
            Stylesheet stylesheet = st.getStylesheet(stylesheetId);
            form.setDescription(stylesheet.getXsl_descr());
            form.setOutputtype(stylesheet.getType());
            form.setSchema(stylesheet.getSchema());
            form.setStylesheetId(stylesheet.getConvId());
            form.setXsl(stylesheet.getXsl());
            form.setXslContent(stylesheet.getXslContent());
            form.setXslFileName(stylesheet.getXslFileName());
            form.setModified(stylesheet.getModified());
            form.setChecksum(stylesheet.getChecksum());
            // set empty string if dependsOn is null to avoid struts error in define tag:
            // Define tag cannot set a null value
            form.setDependsOn(stylesheet.getDependsOn() == null ? "" : stylesheet.getDependsOn());

            ctHolder = st.getConvTypes();

            httpServletRequest.getSession().setAttribute("stylesheet.outputtypeSel", stylesheet.getType());

            SchemaManager schema = new SchemaManager();
            StylesheetManager styleMan = new StylesheetManager();

            StylesheetListHolder stylesheetList = StylesheetListLoader.getGeneratedList(httpServletRequest);
            List<Schema> schemas = stylesheetList.getDdStylesheets();
            httpServletRequest.setAttribute("stylesheet.DDSchemas", schemas);

            String schemaId = schema.getSchemaId(stylesheet.getSchema());
            if (!Utils.isNullStr(schemaId)) {
                httpServletRequest.setAttribute("schemaInfo", schema.getSchema(schemaId));
                httpServletRequest.setAttribute("existingStylesheets", styleMan.getSchemaStylesheets(schemaId, stylesheetId));
            }
            httpServletRequest.setAttribute(StylesheetListLoader.STYLESHEET_LIST_ATTR, StylesheetListLoader.getStylesheetList(httpServletRequest));

        } catch (DCMException e) {
            e.printStackTrace();
            LOGGER.error("Edit stylesheet error", e);
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(e.getErrorCode()));
            saveErrors(httpServletRequest, errors);
        }
        httpServletRequest.getSession().setAttribute("stylesheet.outputtype", ctHolder);

        return actionMapping.findForward("success");
    }
}