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

package eionet.gdem.qa;
import java.io.OutputStream;

import eionet.gdem.GDEMException;


/**
* Class for XQ script 
* used by the workqueue XQTask and XQ sandbox
*/
public class XQScript {
  private String _script; //XQuery script
  private String[] _xqParams; //parameter name + value pairs
  private String strResultFile;
  private String _outputType;

  //XQ Engine instance
  private static XQEngineIF _engine;


  /**
   * @param xqScript
   * @param params XQ parameter name + value pairs in an array
   * in format {name1=value1, name2=value2, ... , nameN=valueN}
   * if no parameters, null should be passed
   */
  public XQScript(String xqScript, String[] params)  {
	  this(xqScript,params,XQEngineIF.DEFAULT_OUTPUTTYPE);
  }
  public XQScript(String xqScript, String[] params, String outputType)  {
	  _script = xqScript;  
	  _xqParams=params;
	  _outputType=outputType;	  
  }

  private void initEngine() throws GDEMException {
    //the engine implementator class specified in the props file
    //at the moment (0404) only Saxon implementation supported
    if (_engine==null)
     _engine = XQueryService.getEngine();
  }
  /**
  * Result of the XQsrcipt
  */
  public String getResult() throws GDEMException {
    initEngine();
    _engine.setOutputType(_outputType);
    return _engine.getResult(_script, _xqParams);
  }
  public void getResult(OutputStream out) throws GDEMException {
    initEngine();
    _engine.setOutputType(_outputType);
    _engine.getResult(_script, _xqParams, out);
  }
  public void setResulFile(String fileName){
  	strResultFile = fileName;
  }
 
}