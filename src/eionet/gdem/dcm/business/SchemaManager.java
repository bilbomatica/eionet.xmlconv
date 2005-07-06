package eionet.gdem.dcm.business;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import com.tee.uit.security.AppUser;

import eionet.gdem.Properties;
import eionet.gdem.conversion.ConversionService;
import eionet.gdem.conversion.ssr.Names;
import eionet.gdem.dcm.BusinessConstants;
import eionet.gdem.dcm.xsl.Conversion;
import eionet.gdem.dcm.xsl.ConversionDto;
import eionet.gdem.dto.RootElem;
import eionet.gdem.dto.Schema;
import eionet.gdem.dto.Stylesheet;
import eionet.gdem.exceptions.AuthorizationException;
import eionet.gdem.exceptions.DCMException;
import eionet.gdem.services.DbModuleIF;
import eionet.gdem.services.GDEMServices;
import eionet.gdem.services.LoggerIF;
import eionet.gdem.utils.SecurityUtil;
import eionet.gdem.utils.Utils;
import eionet.gdem.web.struts.schema.SchemaElemHolder;
import eionet.gdem.web.struts.stylesheet.StylesheetListHolder;

public class SchemaManager {

	private static LoggerIF _logger=GDEMServices.getLogger();
	
	public void delete(String user, String schemaId) throws DCMException{
	
    boolean hasOtherStuff = false;
    
     //AppUser user = SecurityUtil.getUser(req, Names.USER_ATT);
  	 //String user_name=null;
	 //  if (user!=null)
      //  user_name = user.getUserName();

        try{
          if (!SecurityUtil.hasPerm(user, "/" + Names.ACL_SCHEMA_PATH, "d")){
			  _logger.debug("You don't have permissions to delete schemas!");
			  throw new DCMException(BusinessConstants.EXCEPTION_AUTORIZATION_SCHEMA_DELETE);                              
          }
        }
        catch (DCMException e){					    
			throw e;
        }
		catch (Exception e){			
			_logger.debug(e.toString());
			throw new DCMException(BusinessConstants.EXCEPTION_GENERAL);          
        }
		
       //StringBuffer err_buf = new StringBuffer();
       //String del_id= (String)req.getParameter(Names.XSD_DEL_ID);

       try{
		   DbModuleIF dbM= GDEMServices.getDbModule();

            Vector stylesheets = dbM.getSchemaStylesheets(schemaId);
            if (stylesheets!=null){
         		 for (int i=0; i<stylesheets.size(); i++){
           			HashMap hash = (HashMap)stylesheets.get(i);
             		String xslFile = (String)hash.get("xsl");
   
                 String xslFolder=Properties.xslFolder;
                 if (!xslFolder.endsWith(File.separator))
                    xslFolder = xslFolder + File.separator;
            
                 try{
                   Utils.deleteFile(xslFolder + xslFile);
                 }
                 catch (Exception e){
						_logger.debug(e.toString());
						throw new DCMException(BusinessConstants.EXCEPTION_GENERAL);          
                 }
              }        
           	}
            if(dbM.getSchemaQueries(schemaId) != null)
               hasOtherStuff = true;
         
         dbM.removeSchema( schemaId, true, false, !hasOtherStuff);              
        }
        catch (Exception e){
			_logger.debug(e.toString());
			throw new DCMException(BusinessConstants.EXCEPTION_GENERAL);          
        }
        
        
      }
	
