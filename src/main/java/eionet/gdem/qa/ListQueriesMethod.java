package eionet.gdem.qa;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

import eionet.gdem.Constants;
import eionet.gdem.GDEMException;
import eionet.gdem.Properties;
import eionet.gdem.dcm.remote.RemoteServiceMethod;
import eionet.gdem.services.GDEMServices;
import eionet.gdem.services.db.dao.IConvTypeDao;
import eionet.gdem.services.db.dao.IQueryDao;
import eionet.gdem.services.db.dao.ISchemaDao;
import eionet.gdem.utils.Utils;
import java.util.Map;

/**
 * Implementation of listQueries and listQAScripts methods.
 *
 * @author Enriko Käsper, TietoEnator Estonia AS
 */
public class ListQueriesMethod extends RemoteServiceMethod {

    /** Query ID property key in ListQueries method result. */
    public static final String KEY_QUERY_ID = "query_id";
    /** Query file property key in ListQueries method result. */
    public static final String KEY_QUERY = "query";
    /** Query short name property key in ListQueries method result. */
    public static final String KEY_SHORT_NAME = "short_name";
    /** Query description property key in ListQueries method result. */
    public static final String KEY_DESCRIPTION = "description";
    /** Schema ID property key in ListQueries method result. */
    public static final String KEY_SCHEMA_ID = "schema_id";
    /** Schema URL property key in ListQueries method result. */
    public static final String KEY_XML_SCHEMA = "xml_schema";
    /** Type property key in ListQueries method result. */
    public static final String KEY_TYPE = "type";
    /** Output content type property key in ListQueries method result. */
    public static final String KEY_CONTENT_TYPE_OUT = "content_type_out";
    /** Output content type ID property key in ListQueries method result. */
    public static final String KEY_CONTENT_TYPE_ID = "content_type_id";
    /** XML file upper limit property key in ListQueries method result. */
    public static final String KEY_UPPER_LIMIT = "upper_limit";
    /** Upper limit for xml file size to be sent to manual QA. */
    public static final int VALIDATION_UPPER_LIMIT = Properties.qaValidationXmlUpperLimit;

    /** Default conversion output type. */
    public static final String DEFAULT_CONTENT_TYPE_ID = "HTML";

    /** DAO for getting schema info. */
    private ISchemaDao schemaDao = GDEMServices.getDaoService().getSchemaDao();;
    /** DAO for getting query info. */
    private IQueryDao queryDao = GDEMServices.getDaoService().getQueryDao();
    /** DAO for getting conversion types info. */
    private IConvTypeDao convTypeDao = GDEMServices.getDaoService().getConvTypeDao();

    /**
     * List all possible QA scripts (XQueries, XML Schemas, DTD, XSLT?) for this XML Schema. If schema is null, then all possible QA
     * scripts are returned
     *
     * @param schema
     *            URL of XML schema
     * @return array of Hastables with the following keys: qyery_id, short_name, description, query, schema_id, xml_schema,
     *         content_type_out, type
     *
     * @throws GDEMException If an error occurs.
     */
    public Vector listQueries(String schema) throws GDEMException {

        Vector v = new Vector();
        if (schema != null && schema.equals("")) {
            schema = null;
        }

        try {
            // Get schemas that has to be validated
            Vector schemas = schemaDao.getSchemas(schema, false);
            Hashtable convType = convTypeDao.getConvType(DEFAULT_CONTENT_TYPE_ID);
            String contentType =
                    (convType != null && convType.containsKey("content_type")) ? (String) convType.get("content_type")
                            : DEFAULT_QA_CONTENT_TYPE;

            if (schemas != null) {
                for (int i = 0; i < schemas.size(); i++) {
                    HashMap h = (HashMap) schemas.get(i);
                    String validate = (String) h.get("validate");
                    if (!Utils.isNullStr(validate)) {
                        if (validate.equals("1")) {
                            Hashtable ht = new Hashtable();
                            ht.put(KEY_QUERY_ID, String.valueOf(Constants.JOB_VALIDATION));
                            ht.put(KEY_SHORT_NAME, "XML Schema Validation");
                            ht.put(KEY_QUERY, h.get("xml_schema"));
                            ht.put(KEY_DESCRIPTION, h.get("description"));
                            ht.put(KEY_SCHEMA_ID, h.get("schema_id"));
                            ht.put(KEY_XML_SCHEMA, h.get("xml_schema"));
                            ht.put(KEY_CONTENT_TYPE_ID, DEFAULT_CONTENT_TYPE_ID);
                            ht.put(KEY_CONTENT_TYPE_OUT, contentType);
                            ht.put(KEY_TYPE, ((String) h.get("schema_lang")).toLowerCase());
                            ht.put(KEY_UPPER_LIMIT, String.valueOf(VALIDATION_UPPER_LIMIT));
                            v.add(ht);
                        }
                    }
                }
            }
            // Get XQueries
            Vector queries = queryDao.listQueries(schema);
            if (queries != null) {
                for (int i = 0; i < queries.size(); i++) {
                    Hashtable ht = (Hashtable) queries.get(i);
                    if (!isActive(ht)) continue;
                    ht.put(KEY_TYPE, Constants.QA_TYPE_XQUERY);
                    // return full URL of XQuerys
                    ht.put(KEY_QUERY, Properties.gdemURL + "/" + Constants.QUERIES_FOLDER + (String) ht.get("query"));
                    v.add(ht);
                }
            }
        } catch (Exception e) {
            throw new GDEMException("Error getting data from the DB " + e.toString(), e);
        }
        return v;
    }

