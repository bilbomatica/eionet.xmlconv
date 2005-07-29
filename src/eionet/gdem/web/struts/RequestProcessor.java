package eionet.gdem.web.struts;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.tiles.TilesRequestProcessor;

import eionet.gdem.services.GDEMServices;
import eionet.gdem.services.LoggerIF;



public class RequestProcessor extends TilesRequestProcessor {

	private static LoggerIF _logger=GDEMServices.getLogger();
	
	public RequestProcessor() {

		super();

	}

	/**
	 * Preprocess every action that is called from struts framework
	 * 
	 * @param request
	 *            HttpServletRequest
	 * @param response
	 *            HttpServletResponse
	 * @return boolean
	 */
	public boolean processPreprocess(HttpServletRequest request,
			HttpServletResponse response) {

		request.setAttribute("servletPath", request.getServletPath());
		 
		logReq(request); 
		
        //Remove messages from session and add to page context
        ActionMessages errors = (ActionMessages) request.getSession().getAttribute("dcm.errors");
        if (errors != null) {
            request.getSession().setAttribute("dcm.errors", null);
            if (!errors.isEmpty()) {
					request.setAttribute("dcm.errors", errors); 
			};
        }

        ActionMessages messages = (ActionMessages) request.getSession().getAttribute("dcm.messages");
        if (messages != null) {			
            request.getSession().setAttribute("dcm.messages", null);
            if (!messages.isEmpty()) request.setAttribute("dcm.messages", messages);
        }

		 
		
		return true;
	}

	protected ActionForward processActionPerform(HttpServletRequest request,
			HttpServletResponse response, Action action, ActionForm form,
			ActionMapping mapping) throws IOException, ServletException {

		String path = request.getServletPath();
		_logger.debug("servletPath ----- " +request.getServletPath());

		return super.processActionPerform(request, response, action, form, mapping);
	}
	
	private void logReq(HttpServletRequest request){
		_logger.debug("servletPath ----- " +request.getServletPath());

		Enumeration att= request.getAttributeNames();
		String name;
		
	     for (Enumeration e= request.getAttributeNames(); e.hasMoreElements() ;) {
			 name = e.nextElement().toString();
			 _logger.debug("attribute ----- " +  name + "="+ request.getAttribute(name));
	     }

	     for (Enumeration e= request.getParameterNames(); e.hasMoreElements() ;) {
			 name = e.nextElement().toString();
			 _logger.debug("parameter ----- " +  name + "="+ request.getParameter(name));
	     }

	     for (Enumeration e= request.getSession().getAttributeNames(); e.hasMoreElements() ;) {
			 name = e.nextElement().toString();
			 _logger.debug("session attribute ----- " +  name + "="+request.getSession().getAttribute(name));
	     }		
	}
}