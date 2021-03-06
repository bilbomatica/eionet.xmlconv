/*
 * Created on 20.02.2008
 */
package eionet.gdem.conversion;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.io.IOUtils;



import eionet.gdem.GDEMException;
import eionet.gdem.Properties;
import eionet.gdem.conversion.converters.ConvertContext;
import eionet.gdem.conversion.converters.ConvertStrategy;
import eionet.gdem.conversion.converters.ExcelConverter;
import eionet.gdem.conversion.converters.HTMLConverter;
import eionet.gdem.conversion.converters.OdsConverter;
import eionet.gdem.conversion.converters.PDFConverter;
import eionet.gdem.conversion.converters.TextConverter;
import eionet.gdem.conversion.converters.XMLConverter;
import eionet.gdem.dcm.Conversion;
import eionet.gdem.dcm.XslGenerator;
import eionet.gdem.dcm.remote.HttpMethodResponseWrapper;
import eionet.gdem.dcm.remote.RemoteServiceMethod;
import eionet.gdem.dto.ConversionDto;
import eionet.gdem.dto.Stylesheet;
import eionet.gdem.services.GDEMServices;
import eionet.gdem.services.db.dao.IConvTypeDao;
import eionet.gdem.services.db.dao.IStyleSheetDao;
import eionet.gdem.utils.InputFile;
import eionet.gdem.utils.Utils;
import eionet.gdem.utils.ZipUtil;
import eionet.gdem.utils.xml.IXmlCtx;
import eionet.gdem.utils.xml.XmlContext;
import eionet.gdem.utils.xml.XmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Conversion Service methods that executes XML conversions to other file types using XSL transformations.
 *
 * @author Enriko Käsper, TietoEnator Estonia AS
 */

public class ConvertXMLMethod extends RemoteServiceMethod {

    /** Content type key name used in conversion result Hashtable. */
    public static final String CONTENTTYPE_KEY = "content-type";
    /** Filename key name used in conversion result Hashtable. */
    public static final String FILENAME_KEY = "filename";
    /** Content key name used in conversion result Hashtable. */
    public static final String CONTENT_KEY = "content";

    /** Dao for retrieving stylesheet info from DB. */
    private IStyleSheetDao styleSheetDao = GDEMServices.getDaoService().getStyleSheetDao();
    /** Dao for retrieving conversion type info from DB. */
    private IConvTypeDao convTypeDao = GDEMServices.getDaoService().getConvTypeDao();

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(ConvertXMLMethod.class);

    /**
     * Converts the XML file to a specific format.
     * 
     * @param sourceURL URL of the XML file to be converted
     * @param convertId ID of desired conversion as the follows: - If conversion ID begins with the DD DCM will generate appropriate
     *            stylesheet on the fly. - If conversion ID is number the DCM will consider consider hand coded conversion.
     * @return Hashtable containing two elements: - content-type (String) - content (Byte array)
     * @throws GDEMException Thrown in case of conversion errors.
     */
    public Hashtable<String, Object> convert(String sourceURL, String convertId) throws GDEMException {
        return convert(sourceURL, convertId, null);
    }

