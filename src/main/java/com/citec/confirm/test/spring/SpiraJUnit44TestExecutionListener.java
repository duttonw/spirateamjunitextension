package com.citec.confirm.test.spring;


import com.inflectra.spiratest.addons.junitextension.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Date;

public class SpiraJUnit44TestExecutionListener extends AbstractTestExecutionListener {

    private static final Logger logger = LoggerFactory.getLogger(SpiraJUnit44TestExecutionListener.class);

    private static final String TEST_RUNNER_NAME = "JUnit";

    private SpiraServerConfiguration systemSpiraServerConfig;
    final boolean spiraReportingEnabled;

    public SpiraJUnit44TestExecutionListener() {
        spiraReportingEnabled = Boolean.parseBoolean(System.getProperty("EnableSpiraReporting", "false"));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void afterTestMethod(TestContext testContext) throws Exception {

        if (spiraReportingEnabled) {
            Method testMethod = testContext.getTestMethod();
            Validate.notNull(testMethod, "The test method of the supplied TestContext must not be null");
            final SpiraServerConfiguration effectiveSpiraServerConfig = getEffectiveSpiraServerConfiguration(testMethod);
            try {
                //Instantiate the web service proxy class
                final SpiraTestExecute spiraTestExecute = new SpiraTestExecute();
                final TestRun testRun = createAndPopulateTestRun(testContext, effectiveSpiraServerConfig);
                final Date now = new Date();
                spiraTestExecute.url = effectiveSpiraServerConfig.getUrl();
                spiraTestExecute.userName = effectiveSpiraServerConfig.getLogin();
                spiraTestExecute.password = effectiveSpiraServerConfig.getPassword();
                spiraTestExecute.projectId = effectiveSpiraServerConfig.getProjectId();

                if (testRun.creationSuccessful) {
                    spiraTestExecute.recordTestRun(
                            -1,
                            testRun.testCaseId,
                            testRun.releaseId,
                            testRun.testSetId,
                            now,
                            now,
                            testRun.executionStatusId,
                            TEST_RUNNER_NAME,
                            testRun.testName,
                            1,
                            testRun.message,
                            testRun.stackTrace
                    );
                }

            } catch (Exception e) {
                System.out.println(e);
            }
        }

    }

    /**
     * Populates the test thread object from the annotations associated with the test case (class and method)s
     *
     * @param testContext The class and method name in the form 'method(class)'
     */
    protected TestRun createAndPopulateTestRun(TestContext testContext, SpiraServerConfiguration spiraServerConfiguration) {
        final Method testMethod = testContext.getTestMethod();
        final TestRun testRun = createTestRun(testContext, spiraServerConfiguration);

        try {
            //Extract the SpiraTest test case id - if present
            if (testMethod.isAnnotationPresent(SpiraTestCase.class)) {
                final SpiraTestCase methodAnnotation = testMethod.getAnnotation(SpiraTestCase.class);
                testRun.testCaseId = methodAnnotation.testCaseId();
                if (!isTestSuccessful(testContext)) {
                    System.out.println("Matches SpiraTest test case id: " + testRun.testCaseId);
                }
            } else {
                System.out.println("SpiraTest Annotation not Found on method '" + testMethod.getName() + "'!");
                throw new RuntimeException("SpiraTest Annotation not Found on method");
            }
        } catch (RuntimeException e) {
            testRun.creationSuccessful = false;
        }
        return testRun;
    }


    private SpiraServerConfiguration getEffectiveSpiraServerConfiguration(Method testMethod) {
        final SpiraServerConfiguration spiraServerConfiguration = SpiraServerConfiguration.create(createSystemSpiraServerConfig());
        if (spiraServerConfiguration.isEmpty()) {
            final SpiraTestConfiguration classAnnotation = testMethod.getDeclaringClass().getAnnotation(SpiraTestConfiguration.class);

            if (classAnnotation == null) {
                logger.error("In the absence of System properties for Spira Reporting you need to have the SpiraTestConfiguration annotation with values");
                return spiraServerConfiguration;
            }

            if (StringUtils.isBlank(spiraServerConfiguration.getLogin())) {
                spiraServerConfiguration.setLogin(classAnnotation.login());
            }
            if (StringUtils.isBlank(spiraServerConfiguration.getPassword())) {
                spiraServerConfiguration.setLogin(classAnnotation.password());
            }
            if (StringUtils.isBlank(spiraServerConfiguration.getUrl())) {
                spiraServerConfiguration.setLogin(classAnnotation.url());
            }
            if (spiraServerConfiguration.getReleaseId() != -1) {
                spiraServerConfiguration.setReleaseId(classAnnotation.releaseId());
            }

            if (spiraServerConfiguration.getProjectId() != -1) {
                spiraServerConfiguration.setProjectId(classAnnotation.projectId());
            }
        }
        return spiraServerConfiguration;
    }


    private SpiraServerConfiguration createSystemSpiraServerConfig() {

        if (systemSpiraServerConfig != null) {
            return systemSpiraServerConfig;
        }

        final String url = System.getProperty("SpiraUrl", "");
        final String login = System.getProperty("SpiraLogin", "");
        final String password = System.getProperty("SpiraPassword", "");

        Integer projectId = -1;
        Integer releaseId = -1;

        try {
            projectId = Integer.parseInt(System.getProperty("SpiraProjectId", "-1"));
        } catch (NumberFormatException e) {
            System.out.println("SpiraProjectId argument needs To Be a Number");
            throw (e);
        }

        try {
            releaseId = Integer.parseInt(System.getProperty("SpiraReleaseId", "-1"));
        } catch (NumberFormatException e) {
            System.out.println("SpiraReleaseId argument needs To Be a Number");
            throw (e);
        }

        systemSpiraServerConfig = new SpiraServerConfiguration().withLogin(login).withPassword(password).withProjectId(projectId).withReleaseId(releaseId).withUrl(url);

        return systemSpiraServerConfig;
    }

    private TestRun createTestRun(TestContext testContext, SpiraServerConfiguration spiraServerConfiguration) {
        final Method testMethod = testContext.getTestMethod();
        final TestRun testRun;

        if (isTestSuccessful(testContext)) {
            testRun = createSuccessFullTestRun(testMethod);
        } else {
            testRun = createFailureTestRun(testMethod, testContext.getTestException());
        }

        testRun.releaseId = spiraServerConfiguration.getReleaseId();
        testRun.testSetId = -1;
        return testRun;
    }

    private boolean isTestSuccessful(TestContext testContext) {
        return testContext.getTestException() == null;
    }

    private TestRun createSuccessFullTestRun(Method method) {
        TestRun newTestRun = new TestRun();
        newTestRun.message = "Test Passed";
        newTestRun.stackTrace = "";
        newTestRun.executionStatusId = 2;    //Passed
        newTestRun.testName = method.getName();
        return newTestRun;
    }

    private TestRun createFailureTestRun(Method method, Throwable testException) {
        String message = testException.getMessage();
        StringWriter writer = new StringWriter();
        testException.printStackTrace(new PrintWriter(writer));
        String stackTrace = writer.toString();
        if (message.equals("")) {
            message = "No Message Available";
        }
        TestRun newTestRun = new TestRun();
        newTestRun.message = message;
        newTestRun.stackTrace = stackTrace;
        newTestRun.executionStatusId = 1;    //Failed
        newTestRun.testName = method.getName();
        return newTestRun;
    }
}
