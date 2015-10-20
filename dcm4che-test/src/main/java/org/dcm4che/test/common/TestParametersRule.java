/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2011
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
package org.dcm4che.test.common;

import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.util.Arrays;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.dcm4che.test.annotations.TestLocalConfig;
import org.dcm4che.test.annotations.TestParamDefaults;
import org.dcm4che.test.annotations.markers.Heavy;
import org.dcm4che.test.annotations.markers.ReportedIssue;
import org.dcm4che.test.image.ImageAssert;
import org.dcm4che.test.utils.ConfigUtils;
import org.dcm4che.test.utils.DBUtils;
import org.junit.Assert;
import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hesham elbadawi <bsdreko@gmail.com>
 * @author Roman K
 */

public class TestParametersRule implements TestRule {

    private static final Logger log = LoggerFactory.getLogger(TestParametersRule.class);

    private final BasicTest parametrizedTest;

    public TestParametersRule(BasicTest basicTest) {
        this.parametrizedTest = basicTest;
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {

                boolean isHeavy = hasAnnotation(description, Heavy.class);
                ReportedIssue reportedIssue = getAnnotation(description, ReportedIssue.class);
                boolean hasReportedIssue = reportedIssue != null;

                boolean skipHeavyTests = System.getProperty("org.dcm4che.test.skipHeavyTests") != null;
                boolean onlyHeavyTests = System.getProperty("org.dcm4che.test.onlyHeavyTests") != null;
                boolean skipReportedIssueTests = System.getProperty("org.dcm4che.test.skipReportedIssueTests") != null;
                boolean onlyReportedIssueTests = System.getProperty("org.dcm4che.test.onlyReportedIssueTests") != null;

                if (isHeavy && skipHeavyTests) {
                    log.info("Skipping Heavy Test {} {}", description.getTestClass().getName(), description.getMethodName());
                    throw new AssumptionViolatedException("Skipping Heavy Test " + description.getTestClass().getName() + " " + description.getMethodName());
                }

                if (hasReportedIssue && skipReportedIssueTests) {
                    log.info("Skipping Test with Reported Issue {}: {} {}", Arrays.toString(reportedIssue.value()), description.getTestClass().getName(), description.getMethodName());
                    throw new AssumptionViolatedException("Skipping Test with Reported Issue " + Arrays.toString(reportedIssue.value()) + ": " + description.getTestClass().getName() + " " + description.getMethodName());
                }

                if (onlyHeavyTests && !isHeavy) {
                    log.info("Skipping Non-Heavy Test {} {}", description.getTestClass().getName(), description.getMethodName());
                    throw new AssumptionViolatedException("Skipping Non-Heavy Test " + description.getTestClass().getName() + " " + description.getMethodName());
                }

                if (onlyReportedIssueTests && !hasReportedIssue) {
                    log.info("Skipping Non-ReportedIssue Test {} {}", description.getTestClass().getName(), description.getMethodName());
                    throw new AssumptionViolatedException("Skipping Non-ReportedIssue Test " + description.getTestClass().getName() + " " + description.getMethodName());
                }

                log.info("\n\n------------------------------------ \n" +
                             "Running {}({}) \n" +
                             "------------------------------------ \n\n",
                        description.getMethodName(),
                        description.getTestClass().getName());

                if (reportedIssue != null) {
                    log.info("Issue(s) reported for this test: {}", Arrays.toString(reportedIssue.value()));
                }

                Method method = getTestMethod(description);
                getInstance().clearParams();
                for (Annotation anno : method.getAnnotations()) {
                    Class<? extends Annotation> annoType = anno.annotationType();
                    getInstance().addParam(annoType.getSimpleName(), method.getAnnotation(annoType));
                }
                TestLocalConfig cnf = description.getTestClass().getAnnotation(TestLocalConfig.class);
                getInstance().addParam("defaultLocalConfig", cnf);
                TestParamDefaults props = description.getTestClass().getAnnotation(TestParamDefaults.class);
                getInstance().addParam("defaultParams", props);

                getInstance().init(description.getMethodName());

                ImageAssert.setFailureFilePath(getInstance().getBaseTemporaryDirectory().resolve("failureFiles"));

                // clean the temporary directory before each test
                Path tmpDir = getInstance().getBaseTemporaryDirectory();
                FileUtils.cleanDirectory(tmpDir.toFile());

                // Clean DB before each test
                Assert.assertTrue("Database could not be cleaned successfully.", DBUtils.cleanDB(getInstance()));

                // Reset config before each test
                ConfigUtils.restoreConfig(getInstance());

                notifyServerOfNewtest(description.getMethodName() + "(" + description.getTestClass().getName() + ")");

                base.evaluate();
            }
        };
    }

    public BasicTest getInstance() {
        return this.parametrizedTest;
    }

    protected void notifyServerOfNewtest(String testName) {
        String baseURL = getInstance().getProperties().getProperty("remoteConn.url");
        Client client = ClientBuilder.newBuilder().build();

        // seems server/JAX-RS doesn't like slashes, even if they are URL encoded
        String testNameEncoded = testName.replace('/', '|');
        try {
            testNameEncoded = URLEncoder.encode(testNameEncoded, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e); // will not happen
        }

        WebTarget target = client.target(baseURL + "/dcm4chee-arc-test/beginTest/" + testNameEncoded);
        Response rsp = target.request().build("GET").invoke();
        Assert.assertEquals(Response.Status.OK.getStatusCode(), rsp.getStatus());
    }

    private Method getTestMethod(Description description) throws NoSuchMethodException {
        String methodName = description.getMethodName();
        int parametersIndex = methodName.indexOf('[');
        if(parametersIndex != -1)
            methodName = methodName.substring(0, parametersIndex);
        return description.getTestClass().getMethod(methodName);
    }

    private <A extends Annotation> A getAnnotation(Description description, Class<A> annotationClass) throws NoSuchMethodException {
        A annotation = getTestMethod(description).getAnnotation(annotationClass);
        if (annotation == null)
            annotation = description.getTestClass().getAnnotation(annotationClass);
        return annotation;
    }

    private boolean hasAnnotation(Description description, Class<? extends Annotation> annotationClass) throws NoSuchMethodException {
        return getAnnotation(description, annotationClass) != null;
    }

}

