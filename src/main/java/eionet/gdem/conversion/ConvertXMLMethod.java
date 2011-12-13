/*
 * Created on 20.02.2008
 */
package eionet.gdem.conversion;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eionet.gdem.GDEMException;
import eionet.gdem.Properties;
import eionet.gdem.conversion.converters.ConvertContext;
import eionet.gdem.conversion.converters.ConvertStartegy;
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
import eionet.gdem.services.GDEMServices;
import eionet.gdem.services.db.dao.IConvTypeDao;
import eionet.gdem.services.db.dao.IStyleSheetDao;
import eionet.gdem.utils.InputFile;
import eionet.gdem.utils.Utils;
import eionet.gdem.utils.ZipUtil;
import eionet.gdem.utils.xml.IXmlCtx;
import eionet.gdem.utils.xml.XmlContext;
import eionet.gdem.utils.xml.XmlException;

/**
 * Conversion Service methods that executes XML conversions to other file types using XSL transformations.
 *
 * @author Enriko Käsper, TietoEnator Estonia AS
 */

public class ConvertXMLMethod extends RemoteServiceMethod {

    public static final String CONTENTTYPE_KEY = "content-type";
    public static final String FILENAME_KEY = "filename";
    public static final String CONTENT_KEY = "content";

    private IStyleSheetDao styleSheetDao = GDEMServices.getDaoService().getStyleSheetDao();
    private IConvTypeDao convTypeDao = GDEMServices.getDaoService().getConvTypeDao();

    /** */
    private static final Log LOGGER = LogFactory.getLog(ConvertXMLMethod.class);

    /**
     * Converts the XML file to a specific format.
     *
     * @param sourceURL
     *            URL of the XML file to be converted
     * @param convertId
     *            ID of desired conversion as the follows: - If conversion ID begins with the DD DCM will generate appropriate
     *            stylesheet on the fly. - If conversion ID is number the DCM will consider consider hand coded conversion
     * @return Hashtable containing two elements: - content-type (String) - content (Byte array)
     * @throws GDEMException
     *             Thrown in case of errors
     */
    public Hashtable convert(String sourceURL, String convertId) throws GDEMException {
        OutputStream resultStream = null;
        String cnvFileName = null;
        String cnvTypeOut = null;
        String cnvFileExt = null;
        String cnvContentType = null;

        LOGGER.debug("sourceURL=" + sourceURL + "convertId=" + convertId);
        if (convertId.startsWith("DD")) {
            return convertDDTable(sourceURL, convertId);
        } else {

            Hashtable result = new Hashtable();
            String xslFile = null;
            String outputFileName = null;
            InputFile src = null;

            try {
                src = new InputFile(sourceURL);
                src.setAuthentication(getTicket());
                src.setTrustedMode(isTrustedMode());
                cnvFileName = Utils.isNullStr(src.getFileNameNoExtension()) ? DEFAULT_FILE_NAME : src.getFileNameNoExtension();

                try {
                    HashMap styleSheetData = styleSheetDao.getStylesheetInfo(convertId);

                    if (styleSheetData == null) {
                        throw new GDEMException("No stylesheet info for convertID= " + convertId);
                    }
                    xslFile = getXslFolder() + (String) styleSheetData.get("xsl");
                    cnvTypeOut = (String) styleSheetData.get("content_type_out");

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
                outputFileName =
                    executeConversion(ctx, src.getCdrParams(), cnvTypeOut);

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

            byte[] file = Utils.fileToBytes(outputFileName);
            result.put(CONTENT_KEY, file);
            try {
                Utils.deleteFile(outputFileName);
            } catch (Exception e) {

                LOGGER.error("Couldn't delete the result file: " + outputFileName, e);
            }

            return result;
        }
    }

    public Hashtable convertDDTable(String sourceURL, String convertId) throws GDEMException {
        OutputStream result = null;
        Hashtable h = new Hashtable();
        String outputFileName = null;
        InputFile src = null;
        String tblId = "";
        String convId = "";
        String cnvFileName = null;
        String cnvTypeOut = null;
        String cnvFileExt = null;
        String cnvContentType = null;

        // prase idtable and id conversion
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

            try {
                cnvTypeOut = conv.getResultType();
                Hashtable convType = convTypeDao.getConvType(cnvTypeOut);

                if (convType != null) {
                    try {
                        cnvContentType = (String) convType.get("content_type");// content type used in HTTP header
                        cnvFileExt = (String) convType.get("file_ext");
                        cnvTypeOut = (String) convType.get("conv_type");// content type ID
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
            outputFileName =
                executeConversion(ctx, src.getCdrParams(), cnvTypeOut);

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
     * The method checks if the given file is XML and calls convert methdo. If the file is not XML, then the method tries to unzip
     * it and find an XML from zip file.
     *
     * @param fileInput
     * @param convertId
     * @param fileName
     * @return
     * @throws GDEMException
     *             no XML file found from given input stream
     */
    public Hashtable convertPush(InputStream fileInput, String convertId, String fileName) throws GDEMException {
        String filePath = null;
        Hashtable result = null;
        String tmpFolderName = null;
        boolean isXml = false;
        FileOutputStream outStream = null;

        try {
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

            // Creates an URI for temprarily stored XML file and calll convert method with it
            String fileUri = Utils.getURIfromPath(filePath, false);
            result = convert(fileUri, convertId);

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
     * Choose the correct converter for given content type and execute the conversion
     *
     * @param source
     * @param xslt
     * @param result
     * @param params
     * @param cnvFileExt
     * @param cnvTypeOut
     * @return
     * @throws Exception
     */
    private String executeConversion(ConvertContext ctx, Map<String, String> params,
            String cnvTypeOut) throws Exception {
        ConvertStartegy cs = null;
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
     */
    private String getDDTableDefUrl(String tblId) {
        if (GDEMServices.isTestConnection()) {
            return "file://".concat(getClass().getClassLoader().getResource("seed-DD-tabledef-" + tblId + ".xml").getFile());
        } else {
            return Properties.ddURL + "/GetTableDef?id=" + tblId;
        }

    }
}