    /**
     * Converts the XML file to a specific format.
     *
     * @param sourceURL URL of the XML file to be converted
     * @param convertId ID of desired conversion as the follows: - If conversion ID begins with the DD DCM will generate appropriate
     *            stylesheet on the fly. - If conversion ID is number the DCM will consider consider hand coded conversion.
     * @param externalParameters Map of parameters passed to conversion engine.
     * @return Hashtable containing two elements: - content-type (String) - content (Byte array)
     * @throws GDEMException Thrown in case of conversion errors.
     */
    public Hashtable<String, Object> convert(String sourceURL, String convertId, Map<String, String> externalParameters)
            throws GDEMException {
        OutputStream resultStream = new ByteArrayOutputStream();
        String cnvFileName = null;
        String cnvTypeOut = null;
        String cnvFileExt = null;
        String cnvContentType = null;
        Map<String, String> conversionParameters;

        LOGGER.debug("sourceURL=" + sourceURL + "convertId=" + convertId);

        if (convertId.startsWith("DD")) {
            return convertDDTable(sourceURL, convertId, externalParameters);
        } else {

            Hashtable<String, Object> result = new Hashtable<String, Object>();
            String xslFile = null;
            String outputFileName = null;
            InputFile src = null;

            try {
                src = new InputFile(sourceURL);
                src.setAuthentication(getTicket());
                src.setTrustedMode(isTrustedMode());
                cnvFileName = Utils.isNullStr(src.getFileNameNoExtension()) ? DEFAULT_FILE_NAME : src.getFileNameNoExtension();

                conversionParameters = src.getCdrParams();
                // override default CDR parameters if they are set up externally.
                if (externalParameters != null) {
                    conversionParameters.putAll(externalParameters);
                }

                try {
                    Stylesheet styleSheetData = styleSheetDao.getStylesheet(convertId);

                    if (styleSheetData == null) {
                        throw new GDEMException("No stylesheet info for convertID= " + convertId);
                    }
                    xslFile = styleSheetData.getXslFileFullPath();
                    cnvTypeOut = styleSheetData.getType();

                    Hashtable convType = convTypeDao.getConvType(cnvTypeOut);

                    if (convType != null) {
                        try {
                            cnvContentType = (String) convType.get("content_type");
                            cnvFileExt = (String) convType.get("file_ext");
                        } catch (Exception e) {
                            LOGGER.error("error getting conv types", e);
                            // Take no action, use default params
                        }
                    }
                    if (cnvContentType == null) {
                        cnvContentType = RemoteServiceMethod.DEFAULT_CONTENT_TYPE;
                    }
                    if (cnvFileExt == null) {
                        cnvFileExt = DEFAULT_FILE_EXT;
                    }

                } catch (Exception e) {
                    LOGGER.error("error getting con types", e);
                    throw new GDEMException("Error getting stylesheet info from repository for " + convertId, e);
                }
                if (isHttpRequest()) {
                    try {
                        HttpMethodResponseWrapper httpResponse = getHttpResponse();
                        httpResponse.setContentType(cnvContentType);
                        httpResponse.setContentDisposition(cnvFileName + "." + cnvFileExt);
                        resultStream = httpResponse.getOutputStream();
                    } catch (IOException e) {
                        LOGGER.error("Error getting response outputstream ", e);
                        throw new GDEMException("Error getting response outputstream " + e.toString(), e);
                    }
                }
                ConvertContext ctx = new ConvertContext(src.getSrcInputStream(), xslFile, resultStream, cnvFileExt);
                outputFileName = executeConversion(ctx, conversionParameters, cnvTypeOut);

            } catch (MalformedURLException mfe) {
                LOGGER.error("Bad URL", mfe);
                throw new GDEMException("Bad URL", mfe);
            } catch (IOException ioe) {
                LOGGER.error("Error opening URL", ioe);
                throw new GDEMException("Error opening URL", ioe);
            } catch (GDEMException ge) {
                LOGGER.error("Error converting", ge);
                throw ge;
            } catch (Exception e) {
                LOGGER.error("Error converting", e);
                throw new GDEMException("Convert error: " + e.toString(), e);
            } finally {
                try {
                    if (src != null) {
                        src.close();
                    }
                } catch (Exception e) {
                }
            }

            result.put(CONTENTTYPE_KEY, cnvContentType);
            result.put(FILENAME_KEY, cnvFileName + "." + cnvFileExt);

            if (isHttpRequest()) {
                return result;
            }

            // byte[] file = Utils.fileToBytes(outputFileName);
            byte[] file = ((ByteArrayOutputStream) resultStream).toByteArray();
            result.put(CONTENT_KEY, file);
            try {
                Utils.deleteFile(outputFileName);
            } catch (Exception e) {

                LOGGER.error("Couldn't delete the result file: " + outputFileName, e);
            }

            return result;
        }
    }

