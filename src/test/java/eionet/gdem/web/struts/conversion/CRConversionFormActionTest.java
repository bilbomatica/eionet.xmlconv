/*
 * Created on 23.04.2008
 */
package eionet.gdem.web.struts.conversion;

import java.util.List;

import servletunit.struts.MockStrutsTestCase;
import eionet.gdem.test.DbHelper;
import eionet.gdem.test.TestConstants;
import eionet.gdem.test.TestUtils;

/**
 * @author Enriko Käsper, TietoEnator Estonia AS CRConversionFormActionTest
 */

public class CRConversionFormActionTest extends MockStrutsTestCase {

    public CRConversionFormActionTest(String testName) {
        super(testName);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        setContextDirectory(TestUtils.getContextDirectory());
        setInitParameter("validating", "false");

        // setup database and Spring context
        DbHelper.setUpSpringContextWithDatabaseTester(TestConstants.SEED_DATASET_CONVERSIONS_XML);
    }

    /**
     * test if the form is successfully formwarding and stores the schemas list in session
     */
    public void testSuccessfulForward() {

        setRequestPathInfo("/crConversionForm");
        actionPerform();
        verifyForward("success");
        verifyInputTilesForward("/crConversion.jsp");
        verifyNoActionErrors();

        List schemasInSession = (List) request.getAttribute("conversion.schemas");
        assertTrue(schemasInSession.size() > 0);

    }

}
