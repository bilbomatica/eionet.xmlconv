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
 * The Original Code is XMLCONV - Conversion and QA Service
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Enriko Käsper
 */
package eionet.gdem.conversion;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.commons.io.IOUtils;



import eionet.gdem.GDEMException;
import eionet.gdem.conversion.spreadsheet.DDXMLConverter;
import eionet.gdem.dcm.remote.HttpMethodResponseWrapper;
import eionet.gdem.dcm.remote.RemoteServiceMethod;
import eionet.gdem.dto.ConversionLogDto;
import eionet.gdem.dto.ConversionResultDto;
import eionet.gdem.dto.ConvertedFileDto;
import eionet.gdem.utils.InputFile;
import eionet.gdem.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DDXML Conversion method class.
 * @author Enriko Käsper, TietoEnator Estonia AS ConvertDDXMLMethod
 * @author George Sofianos
 */

public class ConvertDDXMLMethod extends RemoteServiceMethod {

    private boolean checkSchemaValidity = true;
    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(ConvertDDXMLMethod.class);

    /**
     * Converts DataDictionary MS Excel file to XML.
     *
     * @param sourceUrl - URL of the srouce Excel file
     * @return Vector result: error_code, xml_url, error_message
     * @throws GDEMException If an error occurs
     */
    public ConversionResultDto convertDD_XML(String sourceUrl) throws GDEMException {
        return convertDD_XML(sourceUrl, false, null);
    }

    /**
     * Converts DataDictionary MS Excel sheets to different XML files, where one xml file is dataset table.
     *
     * @param sourceUrl - URL of the source Excel file
     * @param sheetName Sheet name
     * @return Vector result: error_code, xml_url, error_message
     * @throws GDEMException If an error occurs
     */
    public ConversionResultDto convertDD_XML_split(String sourceUrl, String sheetName) throws GDEMException {
        return convertDD_XML(sourceUrl, true, sheetName);
    }

    /**
     * Method that calls converter to do the conversion.
     *
     * @param sourceUrl Source URL
     * @param split Split or not
     * @param sheetName Sheet name
     * @return Result transfer object
     * @throws GDEMException If an error occurs.
     */
    private ConversionResultDto convertDD_XML(String sourceUrl, boolean split, String sheetName) throws GDEMException {
        OutputStream resultStream = null;
        String sourceFileName = null;
        File file = null;
        ConversionResultDto resultObject = new ConversionResultDto();
        String errorMessage = null;

        try {
            InputFile sourceFile = getInptFile(sourceUrl);
            file = new File(sourceFile.saveSrcFile("tmp"));
            sourceFileName =
                Utils.isNullStr(sourceFile.getFileNameNoExtension()) ? DEFAULT_FILE_NAME : sourceFile.getFileNameNoExtension();

            // Detect the file format
            DDXMLConverter converter = DDXMLConverter.getConverter(file, resultObject, sheetName);
            boolean doConversion =
                (converter.isValidSchema() && ((split && converter.isValidSheetSchemas()) || !split))
                || !isCheckSchemaValidity();
            if (doConversion) {
                resultStream = getResultOutputStream(sourceFileName);
                converter.setHttpResponse(isHttpRequest());
                if (split) {
                    resultObject = converter.convertDD_XML_split(resultStream, sheetName);
                } else {
                    String tmpFileName = Utils.getUniqueTmpFileName(".xml");
                    if (resultStream == null) {
                        resultStream = new FileOutputStream(tmpFileName);
                    }
                    resultObject = converter.convertDD_XML(resultStream);
                    if (!isHttpRequest()) {
                        // resultObject.addConvertedXml(sourceFileName + ".xml",
                        // ((ByteArrayOutputStream) resultStream).toByteArray());
                        resultObject.addConvertedFile(sourceFileName + ".xml", tmpFileName);
                    }
                }
            }
        } catch (MalformedURLException mfe) {
            errorMessage = handleConversionException("Bad URL. ", mfe);
        } catch (IOException ioe) {
            errorMessage = handleConversionException("Error opening URL. ", ioe);
        } catch (Exception e) {
            errorMessage = handleConversionException("Error converting Excel file. ", e);
        } finally {
            IOUtils.closeQuietly(resultStream);
            Utils.deleteFile(file);
        }
        // Creates response Object, if error occurred
        if (errorMessage != null) {
            if (resultObject == null) {
                resultObject = new ConversionResultDto();
            }
            resultObject.setStatusCode(ConversionResultDto.STATUS_ERR_SYSTEM);
            resultObject.setStatusDescription(errorMessage);
            resultObject.addConversionLog(ConversionLogDto.ConversionLogType.CRITICAL, errorMessage, "System");
        }
        resultObject.setSourceUrl(sourceUrl);
        if (isHttpRequest()
                && (ConversionResultDto.STATUS_ERR_SYSTEM.equals(resultObject.getStatusCode()) || ConversionResultDto.STATUS_ERR_SCHEMA_NOT_FOUND
                        .equals(resultObject.getStatusCode()))) {
            throw new GDEMException(resultObject.getStatusDescription());
        }

        return resultObject;
    }

