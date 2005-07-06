package eionet.gdem.web.struts.stylesheet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import eionet.gdem.dcm.business.SchemaManager;
import eionet.gdem.dcm.business.StylesheetManager;
import eionet.gdem.exceptions.DCMException;
import eionet.gdem.services.GDEMServices;
import eionet.gdem.services.LoggerIF;

public class ConvTypeAction  extends Action{

	private static LoggerIF _logger=GDEMServices.getLogger();
	
	   public ActionForward execute(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

			ConvTypeHolder ctHolder = new ConvTypeHolder();
			ActionErrors errors = new ActionErrors();

			System.out.println("-------------ConvTypeAction-  start--------------");
			
			try{
				StylesheetManager sm = new StylesheetManager();
				ctHolder =sm.getConvTypes();
				
			}catch(DCMException e){			
				System.out.println(e.toString());
				_logger.debug(e.toString());
				errors.add("schema", new ActionError(e.getErrorCode()));
			}
	        saveErrors(httpServletRequest, errors);
			
	        httpServletRequest.getSession().setAttribute("stylesheet.outputtype", ctHolder);
			System.out.println("-------------ConvTypeAction---------------");
			
			//StylesheetForm form = (StylesheetForm)actionForm;
			//form.setSchema((String)httpServletRequest.getParameter("schema"));
			
			//httpServletRequest.setAttribute(actionMapping.getAttribute(),form);
			
	        return actionMapping.findForward("success");
			
			
	    }


}