    /**
     * Use automatically generated XSL for converting XML with Data Dictionary XML Schema.
     * @param sourceURL URL of source XML file.
     * @param convertId Conversion ID.
     * @param externalParameters Map of parameters forwarded to conversion engine.
     * @return conversion result as key value pairs in Hashtable.
     * @throws GDEMException in case of conversion Exception.
     */
    public Hashtable<String, Object> convertDDTable(String sourceURL, String convertId, Map<String, String> externalParameters)
            throws GDEMException {
        OutputStream result = null;
        Hashtable<String, Object> h = new Hashtable<String, Object>();
        String outputFileName = null;
        InputFile src = null;
        String tblId = "";
        String convId = "";
        String cnvFileName = null;
        String cnvTypeOut = null;
        String cnvFileExt = null;
        String cnvContentType = null;
        Map<String, String> conversionParameters = null;

        // parse table ID and conversion ID
        if (convertId.startsWith("DD")) {
            tblId = convertId.substring(6, convertId.indexOf("_CONV"));
            convId = convertId.substring(convertId.indexOf("_CONV") + 5, convertId.length());
        }

        ConversionDto conv = Conversion.getConversionById(convId);
        LOGGER.debug("Conv: " + conv);
        String format = Properties.metaXSLFolder + File.separatorChar + conv.getStylesheet();
        String url = getDDTableDefUrl(tblId);

        try {
            ByteArrayInputStream byteIn = XslGenerator.convertXML(url, format);
            src = new InputFile(sourceURL);
            src.setAuthentication(getTicket());
            src.setTrustedMode(isTrustedMode());
            cnvFileName = Utils.isNullStr(src.getFileNameNoExtension()) ? DEFAULT_FILE_NAME : src.getFileNameNoExtension();

            conversionParameters = src.getCdrParams();
            // override default CDR parameters if they are set up externally.
            if (externalParameters != null) {
                conversionParameters.putAll(externalParameters);
            }

            try {
                cnvTypeOut = conv.getResultType();
                Hashtable convType = convTypeDao.getConvType(cnvTypeOut);

                if (convType != null) {
                    try {
                        cnvContentType = (String) convType.get("content_type"); // content type used in HTTP header
                        cnvFileExt = (String) convType.get("file_ext");
                        cnvTypeOut = (String) convType.get("conv_type"); // content type ID
                    } catch (Exception e) {
                        LOGGER.error("Error getting conversion types ", e);
                        // Take no action, use default params
                    }
                }
                if (cnvContentType == null) {
                    cnvContentType = DEFAULT_CONTENT_TYPE;
                }
                if (cnvFileExt == null) {
                    cnvFileExt = DEFAULT_FILE_EXT;
                }

            } catch (Exception e) {
                LOGGER.error("Error getting stylesheet info from repository for " + convertId, e);
                throw new GDEMException("Error getting stylesheet info from repository for " + convertId, e);
            }
            if (isHttpRequest()) {
                try {
                    HttpMethodResponseWrapper httpResult = getHttpResponse();
                    httpResult.setContentType(cnvContentType);
                    httpResult.setContentDisposition(cnvFileName + "." + cnvFileExt);
                    result = httpResult.getOutputStream();

                } catch (IOException e) {
                    LOGGER.error("Error getting response outputstream ", e);
                    throw new GDEMException("Error getting response outputstream " + e.toString(), e);
                }
            }
            ConvertContext ctx = new ConvertContext(src.getSrcInputStream(), byteIn, result, cnvFileExt);
            outputFileName = executeConversion(ctx, conversionParameters, cnvTypeOut);

        } catch (MalformedURLException mfe) {
            LOGGER.error("Bad URL", mfe);
            throw new GDEMException("Bad URL", mfe);
        } catch (IOException ioe) {
            LOGGER.error("Error opening URL", ioe);
            throw new GDEMException("Error opening URL", ioe);
        } catch (GDEMException ge) {
            throw ge;
        } catch (Exception e) {
            LOGGER.error("Error converting", e);
            throw new GDEMException("Error converting", e);
        } finally {
            try {
                if (src != null) {
                    src.close();
                }
            } catch (Exception e) {
                LOGGER.error("Error converting", e);
            }
        }

        h.put(CONTENTTYPE_KEY, cnvContentType);
        h.put(FILENAME_KEY, cnvFileName + "." + cnvFileExt);
        if (isHttpRequest()) {
            return h;
        }
        byte[] file = Utils.fileToBytes(outputFileName);
        h.put(CONTENT_KEY, file);
        try {
            Utils.deleteFile(outputFileName);
        } catch (Exception e) {
            LOGGER.error("Couldn't delete the result file: " + outputFileName, e);
        }

        return h;

    }