    /**
     * Returns source file as InputFile object.
     *
     * @param sourceUrl Source URL
     * @return Input File
     * @throws MalformedURLException If URL is malformed
     * @throws IOException IO Exception
     */
    private InputFile getInptFile(String sourceUrl) throws MalformedURLException, IOException {
        InputFile src = new InputFile(sourceUrl);
        src.setAuthentication(getTicket());
        src.setTrustedMode(isTrustedMode());

        return src;
    }

    /**
     * Get OutpuStram where to write the conversion result.
     *
     * @param outputFileName Output file name
     * @return OutputStream
     * @throws GDEMException If an error occurs
     */
    private OutputStream getResultOutputStream(String outputFileName) throws GDEMException {
        OutputStream resultStream = null;
        if (isHttpRequest()) {
            try {
                HttpMethodResponseWrapper httpResponse = getHttpResponse();
                httpResponse.setContentType("text/xml");
                httpResponse.setContentDisposition(outputFileName + ".xml");
                resultStream = httpResponse.getOutputStream();
            } catch (IOException e) {
                LOGGER.error("Error getting response outputstream ", e);
                throw new GDEMException("Error getting response outputstream " + e.toString(), e);
            }
        }
        return resultStream;
    }

    /**
     * Handle exceptions - throws Exception if the call is coming from web page, otherwise logs and returns error message.
     *
     * @param errorMessage Error message
     * @param e Exception
     * @return Error message
     * @throws GDEMException If an error occurs
     */
    private String handleConversionException(String errorMessage, Exception e) throws GDEMException {
        LOGGER.error(errorMessage, e);
        if (isHttpRequest()) {
            throw new GDEMException(errorMessage + e.getMessage(), e);
        } else {
            errorMessage = errorMessage + e.getMessage();
        }
        return errorMessage;
    }

    /**
     * Converts conversion result object into Hashtable that is used in XML-RPC method result.
     *
     * @param dto Result transfer object
     * @return Hash table with result
     * @throws GDEMException If an error occurs
     */
    public static final Hashtable<String, Object> convertExcelResult(ConversionResultDto dto) throws GDEMException {
        Hashtable<String, Object> result = new Hashtable<String, Object>();

        result.put("resultCode", dto.getStatusCode());
        result.put("resultDescription", dto.getStatusDescription());
        result.put("conversionLog", dto.getConversionLogAsHtml());
        Vector<Hashtable<String, Object>> convertedFiles = new Vector<Hashtable<String, Object>>();

        if (dto.getConvertedFiles() != null) {
            for (ConvertedFileDto convertedFileDto : dto.getConvertedFiles()) {
                Hashtable<String, Object> convertedFile = new Hashtable<String, Object>();
                convertedFile.put("fileName", convertedFileDto.getFileName());
                convertedFile.put("content", convertedFileDto.getFileContentAsByteArray());
                convertedFiles.add(convertedFile);
            }
        }
        result.put("convertedFiles", convertedFiles);
        return result;
    }

    /**
     * @return the checkSchemaValidity
     */
    public boolean isCheckSchemaValidity() {
        return checkSchemaValidity;
    }

    /**
     * @param checkSchemaValidity the checkSchemaValidity to set
     */
    public void setCheckSchemaValidity(boolean checkSchemaValidity) {
        this.checkSchemaValidity = checkSchemaValidity;
    }

}
