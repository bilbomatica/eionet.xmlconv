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
 *    Original code: Nedeljko Pavlovic (ED)
 */

package eionet.gdem.web.listeners;

import java.io.File;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;


import eionet.gdem.Properties;


public class AppServletContextListener implements ServletContextListener {


	/**
	 * Public constuctor
	 */
	public AppServletContextListener() {
	}


	/**
	 * Method that is triggered once on start of application (context
	 * initialization):
	 * 
	 */
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		System.out.println("Application started !");
		try {
			String pathPrefix = servletContextEvent.getServletContext().getRealPath("/");
			checkHomeDirectories(pathPrefix);
			Properties.metaXSLFolder=servletContextEvent.getServletContext().getRealPath("/dcm");
			Properties.convFile=servletContextEvent.getServletContext().getRealPath("/dcm/conversions.xml");
			Properties.xslFolder=servletContextEvent.getServletContext().getRealPath("/xsl");
			Properties.schemaFolder=servletContextEvent.getServletContext().getRealPath("/schema");
			Properties.tmpFolder=servletContextEvent.getServletContext().getRealPath("/tmp");
			Properties.uiFolder=servletContextEvent.getServletContext().getRealPath("/uixsl");
			Properties.appHome=servletContextEvent.getServletContext().getRealPath("/WEB-INF/classes");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	
	
	/**
	 * Checks persistence of all home directories needed for correct WDS work.
	 * Home directory must be present. Rest directories will be created in case
	 * that they don't exist.
	 */
	private void checkHomeDirectories(String pathPrefix) throws Exception {
			File tmp = new File(pathPrefix + File.separatorChar + "tmp");
			File uixsl = new File(pathPrefix + File.separatorChar + "uixsl");
			File schema = new File(pathPrefix + File.separatorChar + "schema");
			File xsl = new File(pathPrefix + File.separatorChar + "xsl");
			File[] dcmDirs={tmp, uixsl, schema, xsl};
			
			for (int i = 0; i < dcmDirs.length; i++) {
				if (!dcmDirs[i].exists()) {
					if (!dcmDirs[i].mkdir()) {
						System.out.println("ERROR !!!! While creating directory "+dcmDirs[i].getName());
					}
				}			
			}
	}



	/**
	 * Method that is triggered once on destroy of servlet context
	 * 
	 */
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
		System.out.println("Application terminated !");
	}



}