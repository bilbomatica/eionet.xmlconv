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
 * The Original Code is XMLCONV.
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Enriko Käsper, Tieto Estonia
 */

package eionet.gdem.qa.engines;

import java.io.OutputStream;
import java.io.StringReader;
import java.util.Map;




import eionet.gdem.Constants;
import eionet.gdem.GDEMException;
import eionet.gdem.qa.XQScript;
import eionet.gdem.utils.InputFile;
import eionet.gdem.utils.Streams;
import eionet.gdem.utils.Utils;
import eionet.gdem.utils.system.SysCommandExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Enriko Käsper, Tieto Estonia SystemQueryEngineImpl
 */

public abstract class ExternalQueryEngine extends QAScriptEngineStrategy {
    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalQueryEngine.class);

    /**
     * Gets shell command
     * @param dataFile data file
     * @param scriptFile script file
     * @param params parameters
     * @return
     */
    protected abstract String getShellCommand(String dataFile, String scriptFile, Map<String, String> params);

    @Override
    protected void runQuery(XQScript script, OutputStream result) throws GDEMException {

        String tmpScriptFile = null;
        InputFile src = null;

        try {

            // build InputSource for xsl
            if (!Utils.isNullStr(script.getScriptSource())) {
                tmpScriptFile = Utils.saveStrToFile(null, script.getScriptSource(), "xgawk");
                script.setScriptFileName(tmpScriptFile);
            } else if (!Utils.isNullStr(script.getScriptFileName())) {
                // fisXsl=new FileInputStream(script.getScriptFileName());
            } else {
                throw new GDEMException("XQuery engine could not find script source or script file name!");
            }
            // Build InputSource for xml file
            src = new InputFile(script.getSrcFileUrl());
            String srcFile = src.saveSrcFile();

            InputFile srcOrigFile = new InputFile(script.getOrigFileUrl());
            Map<String, String> params = srcOrigFile.getCdrParams();
            params.put(Constants.XQ_SOURCE_PARAM_NAME, script.getOrigFileUrl());

            String cmd = getShellCommand(srcFile, script.getScriptFileName(), params);

            LOGGER.debug("Execute command: " + cmd);

            SysCommandExecutor cmdExecutor = new SysCommandExecutor();
            int exitStatus = cmdExecutor.runCommand(cmd);
            LOGGER.debug("Exit status: " + exitStatus);

            String cmdError = cmdExecutor.getCommandError();
            LOGGER.debug("Command error: " + cmdError);

            String cmdOutput = cmdExecutor.getCommandOutput();
            // _logger.debug("Command output: " + cmdOutput);
            boolean throwError = false;

            if (Utils.isNullStr(cmdOutput) && !Utils.isNullStr(cmdError)) {
                Streams.drain(new StringReader(cmdError), result);
                throwError = true;
            } else {
                Streams.drain(new StringReader(cmdOutput), result);
            }

            // clean tmp files
            if (tmpScriptFile != null) {
                Utils.deleteFile(tmpScriptFile);
            }
            if (srcFile != null) {
                Utils.deleteFile(srcFile);
            }
            if (throwError) {
                throw new GDEMException(cmdError);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("==== CATCHED EXCEPTION " + e.toString());
            throw new GDEMException(e.getMessage());
        } finally {
            try {
                result.close();
                result.flush();
            } catch (Exception e) {
                LOGGER.warn(e.getMessage());
            }
            if (src != null) {
                try {
                    src.close();

                } catch (Exception e) {
                }
            }
        }

    }
}
