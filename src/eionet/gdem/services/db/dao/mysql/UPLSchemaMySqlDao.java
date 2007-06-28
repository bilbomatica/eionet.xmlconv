package eionet.gdem.services.db.dao.mysql;

import eionet.gdem.Properties;
import eionet.gdem.services.db.dao.IUPLSchemaDao;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Vector;
import java.util.Hashtable;


public class UPLSchemaMySqlDao  extends MySqlBaseDao implements IUPLSchemaDao {

	private static final String qUplSchema = 	"SELECT " 
												+ UPL_SCHEMA_ID_FLD + ", " 
												+ UPL_SCHEMA_FLD + ", " 
												+ UPL_SCHEMA_DESC 
												+ " FROM " + UPL_SCHEMA_TABLE 
												+ " ORDER BY " + UPL_SCHEMA_FLD;
	

	private static final String qInsertUplSchema = "INSERT INTO " 
													+ UPL_SCHEMA_TABLE 
													+ " ( " 
													+ UPL_SCHEMA_FLD + " ," 
													+ UPL_SCHEMA_DESC 
													+ ") " 
													+ "VALUES (?,?)";
	
	
	public static final String qRemoveUplSchema = "DELETE FROM " + UPL_SCHEMA_TABLE + " WHERE " + UPL_SCHEMA_ID_FLD + "= ?" ;
	
	
	private static final String  qUplSchemaByID = 	"SELECT " 
													+ SCHEMA_ID_FLD + ", " 
													+ UPL_SCHEMA_FLD + "," 
													+ UPL_SCHEMA_DESC 
													+ " FROM " + UPL_SCHEMA_TABLE 
													+ " WHERE " + SCHEMA_ID_FLD + "= ?";
	

	private static final String qUpdateUplSchema = "UPDATE  " + UPL_SCHEMA_TABLE 
													+ " SET " + SCHEMA_DESCR_FLD + "= ? " 
													+ " WHERE " + UPL_SCHEMA_ID_FLD + "= ?";

	private static final String qUpdateSchema = "UPDATE  " + SCHEMA_TABLE 
												+ " SET " + SCHEMA_DESCR_FLD + "= ? "  
												+ " WHERE " + XML_SCHEMA_FLD + "= ? " ;

	private static final String qUplSchemaByUplSchemaId = "SELECT " + UPL_SCHEMA_FLD + " FROM " + UPL_SCHEMA_TABLE + " WHERE " + UPL_SCHEMA_ID_FLD + "= ?";	
	

	private static final String  checkUplSchemaFile = "SELECT COUNT(*) FROM " + UPL_SCHEMA_TABLE + " WHERE " + UPL_SCHEMA_FLD + "= ?";	
	

	public UPLSchemaMySqlDao() {}
	

	
	
/*	public Vector getUplSchema() throws SQLException {

		String sql = "SELECT " + UPL_SCHEMA_ID_FLD + ", " + UPL_SCHEMA_FLD + ", " + UPL_SCHEMA_DESC + " FROM " + UPL_SCHEMA_TABLE + " ORDER BY " + UPL_SCHEMA_FLD;

		String r[][] = _executeStringQuery(sql);

		Vector v = new Vector();

		for (int i = 0; i < r.length; i++) {
			Hashtable h = new Hashtable();
			h.put("id", r[i][0]);
			h.put("schema", r[i][1]);
			h.put("description", r[i][2]);
			v.add(h);
		}
		return v;
	}
*/
	public Vector getUplSchema() throws SQLException{
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs =null;
		Vector v = null;
 		
		if (isDebugMode){ logger.debug("Query is " + qUplSchema);}
		
		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(qUplSchema);
			rs = pstmt.executeQuery();			
			String[][] r = getResults(rs);
			v = new Vector(r.length);
			for (int i = 0; i < r.length; i++) {
				Hashtable h = new Hashtable();
				h.put("id", r[i][0]);
				h.put("schema", r[i][1]);
				h.put("description", r[i][2]);
				v.add(h);
			}
		} 
		finally {
			closeAllResources(rs,pstmt,conn);
		}		
		return v;		
	}

	
	
	