	public StylesheetListHolder getSchemas(String user_name) throws DCMException{
		
		StylesheetListHolder st = new StylesheetListHolder();

		boolean ssiPrm = false;
		boolean ssdPrm = false;
		Vector hcSchemas;
		ArrayList schemas;
		
		
		try {
			schemas = new ArrayList();
			ssiPrm = SecurityUtil.hasPerm(user_name, "/" + Names.ACL_STYLESHEETS_PATH, "i");
			ssdPrm = SecurityUtil.hasPerm(user_name, "/" + Names.ACL_STYLESHEETS_PATH, "d");

			st.setSsdPrm(ssdPrm);
			st.setSsiPrm(ssiPrm);

			DbModuleIF dbM= GDEMServices.getDbModule();
			hcSchemas = dbM.getSchemas(null);
			if (hcSchemas==null) hcSchemas=new Vector();
			
			for (int i=0; i<hcSchemas.size(); i++){
				HashMap schema = (HashMap)hcSchemas.get(i);
				Schema sc = new Schema();
				sc.setId((String)schema.get("schema_id"));
				sc.setSchema((String)schema.get("xml_schema"));
				sc.setDescription((String)schema.get("description"));
				
				Vector stylesheets =new Vector();
				if (schema.containsKey("stylesheets")){
					stylesheets = (Vector)schema.get("stylesheets");
				}

				ArrayList stls = new ArrayList();
				for (int j=0; j<stylesheets.size(); j++){
					HashMap stylesheet = (HashMap)stylesheets.get(j);
					Stylesheet stl = new Stylesheet();
					//st.setConvId(1);
					stl.setType((String)stylesheet.get("content_type_out"));
					stl.setXsl(Names.XSL_FOLDER + (String)stylesheet.get("xsl"));
					stl.setXsl_descr((String)stylesheet.get("description"));
					stl.setDdConv(false);
					stls.add(stl);
				}
				
				if(stls.size()>0){
					sc.setStylesheets(stls);				
					schemas.add(sc);
				}
			}
			st.setHandCodedStylesheets(schemas);

			
			//retrive conversions for DD tables
			List ddTables = DDServiceClient.getDDTables();
			schemas = new ArrayList();
			
			for (int i=0; i<ddTables.size(); i++){
				Hashtable schema = (Hashtable)ddTables.get(i);
				String tblId = (String)schema.get("tblId");
				String schemaUrl = Properties.ddURL + "GetSchema?id=TBL" + tblId;

				Schema sc = new Schema();
				sc.setId("TBL" + tblId);
				sc.setSchema(schemaUrl);
				sc.setDescription((String)schema.get("shortName"));

				
				List ddStylesheets = Conversion.getConversions();
				ArrayList stls = new ArrayList();
								
				for (int j=0; j<ddStylesheets.size(); j++){
					ConversionDto ddConv = ((ConversionDto) ddStylesheets.get(j));

					String convId = ddConv.getConvId();
					String xsl_url = Properties.gdemURL + "GetStylesheet?id=" + tblId + "&conv=" + convId;
					
					Stylesheet stl = new Stylesheet();
					stl.setType(ddConv.getResultType());
					stl.setXsl(xsl_url);
					stl.setXsl_descr(ddConv.getDescription());
					stl.setDdConv(true);
					stls.add(stl);				
				}
				
				sc.setStylesheets(stls);				
				schemas.add(sc);
							
			}
			st.setDdStylesheets(schemas);
			
		} catch (Exception e) {			
			_logger.debug(e.toString());
			throw new DCMException(BusinessConstants.EXCEPTION_GENERAL);          
		}
		return st;
		
	}
	
