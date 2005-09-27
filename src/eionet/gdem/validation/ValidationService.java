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
 * Original Code: Enriko K�sper (TietoEnator)
 */

package eionet.gdem.validation;

//import javax.xml.parsers.SAXParser;
//import org.apache.xerces.parsers.SAXParser;
import eionet.gdem.conversion.ssr.InputAnalyser;
import eionet.gdem.conversion.ssr.Names;
import eionet.gdem.dto.Stylesheet;
import eionet.gdem.dto.ValidateDto;
import eionet.gdem.utils.Utils;
import org.xml.sax.*;
import javax.xml.parsers.*;

import org.w3c.dom.Document;
import org.w3c.dom.*;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;

import eionet.gdem.utils.InputFile;

import java.io.IOException;
import java.io.InputStream;

//import eionet.gdem.utils.Utils;
import eionet.gdem.GDEMException;


public class ValidationService {
  private StringBuffer errors;
  private StringBuffer htmlErrors;
  private String uriXml;
  private ArrayList errorsList;
  private ErrorHandler errHandler;
  
  public ValidationService()  {
    errors=new StringBuffer()  ;
    htmlErrors=new StringBuffer()  ;
    errHandler = new GErrorHandler(errors, htmlErrors);
  }

  public ValidationService(boolean list)  {
	  
	    errorsList = new ArrayList(); 
	    errHandler = new ValidatorErrorHandler(errorsList);
	  }
  
  public String validateSchema (String srcUrl, String schema) throws GDEMException {
    InputFile src=null;
    InputStream src_stream = null;
	uriXml= srcUrl;	
    try{
      src = new InputFile(srcUrl);
      src_stream = src.getSrcInputStream();
      return validateSchema(src_stream, schema);
    } catch (MalformedURLException mfe ) {
      throw new GDEMException("Bad URL : " + mfe.toString());
    } catch (IOException ioe ) {
      throw new GDEMException("Error opening URL " + ioe.toString());
    }
    finally{
      if (src_stream!=null){
        try{
          src_stream.close();
        }catch(Exception e){};
      }
    }
    
  }
  public String validateSchema (InputStream src_stream, String schema) throws GDEMException {
	String newSchema = null;

    try {
        
      //URL url = new URL(srcUrl);

      //SAXParser parser = new SAXParser();
      //parser.setErrorHandler(errHandler);
      SAXParserFactory spfact = SAXParserFactory.newInstance();
      SAXParser parser = spfact.newSAXParser();
      XMLReader reader = parser.getXMLReader();

      reader.setErrorHandler(errHandler);
      //make parser to validate
      reader.setFeature("http://xml.org/sax/features/validation", true); 
      reader.setFeature("http://apache.org/xml/features/validation/schema", true);
      reader.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);
      
      reader.setFeature("http://xml.org/sax/features/namespaces", true);
      reader.setFeature("http://xml.org/sax/features/namespace-prefixes",true);
      	 
		
	
//      parser.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);
      if (schema != null){
      //  reader.setProperty("http://apache.org/xml/properties/schema/external-schemaLocation", schema);      
          reader.setProperty("http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation", schema);      
      }
      
      InputSource is = new InputSource( src_stream);

/////////////////////////////////////////

	  DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	  //Document doc = builder.parse( src_stream);
	  Document doc = builder.parse( uriXml);
	  
	  			  
	  Element root = doc.getDocumentElement();
	  String rootName = root.getTagName();
	          
	  

	          
	  String namespace=null;
	  
	  if(rootName.indexOf(":")>0){
		  String attName1 = "xmlns:" + rootName.substring(0, rootName.indexOf(":")) ;
		  namespace = root.getAttribute(attName1);    
	          
	  }
	  
///////////////////////////////////////////
      	 
		
	
//      parser.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);
      if (schema != null){
		newSchema = namespace + " " +schema  ;
		
		
		System.out.println("newSchema="+newSchema);
        reader.setProperty("http://apache.org/xml/properties/schema/external-schemaLocation", newSchema);      
//      reader.setProperty("http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation", schema)     
      }
	  
      //InputSource is = new InputSource( url.openStream());
      reader.parse(is);

      //log("OK");      
  
    } catch ( SAXParseException se ) {
      //ignore
    } catch (Exception e ) {
      Exception se = e;
      if (e instanceof SAXException) {
          se = ((SAXException)e).getException();
      }
      if (se != null)
        se.printStackTrace(System.err);
      else
        e.printStackTrace(System.err);    
      throw new GDEMException("Error parsing: " + e.toString());
    }

    //we have errors!
    if (errors!= null && errors.length()>0){
      //return errors.toString();
      htmlErrors.append("</table></html>");
      return htmlErrors.toString();
    }
    else
      return "OK - XML Schema validation passed without errors.";
  }


  public String validate (String srcUrl) throws GDEMException {
    return validateSchema(srcUrl, null);
  }
  
  public ArrayList getErrorList(){
	  return errorsList;
  }

  /*private void log(String s ) {
    Utils.log(s);
  } */

  public void printList(){
	  for (int j=0; j<errorsList.size(); j++){
		  	ValidateDto val = (ValidateDto)errorsList.get(j);
		}	  
  }
  
  
  public static void main(String[] s) {

try {
    //String xml = "http://reportek2.eionet.eu.int/colqaj8nw/envqe8zva/countrynames.tmx";
    String xml = "http://localhost:8080/gdem/test/MT_bodies.xml";
    //String sch = "http://dd.eionet.eu.int/GetSchema?comp_id=1752&comp_type=TBL";
    // String sch = "http://www.lisa.org/tmx/tmx14.dtd";
    //String sch = "http://roddev.eionet.eu.int/waterdemo/water_measurements.xsd";
    
    ValidationService v = new ValidationService(true);
	
    String result = v.validate("http://reporter.ceetel.net:18180/nl/eea/ewn3/envqyyafg/BG_bodies_Rubi.xml");
  //System.out.println(v.validateSchema(xml,sch));
  //System.out.println(result);
    //v.log(v.validate(xml));
    
} catch (Exception e) {
	e.printStackTrace();
System.out.println("===== " + e.toString());
}

  }
}