/*	public String addUplSchema(String schema, String description) throws SQLException {

		String sql = "INSERT INTO " + UPL_SCHEMA_TABLE + " ( " + UPL_SCHEMA_FLD + " ," + UPL_SCHEMA_DESC + ") VALUES (" + Utils.strLiteral(schema) + ", " + Utils.strLiteral(description) + ")";

		_executeUpdate(sql);

		return _getLastInsertID();
	}
*/
	public String addUplSchema(String schema, String description) throws SQLException{
		Connection conn = null;
		PreparedStatement pstmt = null;
		
		if (isDebugMode){ logger.debug("Query is " + qInsertUplSchema);}		
		try{
			conn = getConnection();	
			pstmt = conn.prepareStatement(qInsertUplSchema);
			pstmt.setString(1, schema);
			pstmt.setString(2, description);
			pstmt.executeUpdate();
		}finally{
			closeAllResources(null,pstmt,conn);			
		}
		return getLastInsertID();				
	}
	
	
	
	
/*	public void removeUplSchema(String uplSchemaId) throws SQLException {

		String sql = "DELETE FROM " + UPL_SCHEMA_TABLE + " WHERE " + UPL_SCHEMA_ID_FLD + "=" + uplSchemaId;
		_executeUpdate(sql);
		// System.out.println(sql);

	}
*/	
	public void removeUplSchema(String uplSchemaId) throws SQLException{
		Connection conn = null;
		PreparedStatement pstmt = null;
		
		if (isDebugMode){ logger.debug("Query is " + qRemoveUplSchema);}
		
		try{
			conn = getConnection();
			pstmt = conn.prepareStatement(qRemoveUplSchema);
			pstmt.setInt(1, Integer.parseInt(uplSchemaId));				
			pstmt.executeUpdate();
		}finally{
			closeAllResources(null,pstmt,conn);			
		}						
	}
	

	
/*	public void updateUplSchema(String schema_id, String description) throws SQLException {

		description = (description == null ? "" : description);

		String sql = "UPDATE  " + UPL_SCHEMA_TABLE + " SET " + SCHEMA_DESCR_FLD + "=" + Utils.strLiteral(description) + " WHERE " + UPL_SCHEMA_ID_FLD + "=" + schema_id;

		_executeUpdate(sql);

		Hashtable sch = getUplSchemaById(schema_id);
		String schema = (String) sch.get("schema");

		sql = "UPDATE  " + SCHEMA_TABLE + " SET " + SCHEMA_DESCR_FLD + "=" + Utils.strLiteral(description) + " WHERE " + XML_SCHEMA_FLD + "=" + Utils.strLiteral(Properties.gdemURL + "/schema/" + schema);

		_executeUpdate(sql);

	}
	
*/	

	
	public void updateUplSchema(String schema_id, String description) throws SQLException{
		Connection conn = null;
		PreparedStatement pstmt = null;

		description = (description == null ? "" : description);
		
		try{
			conn = getConnection();
			conn.setAutoCommit(false);

			if (isDebugMode){ logger.debug("Query is " + qUpdateUplSchema);}			
			pstmt = conn.prepareStatement(qUpdateUplSchema);
			pstmt.setString(1,description);
			pstmt.setInt(2,Integer.parseInt(schema_id));
			pstmt.executeUpdate();
			pstmt.close();				
			
			Hashtable sch = getUplSchemaById(schema_id);
			String schema = (String) sch.get("schema");
			
			if (isDebugMode){ logger.debug("Query is " + qUpdateSchema);}			
			pstmt = conn.prepareStatement(qUpdateSchema);
			pstmt.setString(1,description);
			pstmt.setString(2,Properties.gdemURL + "/schema/" + schema);
			pstmt.executeUpdate();				

			conn.commit();
		}
		catch(SQLException sqle){
			if (conn != null) conn.rollback();
			throw new SQLException(sqle.getMessage(),sqle.getSQLState());				
		}finally{
			closeAllResources(null,pstmt,conn);			
		}		
		
	}	
	

	