    /**
     * The method checks if the given file is XML and calls convert method. If the file is not XML, then the method tries to unzip
     * it and find an XML from zip file. File name value is used in the result of the conversion.
     * File name parameter can be URL. In this case the CDR envelope URL is extracted from URL and passed to converter.
     *
     * @param fileInput InputStream with the source XML file to be converted.
     * @param convertId Conversion(XSL) ID.
     * @param fileName File name or URL of the input file.
     * @return Conversion result.
     * @throws GDEMException no XML file found from given input stream or XSL is missing with the given id.
     */
    public Hashtable<String, Object> convertPush(InputStream fileInput, String convertId, String fileName) throws GDEMException {
        String filePath = null;
        Hashtable<String, Object> result = null;
        String tmpFolderName = null;
        boolean isXml = false;
        FileOutputStream outStream = null;
        Map<String, String> cdrParams = null;

        try {
            if (Utils.isURL(fileName)) {
                InputFile inputFile = new InputFile(fileName);
                fileName = inputFile.getFileName();
                cdrParams = inputFile.getCdrParams();
            }
            // Store the file into temporary folder
            tmpFolderName = Utils.createUniqueTmpFolder();
            filePath = tmpFolderName + File.separator + (Utils.isNullStr(fileName) ? DEFAULT_FILE_NAME : fileName);

            File file = new File(filePath);
            outStream = new FileOutputStream(file);
            IOUtils.copy(fileInput, outStream);
            IOUtils.closeQuietly(outStream);
            // check if the stored file is XML
            try {
                IXmlCtx x = new XmlContext();
                x.setWellFormednessChecking();
                x.checkFromFile(filePath);
                isXml = true;
            } catch (XmlException xmle) {
                LOGGER.debug("The file is not well-formed XML, try to unzip it.");
            }

            // try to unzip the input file, if it is not XML
            if (!isXml) {
                try {
                    File zipFolder = new File(tmpFolderName + File.separator + "zip");
                    zipFolder.mkdir();
                    ZipUtil.unzip(filePath, zipFolder.getPath());
                    String zippedXml = Utils.findXMLFromFolder(zipFolder);
                    if (zippedXml == null) {
                        throw new GDEMException("Could not find XML");
                    } else {
                        filePath = zippedXml;
                    }
                } catch (Exception e) {
                    LOGGER.error("The file is not well-formed XML or zipped XML. Unable to convert it.");
                    throw new GDEMException("The file is not well-formed XML or zipped XML. Unable to convert it.");
                }
            }

            // Creates an URI for temporarily stored XML file and call convert method with it
            String fileUri = Utils.getURIfromPath(filePath, false);
            result = convert(fileUri, convertId, cdrParams);

        } catch (Exception e) {
            LOGGER.error(e.toString());
            throw new GDEMException(e.toString());
        } finally {
            IOUtils.closeQuietly(outStream);
            // delete all the data stored in temp folder
            try {
                Utils.deleteFolder(tmpFolderName);
            } catch (Exception e) {
                LOGGER.error("Couldn't delete the temporary folder: " + tmpFolderName, e);
            }
        }

        return result;

    }

    /**
     * Choose the correct converter for given content type and execute the conversion.
     * @param ctx ConvertContext
     * @param params Map of parameters passed to converter.
     * @param cnvTypeOut Output type of conversion.
     * @return File name of conversion result with correct extension.
     * @throws Exception in case of conversion error occurs
     */
    private String executeConversion(ConvertContext ctx, Map<String, String> params, String cnvTypeOut) throws Exception {
        ConvertStrategy cs = null;
        if (cnvTypeOut.startsWith("HTML")) {
            cs = new HTMLConverter();
        } else if (cnvTypeOut.equals("PDF")) {
            cs = new PDFConverter();
        } else if (cnvTypeOut.equals("EXCEL")) {
            cs = new ExcelConverter();
        } else if (cnvTypeOut.equals("XML")) {
            cs = new XMLConverter();
        } else if (cnvTypeOut.equals("ODS")) {
            cs = new OdsConverter();
        } else {
            cs = new TextConverter();
        }
        cs.setXslParams(params);
        return ctx.executeConversion(cs);
    }

    /**
     * Creates DD table definition URL. If it is a test, then the definition should be in xml stored locally.
     * @param tblId Numeric ID of Data Dictionary table.
     * @return URL of DD table definition XML.
     */
    private String getDDTableDefUrl(String tblId) {
        if (GDEMServices.isTestConnection()) {
            return "file://".concat(getClass().getClassLoader().getResource("seed-DD-tabledef-" + tblId + ".xml").getFile());
        } else {
            return Properties.ddURL + "/GetTableDef?id=" + tblId;
        }

    }
}