    /**
     * List all XQueries and their modification times for this namespace returns also XML Schema validation.
     *
     * @param schema Schema to use
     * @return result is an Array of Arrays that contains 3 fields (script_id, description, last modification)
     * @throws GDEMException If an error occurs.
     */
    public Vector listQAScripts(String schema) throws GDEMException {
        Vector<Vector<String>> result = new Vector<Vector<String>>();
        Vector<String> resultQuery = null;
        try {
            Vector v = schemaDao.getSchemas(schema);

            if (Utils.isNullVector(v)) {
                return result;
            }

            HashMap h = (HashMap) v.get(0);
            String validate = (String) h.get("validate");
            if (!Utils.isNullStr(validate)) {
                if (validate.equals("1")) {
                    resultQuery = new Vector<String>();
                    resultQuery.add(String.valueOf(Constants.JOB_VALIDATION));
                    resultQuery.add("XML Schema Validation");
                    resultQuery.add("");
                    resultQuery.add(String.valueOf(VALIDATION_UPPER_LIMIT));
                    result.add(resultQuery);
                }
            }
            Vector queries = (Vector) h.get("queries");
            if (Utils.isNullVector(queries)) {
                return result;
            }

            for (int i = 0; i < queries.size(); i++) {
                HashMap hQueries = (HashMap) queries.get(i);
                if (!isActive(hQueries)) continue;
                String queryId = (String) hQueries.get("query_id");
                String queryFile = (String) hQueries.get("query");
                String queryDescription = (String) hQueries.get("descripton");
                String queryName = (String) hQueries.get("short_name");
                String queryUpperLimit = (String) hQueries.get("upper_limit");
                if (Utils.isNullStr(queryDescription)) {
                    if (Utils.isNullStr(queryName)) {
                        queryDescription = "Quality Assurance script";
                    } else {
                        queryDescription = queryName;
                    }
                }
                resultQuery = new Vector<String>();
                resultQuery.add(queryId);
                resultQuery.add(queryDescription);
                File f = new File(Properties.queriesFolder + File.separator + queryFile);
                String last_modified = "";

                if (f != null) {
                    last_modified = Utils.getDateTime(new Date(f.lastModified()));
                }

                resultQuery.add(last_modified);
                resultQuery.add(queryUpperLimit);
                result.add(resultQuery);
            }

        } catch (Exception e) {
            throw new GDEMException("Error getting data from the DB " + e.toString(), e);
        }

        return result;
    }

    /**
     * Returns if script is active.
     * @param query Query map
     * @return True if script is active.
     */
    private boolean isActive(Map query){
        return query.get("is_active").equals("1");
    }
}