	public StylesheetListHolder getSchemaStylesheets(String schema,String user_name) throws DCMException{
		StylesheetListHolder st = new StylesheetListHolder();

		Vector hcSchemas;
		ArrayList schemas;
		
		
		try {
			
			schemas = new ArrayList();
			boolean ssiPrm = SecurityUtil.hasPerm(user_name, "/" + Names.ACL_STYLESHEETS_PATH, "i");
			boolean ssdPrm = SecurityUtil.hasPerm(user_name, "/" + Names.ACL_STYLESHEETS_PATH, "d");
			boolean convPrm = SecurityUtil.hasPerm(user_name, "/" + Names.ACL_TESTCONVERSION_PATH, "x");
			
		    			
			st.setSsdPrm(ssdPrm);
			st.setSsiPrm(ssiPrm);
			st.setConvPrm(convPrm);
		

			DbModuleIF dbM= GDEMServices.getDbModule();
			
			String schemaId = dbM.getSchemaID(schema);

			if(schemaId == null){
				st.setHandcoded(false);			
			}else{
				st.setHandcoded(true);
			}
				
			
			ConversionService cs = new ConversionService();
			Vector stylesheets=cs.listConversions(schema);
			ArrayList stls = new ArrayList();
			Schema sc = new Schema();
			sc.setId(schemaId);
			sc.setSchema(schema);
			
            for (int i=0; i<stylesheets.size(); i++){
                Hashtable hash = (Hashtable)stylesheets.get(i);
                String convert_id = (String)hash.get("convert_id");
                String xsl = (String)hash.get("xsl");
                String type = (String)hash.get("content_type_out");
                String description = (String)hash.get("description");
				String last_modified="";
				boolean	ddConv = false;
				String  xslUrl;
				
				if(!xsl.startsWith(Properties.gdemURL + "GetStylesheet?id=")){
				
                    File f=new File(Properties.xslFolder + xsl);	
					if (f!=null)
						last_modified = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.MEDIUM).format(new Date(f.lastModified()));
					xslUrl = Names.XSL_FOLDER + (String)hash.get("xsl");
				}else{
					xslUrl = (String)hash.get("xsl");
					ddConv = true;
				}

				Stylesheet stl = new Stylesheet();
				//st.setConvId(1);
				stl.setType((String)hash.get("content_type_out"));				
				stl.setXsl(xslUrl);
				stl.setXsl_descr((String)hash.get("description"));
				stl.setModified(last_modified);				
				stl.setConvId((String)hash.get("convert_id"));
				stl.setDdConv(ddConv);
				stls.add(stl);

				
            }			
			
			sc.setStylesheets(stls);
			schemas.add(sc);
			st.setHandCodedStylesheets(schemas);
		} catch (Exception e) {			
			_logger.debug(e.toString());
			throw new DCMException(BusinessConstants.EXCEPTION_GENERAL);          
		}
		return st;
	}
			
	public void update(String user, String schemaId, String schema, String description, String  dtdPublicId) throws DCMException{
		
	        try{
	          if (!SecurityUtil.hasPerm(user, "/" + Names.ACL_SCHEMA_PATH, "u")){
				  _logger.debug("You don't have permissions to delete schemas!");
				  throw new DCMException(BusinessConstants.EXCEPTION_AUTORIZATION_SCHEMA_UPDATE);                              
	          }
	        }
	        catch (DCMException e){					    
				throw e;
	        }
			catch (Exception e){			
				_logger.debug(e.toString());
				throw new DCMException(BusinessConstants.EXCEPTION_GENERAL);          
	        }
			
	       //StringBuffer err_buf = new StringBuffer();
	       //String del_id= (String)req.getParameter(Names.XSD_DEL_ID);

	       try{
			   DbModuleIF dbM= GDEMServices.getDbModule();
    		   dbM.updateSchema(schemaId, schema, description, dtdPublicId);
	        }
	        catch (Exception e){
				_logger.debug(e.toString());
				throw new DCMException(BusinessConstants.EXCEPTION_GENERAL);          
	        }
				
	}

	
	public SchemaElemHolder getSchemaElems(String user_name, String schemaId) throws DCMException{
		
		SchemaElemHolder se = new SchemaElemHolder();

		boolean xsduPrm = false;
		Schema schema;
		ArrayList elems;
		
		
		try {
			elems = new ArrayList();
			xsduPrm = SecurityUtil.hasPerm(user_name, "/" + Names.ACL_SCHEMA_PATH, "u");
			
			se.setXsduPrm(xsduPrm);

			DbModuleIF dbM= GDEMServices.getDbModule();

			
			Vector list = dbM.getSchemas(schemaId, false);
			if (list==null) list=new Vector();
			
			String name = "";
			String schema_desc = null;
			String dtd_public_id = null;
			boolean isDTD = false;
						
			
			if (list.size()>0){
			
				schema = new Schema();
				
				HashMap schemaHash = (HashMap)list.get(0);
				schema.setSchema((String)schemaHash.get("xml_schema"));
				schema.setDescription((String)schemaHash.get("description"));
				schema.setDtdPublicId((String)schemaHash.get("dtd_public_id"));
				name=(String)schemaHash.get("xml_schema");
				int name_len = name.length();
				String schema_end = name.substring((name_len-3), (name_len)).toLowerCase();
				if (schema_end.equals("dtd")) isDTD=true;				
				schema.setIsDTD(isDTD);
				se.setSchema(schema);
			}
			
			
			
			Vector root_elems = (Vector)dbM.getSchemaRootElems(schemaId);
			if (root_elems==null) root_elems=new Vector();


			for (int i=0; i<root_elems.size(); i++){
				HashMap hash = (HashMap)root_elems.get(i);
				
				RootElem rElem = new RootElem();
				rElem.setElemId((String)hash.get("rootelem_id"));
				rElem.setName((String)hash.get("elem_name"));
				rElem.setNamespace((String)hash.get("namespace"));
				elems.add(rElem);
			}			
			se.setRootElem(elems);
			
		} catch (Exception e) {			
			e.printStackTrace();
			_logger.debug(e.toString());
			throw new DCMException(BusinessConstants.EXCEPTION_GENERAL);          
		}
		return se;
		
	}
	
	
	 public static void main(String[] args) throws DCMException
     {
		 SchemaManager s = new SchemaManager();
		 SchemaElemHolder d = s.getSchemaElems( "_admin","37");
     }
	
	
}
