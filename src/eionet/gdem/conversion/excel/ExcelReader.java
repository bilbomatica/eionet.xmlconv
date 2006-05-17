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


import eionet.gdem.conversion.DDXMLConverter;
import eionet.gdem.conversion.SourceReaderIF;
import eionet.gdem.utils.Utils;

import java.util.Hashtable;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.hssf.usermodel.*;

import java.util.Vector;
import java.io.InputStream;
import eionet.gdem.GDEMException;

/**
* The main class, which is calling POI HSSF methods for reading Excel file
* @author Enriko K�sper
*/

public class ExcelReader implements SourceReaderIF
{
  private HSSFWorkbook wb=null;
  private static final String SCHEMA_SHEET_NAME = "DO_NOT_DELETE_THIS_SHEET";

  public void initReader(InputStream input) throws GDEMException{
    try{
      POIFSFileSystem fs = new POIFSFileSystem(input);
      wb = new HSSFWorkbook(fs);
    }
    catch(Exception e)
    {
       throw new GDEMException("ErrorConversionHandler - couldn't open Excel file: " + e.toString());
    }
  }
  public String getXMLSchema(){

    if (wb==null) return null;

    HSSFSheet schema_sheet = wb.getSheet(SCHEMA_SHEET_NAME);

    if (schema_sheet == null){
      for (int i=0; i<wb.getNumberOfSheets();i++){
        schema_sheet = wb.getSheetAt(i);
        String schema = findSchemaFromSheet(schema_sheet);
        if (schema!=null)
          return schema;
      }
    }
    else{
      return findSchemaFromSheet(schema_sheet);
    }
    return null;
  }
  public String getFirstSheetName(){

  	if (wb==null) return null;

    for (int i=0; i<wb.getNumberOfSheets();i++){
    	String sheet_name = wb.getSheetName(i);
    	if (sheet_name.equalsIgnoreCase(SCHEMA_SHEET_NAME))
			continue;
    	return sheet_name;
    }
    return null;

  }
  public Hashtable getSheetSchemas(){

      if (wb==null) return null;

      HSSFSheet schema_sheet = wb.getSheet(SCHEMA_SHEET_NAME);

      if (schema_sheet == null){
        for (int i=0; i<wb.getNumberOfSheets();i++){
          schema_sheet = wb.getSheetAt(i);
          Hashtable schemas = findSheetSchemas(schema_sheet);
          if (schemas!=null){
          	if (!schemas.isEmpty())
            return schemas;
          }
        }
      }
      else{
        return findSheetSchemas(schema_sheet);
      }
      return null;
    }
  public void writeContentToInstance(DD_XMLInstance instance)throws GDEMException{
    Vector tables = instance.getTables();
    if (tables == null)
      throw new GDEMException("could not find tables from DD instance file");
    if (wb==null) return;

    for (int i=0;i<tables.size();i++){
      Hashtable table = (Hashtable)tables.get(i);
      String tbl_localname = (String)table.get("localName");
      String tbl_name = (String)table.get("name");
      String tbl_attrs = (String)table.get("attributes");

      HSSFSheet sheet = getSheet(tbl_localname);
      HSSFSheet meta_sheet = getMetaSheet(tbl_localname);

      if (sheet == null) continue;
      int first_row = sheet.getFirstRowNum();
      int last_row = sheet.getLastRowNum();
      HSSFRow row = sheet.getRow(first_row);
      HSSFRow meta_row = null;
      Vector elements = instance.getTblElements(tbl_name);

      setColumnMappings(row, elements, true);

      if (meta_sheet!=null){
        int meta_first_row = meta_sheet.getFirstRowNum();
        int meta_last_row = meta_sheet.getLastRowNum();
        meta_row = meta_sheet.getRow(first_row);
        setColumnMappings(meta_row, elements, false);
      }

      instance.writeTableStart(tbl_name, tbl_attrs);
      instance.setCurRow(tbl_name);

//read data
      // there are no data rows in the Excel file. We create empty table
      first_row = (first_row == last_row) ? last_row : first_row+1;

      for (int j=first_row;j<=last_row;j++){
        row = (first_row==0) ? null:sheet.getRow(j);
        meta_row = (meta_sheet!=null && first_row!=0) ? meta_sheet.getRow(j):null;
        //don't convert empty rows.
        if (isEmptyRow(row)) continue;

        instance.writeRowStart();
        for (int k=0;k<elements.size();k++){
          Hashtable elem = (Hashtable)elements.get(k);
          String elem_name = (String)elem.get("name");
          String elem_attributes = (String)elem.get("attributes");
          Integer col_idx = (Integer)elem.get("col_idx");
          Boolean main_table = (Boolean)elem.get("main_table");

          String data = "";
          if (col_idx!=null && main_table!=null){
            data = (main_table.booleanValue()) ?
                getCellValue(row,col_idx) : getCellValue(meta_row,col_idx);
          }
          instance.writeElement(elem_name,elem_attributes, data);
        }
        instance.writeRowEnd();
      }
      instance.writeTableEnd(tbl_name);
    }

  }
  public boolean isEmptyRow(HSSFRow row){
  	if (row==null) return true;

  	for (int j=0; j<=row.getLastCellNum();j++){
  		HSSFCell cell = row.getCell((short)j);
  		if (cell==null) continue;
  		if (!Utils.isNullStr(cellValueToString(cell)))
  			return false;
  	}
  	return true;
  }
  public boolean isEmptySheet(String sheet_name){

  	HSSFSheet sheet = getSheet(sheet_name);
  	int row_count =sheet.getLastRowNum();
  	if (row_count<1) return true;

  	//check if the first row has any data
  	for (int i=1; i<=row_count;i++){
  		HSSFRow row = sheet.getRow(i);
  		if (isEmptyRow(row))
  				continue;
  		else{
  			return false;
  		}
  	}

  	return true;
  }
  /*
   * method goes through 4 rows and search the best fit of XML Schema.
   * The deault row is 4.
   */
  private String findSchemaFromSheet(HSSFSheet schema_sheet){
    String xml_schema = null;

    HSSFRow schema_row = null;
    HSSFCell schema_cell = null;
    for (int i=3; i>-1; i--){
      if (schema_sheet.getLastRowNum()<i) continue;
      schema_row = schema_sheet.getRow(i);
      if (schema_row==null) continue;
      if (schema_row.getLastCellNum()<0) continue;
      schema_cell = schema_row.getCell((short)0);
      String val = schema_cell.getStringCellValue();

      if (val.startsWith("http://") &&
            val.toLowerCase().indexOf("/getschema")>0 &&
            Utils.isURL(val)){
        return val;
      }
    }
    return null;
  }
  /*
   * method goes through rows after XML Schema and finds schemas for Excel sheets (DataDict tables).
   *  cell(0) =sheet name; cell(1)= XML schema
   */
  private Hashtable findSheetSchemas(HSSFSheet schema_sheet){

  	String xml_schema = null;

    HSSFRow schema_row = null;
    HSSFCell schema_cell = null;
    HSSFCell sheet_cell = null;

    Hashtable result = new Hashtable();
    if (schema_sheet.getLastRowNum()<1)return null;

    for (int i=0; i<=schema_sheet.getLastRowNum(); i++){
      schema_row = schema_sheet.getRow(i);
      if (schema_row==null) continue;
      if (schema_row.getLastCellNum()<1) continue;
      schema_cell = schema_row.getCell((short)1);
      if(schema_cell==null)continue;
      String schema_val = schema_cell.getStringCellValue();

      if (schema_val.startsWith("http://") &&
      		schema_val.toLowerCase().indexOf("/getschema")>0 &&
            Utils.isURL(schema_val)){

      		sheet_cell = schema_row.getCell((short)0);
      		String sheet_val = sheet_cell.getStringCellValue();
      		if (sheet_val==null)continue;
      	    HSSFSheet sheet = wb.getSheet(sheet_val);
      	    if (sheet!=null && !result.containsKey(sheet_val))
      	    	result.put(sheet_val,schema_val);
      }
    }
    return result;
  }
    private HSSFSheet getSheet(String name){
      HSSFSheet sheet = wb.getSheet(name);

      if (sheet == null){
        for (int i=0; i<wb.getNumberOfSheets();i++){
          String sheet_name = wb.getSheetName(i);
          if (sheet_name.equalsIgnoreCase(name))
            return wb.getSheet(sheet_name);
      }

      }
      else{
        return sheet;
      }

      return null;
  }
  private String cellValueToString(HSSFCell cell){
    String   value = "";
    switch (cell.getCellType()){
      case HSSFCell.CELL_TYPE_FORMULA :
        break;
      case HSSFCell.CELL_TYPE_NUMERIC :
      	value = POINumericToString(cell.getNumericCellValue());
        break;
      case HSSFCell.CELL_TYPE_STRING :
        value = cell.getStringCellValue();
        break;
      case HSSFCell.CELL_TYPE_BOOLEAN :
        value = Boolean.toString(cell.getBooleanCellValue());
        break;
      case HSSFCell.CELL_TYPE_ERROR :
        break;
      }

    return value.trim();
  }
  /*
   * DD can generate additional "-meta" sheets with GIS elements for one DD table.
   * In XML these should be handled as 1 table.
   * This is method for finding these kind of sheets and parsing these in parallel with the main sheet
   */
  private HSSFSheet getMetaSheet(String main_sheet_name){
    return getSheet(main_sheet_name + DDXMLConverter.META_SHEET_NAME);
  }
  private void setColumnMappings(HSSFRow row, Vector elements, boolean main_table){
 //read column header
      int first_cell = row.getFirstCellNum();
      int last_cell = row.getLastCellNum();

      for (int j=0;j<elements.size();j++){
        Hashtable elem = (Hashtable)elements.get(j);
        String elem_localName = (String)elem.get("localName");
        for (int k=first_cell;k<last_cell;k++){
          HSSFCell cell = row.getCell((short)k);
          String col_name = cell.getStringCellValue();
          if (col_name.equalsIgnoreCase(elem_localName)){
              elem.put("col_idx", new Integer(k));
              elem.put("main_table", new Boolean(main_table));
              break;
          }
        }
      }

  }
  private String getCellValue(HSSFRow row, Integer col_idx){

    HSSFCell cell = (col_idx==null || row==null) ? null : row.getCell(col_idx.shortValue());
    String data = (cell==null) ? "" : cellValueToString(cell);
    return data;
  }
  /*
  *      poi is returning all the numeric values as double and ending with .0
  *      we want hav also integer values
  *      small hack to change 2.0 value to 2
  */
  private String POINumericToString(double d_val){
  	Double d = new Double(d_val);

  	int int_val = (int)d_val;

    if (d_val - int_val>0 || d_val - int_val<0){
			return  Double.toString(d_val);
    }
    else{
      return  Integer.toString(int_val);
    }
  }
}
