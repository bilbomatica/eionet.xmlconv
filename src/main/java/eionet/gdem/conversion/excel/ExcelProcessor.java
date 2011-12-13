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
 * Original Code: Enriko Käsper (TietoEnator)
 */

package eionet.gdem.conversion.excel;

import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.IOUtils;
import org.xml.sax.XMLReader;

import eionet.gdem.GDEMException;
import eionet.gdem.conversion.excel.writer.ExcelConversionHandlerIF;
import eionet.gdem.conversion.excel.writer.ExcelXMLHandler;


/**
 * This class is creating handlers for creating Excel file from xml
 * called from ConversionService
 * @author Enriko Käsper
 */

public class ExcelProcessor  {
    public ExcelProcessor() {
    }
    public void makeExcel(String sIn, String sOut) throws GDEMException {
        FileOutputStream outStream = null;
        try
        {
            outStream = new FileOutputStream(sOut);
            makeExcel(sIn, outStream);
        }
        catch(Exception e)
        {
            throw new GDEMException("ErrorConversionHandler - couldn't save the Excel file: " + e.toString(), e);
        }
        finally{
            IOUtils.closeQuietly(outStream);
        }
    }
    public void makeExcel(String sIn, OutputStream sOut) throws GDEMException {

        if (sIn == null) {
            return;
        }
        if (sOut == null) {
            return;
        }

        try{
            ExcelConversionHandlerIF excel = ExcelUtils.getExcelConversionHandler();
            //excel.setFileName(sOut);

            ExcelXMLHandler handler=new ExcelXMLHandler(excel);
            SAXParserFactory spfact = SAXParserFactory.newInstance();
            SAXParser parser = spfact.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            spfact.setValidating(true);

            reader.setContentHandler(handler);
            reader.parse(sIn);
            excel.writeToFile(sOut);
        }
        catch (Exception e){
            throw new GDEMException("Error generating Excel file: " + e.toString(), e);
        }

        return;
    }
    public static void main(String[] args){
        String srcFile = "C:/Projects/EEA/webapps/xmlconv/ROOT/tmp/gdem_out1314612515459.xml";
        String excelFile = "C:/Projects/EEA/webapps/xmlconv/ROOT/tmp/gdem_out1314612515459.xls";
        //String srcFile = "F:\\Projects\\gdem\\test\\content2.xml";
        //String srcFile = "http://reportek2.eionet.eu.int/AAAcolqv1nta/envqwkktq/EE_bodies.xml";
        try{
            ExcelProcessor processor = new ExcelProcessor();
            processor.makeExcel(srcFile, excelFile);
        }
        catch(Exception e){
            System.out.println(e.toString());
        }
    }
}