/*	public String getUplSchema(String uplSchemaId) throws SQLException {

		String sql = "SELECT " + UPL_SCHEMA_FLD + " FROM " + UPL_SCHEMA_TABLE + " WHERE " + UPL_SCHEMA_ID_FLD + "=" + uplSchemaId;

		String[][] r = _executeStringQuery(sql);

		if (r.length == 0) return null;

		return r[0][0];
	}
*/
	
	public String getUplSchema(String uplSchemaId) throws SQLException{
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs =null;
 		String result = null;
		
		if (isDebugMode){ logger.debug("Query is " + qUplSchemaByUplSchemaId);}
		
		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(qUplSchemaByUplSchemaId);

			pstmt.setInt(1,Integer.parseInt(uplSchemaId));
			rs = pstmt.executeQuery();

			String[][] r = getResults(rs);
			if (r.length == 0) return null;
			
			result = r[0][0];
		} 
		finally {
			closeAllResources(rs,pstmt,conn);
		}		
		return result;	
	} 
	
	
/*	public Hashtable getUplSchemaById(String schemaId) throws SQLException {

		int id = 0;

		if (schemaId == null) throw new SQLException("Schema ID not defined");
		try {
			id = Integer.parseInt(schemaId);
		} catch (NumberFormatException n) {
			throw new SQLException("not numeric ID " + schemaId);
		}

		String sql = "SELECT " + SCHEMA_ID_FLD + ", " + UPL_SCHEMA_FLD + "," + UPL_SCHEMA_DESC + " FROM " + UPL_SCHEMA_TABLE + " WHERE " + SCHEMA_ID_FLD + "=" + id;

		String[][] r = _executeStringQuery(sql);

		Hashtable h = new Hashtable();
		h.put("schema_id", r[0][0]);
		h.put("schema", r[0][1]);
		h.put("description", r[0][2]);

		return h;
	}
*/	

	
	public Hashtable getUplSchemaById(String schemaId) throws SQLException{
		int id = 0;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs =null;
		Hashtable h = null;
	
		if (schemaId == null) throw new SQLException("Schema ID not defined");
		try {
			id = Integer.parseInt(schemaId);
		} catch (NumberFormatException n) {
			throw new SQLException("not numeric ID " + schemaId);
		}
			
		if (isDebugMode){ logger.debug("Query is " + qUplSchemaByID);}
		
		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(qUplSchemaByID);
			pstmt.setInt(1,id);
			rs = pstmt.executeQuery();			
			String[][] r = getResults(rs);

			h = new Hashtable();
			h.put("schema_id", r[0][0]);
			h.put("schema", r[0][1]);
			h.put("description", r[0][2]);			
		} 
		finally {
			closeAllResources(rs,pstmt,conn);
		}		
		return h;			
	}
		
	
/*	public boolean checkUplSchemaFile(String schemaFileName) throws SQLException {

		String sql = "SELECT COUNT(*) FROM " + UPL_SCHEMA_TABLE + " WHERE " + UPL_SCHEMA_FLD + "="+ Utils.strLiteral(schemaFileName);

		String[][] r = _executeStringQuery(sql);

		String count = r[0][0];
		if (count.equals("0")) {
			return false;
		} else {
			return true;
		}
	}
*/		
	public boolean checkUplSchemaFile(String schemaFileName) throws SQLException{
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs =null;
		boolean result = false;
 		
		if (isDebugMode){ logger.debug("Query is " + checkUplSchemaFile);}
		
		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(checkUplSchemaFile);
			pstmt.setString(1,schemaFileName);
			rs = pstmt.executeQuery();			
			String[][] r = getResults(rs);
			String count = r[0][0];
			if (!count.equals("0")) 
				result = true;			
		} 
		finally {
			closeAllResources(rs,pstmt,conn);
		}
		return result;					
	}
}