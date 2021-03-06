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

package eionet.gdem.web.struts.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



import org.apache.struts.action.Action;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.validator.DynaValidatorForm;

import eionet.gdem.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LDAP Form action class.
 * @author Unknown
 * @author George Sofianos
 */
public class LdapFormAction extends Action {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(LdapFormAction.class);

    @Override
    public ActionForward execute(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) {
        ActionErrors errors = new ActionErrors();
        DynaValidatorForm form = (DynaValidatorForm) actionForm;
        try {
            form.set("url", Properties.ldapUrl);
            form.set("context", Properties.ldapContext);
            form.set("userDir", Properties.ldapUserDir);
            form.set("attrUid", Properties.ldapAttrUid);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Error getting ldap parameters", e);
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("label.exception.unknown"));
            saveMessages(httpServletRequest, errors);
        }
        saveMessages(httpServletRequest, errors);

        return actionMapping.findForward("success");
    }

}
