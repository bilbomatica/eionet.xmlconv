/*
 * Created on 16.04.2008
 */
package eionet.gdem.web.struts.conversion;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import eionet.gdem.exceptions.DCMException;
import eionet.gdem.web.struts.stylesheet.StylesheetListLoader;

/**
 * @author Enriko Käsper, TietoEnator Estonia AS ValidateXMLFormAction
 */

public class ValidateXMLFormAction extends Action {

    /** */
    private static final Log LOGGER = LogFactory.getLog(ValidateXMLFormAction.class);

    /*
     * (non-Javadoc)
     *
     * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm,
     * javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public ActionForward execute(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) {
        ActionErrors errors = new ActionErrors();

        try {
            httpServletRequest.setAttribute(StylesheetListLoader.CONVERSION_SCHEMAS_ATTR, StylesheetListLoader.getConversionSchemasList(httpServletRequest));
        } catch (DCMException e) {
            e.printStackTrace();
            LOGGER.error("Serach CR Conversions error", e);
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(e.getErrorCode()));
            saveMessages(httpServletRequest, errors);
        }
        return actionMapping.findForward("success");
    }
}
