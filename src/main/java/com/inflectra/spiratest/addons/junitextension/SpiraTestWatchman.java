package com.inflectra.spiratest.addons.junitextension;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Vector;

import org.junit.rules.TestWatchman;
import org.junit.runners.model.FrameworkMethod;

public class SpiraTestWatchman extends TestWatchman {
	
	 
	static final String TEST_RUNNER_NAME = "JUnit";
	protected Vector<TestRun> testRunVector;
	protected boolean enabled = false;
	protected String url = null;
	protected String login = null;
	protected String password = null;
	private Integer projectId = -1;
	private Integer releaseId = -1;
	
	public SpiraTestWatchman() {
		this.testRunVector = new Vector<TestRun>();
		enabled = Boolean.parseBoolean(System.getProperty("EnableSpiraReporting", "false"));
		url = System.getProperty("SpiraUrl", "");
		login = System.getProperty("SpiraLogin", "");
		password = System.getProperty("SpiraPassword", "");

		try {
			projectId = Integer.parseInt(System.getProperty("SpiraProjectId", "-1"));
		} catch (NumberFormatException e) {
			System.out.println("SpiraProjectId argument needs To Be a Number");
			throw(e);
		}

		try {
			releaseId = Integer.parseInt(System.getProperty("SpiraReleaseId", "-1"));
		} catch (NumberFormatException e) {
			System.out.println("SpiraReleaseId argument needs To Be a Number");
			throw(e);
		}
	}
	
	 /**
	* Invoked when a test method succeeds
	*
	* @param method
	*/
	@Override
	public void succeeded(FrameworkMethod method) {
		if (enabled){
			TestRun newTestRun = new TestRun();
			newTestRun.message = "Test Passed";
			newTestRun.stackTrace = "";
			newTestRun.executionStatusId = 2;	//Passed
			newTestRun.testName = method.getName();
			newTestRun = populateTestRun (method, newTestRun, false);
			testRunVector.addElement (newTestRun);
		}
	}

	/**
	* Invoked when a test method fails
	*
	* @param e
	* @param method
	*/
	@Override
	public void failed(Throwable e, FrameworkMethod method) {
		if (enabled){
			//Extract the values out of the failure object
			String message = e.getMessage();
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			String stackTrace = writer.toString();
			if (message.equals(""))
			{
				message = "No Message Available";
			}
			TestRun newTestRun = new TestRun();
			newTestRun.message = message;
			newTestRun.stackTrace = stackTrace;
			newTestRun.executionStatusId = 1;	//Failed
			newTestRun.testName = method.getName();
			newTestRun = populateTestRun (method, newTestRun, true);
	
			testRunVector.addElement (newTestRun);
		}
	}

	/**
	* Invoked when a test method finishes (whether passing or failing)
	*
	* @param method
	*/
	@Override
	public void finished(FrameworkMethod method) {
		if (enabled){
			try {
				//Instantiate the web service proxy class
				SpiraTestExecute spiraTestExecute = new SpiraTestExecute();
	
				//Now we need to iterate through the vector and call the SpiraTest API to record the results
				//We need to record both passes and failures
				for (int i = 0; i < this.testRunVector.size(); i++) {
					TestRun testRun = (TestRun) this.testRunVector.elementAt(i);

					if (testRun.creationSuccessful) {
						//Get the current date/time
						Date now = new Date();
						spiraTestExecute.url = testRun.url;
						spiraTestExecute.userName = testRun.userName;
						spiraTestExecute.password = testRun.password;
						spiraTestExecute.projectId = testRun.projectId;
						spiraTestExecute.recordTestRun (
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
				}
			} catch(Exception e) {
				System.out.println(e);
			} 
		}
	}


	/**
	 * Populates the test thread object from the annotations associated with the test case (class and method)s
	 * 
	 * @param classAndMethod	The class and method name in the form 'method(class)'
	 * @param displayMessage	Should we display a message or not
	 * @param testRun		The test thread object to be populated
	 */
	protected TestRun populateTestRun (FrameworkMethod classAndMethod, TestRun testRun, boolean displayMessage)
	{
		try {
			//Extract the SpiraTest test case id - if present
			if (classAndMethod.getMethod().isAnnotationPresent(SpiraTestCase.class)) {
				SpiraTestCase methodAnnotation = classAndMethod.getMethod().getAnnotation (SpiraTestCase.class);
				testRun.testCaseId = methodAnnotation.testCaseId();
				if (displayMessage)	{
					System.out.println ("Matches SpiraTest test case id: " + testRun.testCaseId );
				}
			} else {
				System.out.println ("SpiraTest Annotation not Found on method '" + classAndMethod.getName() + "'!");
				throw new RuntimeException("SpiraTest Annotation not Found on method");
			}
			testRun = populateLoginAndProjectDetails(classAndMethod, testRun);
		} catch (RuntimeException e) {
			testRun.creationSuccessful = false;
		}
		return testRun;
	}

	private TestRun populateLoginAndProjectDetails(FrameworkMethod classAndMethod, TestRun testRun) throws RuntimeException{


		//Extract the SpiraTest configuration data - if present
		if (classAndMethod.getMethod().getDeclaringClass().isAnnotationPresent(SpiraTestConfiguration.class)) {
			SpiraTestConfiguration classAnnotation = classAndMethod.getMethod().getDeclaringClass().getAnnotation (SpiraTestConfiguration.class);
			if (url.isEmpty()) {
				testRun.url = classAnnotation.url();
			}else {
				testRun.url = url;
			}
			if (login.isEmpty()) {
				testRun.userName = classAnnotation.login();
			} else {
				testRun.userName = login;
			}
			if (password.isEmpty()){
				testRun.password = classAnnotation.password();
			} else {
				testRun.password = password;
			}
			if (projectId.equals(-1)){
				testRun.projectId = classAnnotation.projectId();
			}else{
				testRun.projectId = projectId;
			}
			if (releaseId.equals(-1)){
				testRun.releaseId = classAnnotation.releaseId();
			} else {
				testRun.releaseId = releaseId;
			}
			testRun.testSetId = classAnnotation.testSetId();
			
		} else {
			System.out.print ("SpiraTest Annotation not Found on class '" + classAndMethod.getMethod().getDeclaringClass() + "'!\n\n");
			throw new RuntimeException("SpiraTest Annotation not Found");
		}
		return testRun;
	}




}
