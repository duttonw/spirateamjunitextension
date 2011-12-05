package com.inflectra.spiratest.addons.junitextension;

import org.junit.runner.notification.*;
import org.junit.runner.*;

import java.lang.reflect.*;   
import java.util.*;                           

/**
 * This defines the 'SpiraTestConfiguration' annotation used to specify the authentication,
 * project and release information for the test being executed
 * 
 * @author		Inflectra Corporation
 * @version		2.3.0
 *
 */
public class SpiraTestListener extends RunListener
{
	static final String TEST_RUNNER_NAME = "JUnit";

	protected Vector<TestRun> testRunVector;

	/**
	 * Logs a failure with SpiraTest whenever a unit test fails
	 * 
	 * @param failure	JUnit Failure object that describes the failed test
	 */
	public void testFailure(Failure failure)
	{
		//Extract the values out of the failure object
		String message = failure.getMessage();
		String stackTrace = failure.getTrace();
		String classAndMethod = failure.getTestHeader();

		//Handle the empty string case
		if (message.equals(""))
		{
			message = "No Message Available";
		}

		System.out.print ("Test Failed\n");
		System.out.print (message + "\n");
		System.out.print ("Stack Trace:\n" + stackTrace + "\n");

		//Create a new test run
		TestRun newTestRun = new TestRun();
		newTestRun.message = message;
		newTestRun.stackTrace = stackTrace;
		newTestRun.executionStatusId = 1;	//Failed
		newTestRun.testName = classAndMethod;

		//Populate the Test Run from the meta-data derived from 
		//the class/method name combination
		populateTestRun (classAndMethod, newTestRun, true);

		//See if this test case is already in the vector
		//since we cannot guarentee whether the testFailure or testFinished method is called first
		boolean found = false;
		for (int i = 0; i < testRunVector.size(); i++)
		{
			TestRun testRun = (TestRun) this.testRunVector.elementAt(i);
			if (testRun.testName.equals (newTestRun.testName))
			{
				//We simply need to update the item as a failure and add the appropriate data
				testRun.message = newTestRun.message;
				testRun.stackTrace = newTestRun.stackTrace;
				testRun.executionStatusId = newTestRun.executionStatusId;
				found = true;
			}
		}

		//Add it to the vector if we didn't find a match
		if (! found)
		{
			testRunVector.addElement (newTestRun);
		}
	}

	/**
	 * Logs an event with SpiraTest whenever a unit test is finished
	 * 
	 * @param description	JUnit Description object that describes the test just run
	 */
	public void testFinished (Description description)
	{
		//Extract the values out of the description object
		String classAndMethod = description.getDisplayName();

		//Add an entry to the list of test runs
		TestRun newTestRun = new TestRun();
		newTestRun.message = "Test Passed";
		newTestRun.stackTrace = "";
		newTestRun.executionStatusId = 2;	//Passed
		newTestRun.testName = description.getDisplayName();
		
		//Populate the Test Run from the meta-data derived from 
		//the class/method name combination
		populateTestRun (classAndMethod, newTestRun, false);

		//See if this test case is already in the vector
		//since we cannot guarentee whether the testFailure or testFinished method is called first
		boolean found = false;
		for (int i = 0; i < testRunVector.size(); i++)
		{
			TestRun testRun = (TestRun) this.testRunVector.elementAt(i);
			if (testRun.testName.equals(newTestRun.testName))
			{
				//Since a failure overrides a success, we can leave the data alone
				//and just mark it as found, to prevent the duplicate addition to the vector
				found = true;
			}
		}

		//Add it to the vector if we didn't find a match
		if (! found)
		{
			testRunVector.addElement (newTestRun);
		}
	}

	/**
	 * Called when the test run is started for a fixture
	 * 
	 * @param description	JUnit Description object that describes the tests to be run
	 */
	public void testRunStarted (Description description)
	{
		//Create a new vector of test runs
		this.testRunVector = new Vector<TestRun>();

		System.out.print ("Starting test run...\n\n");
	}

	/**
	 * Called when the test run is finished for a fixture
	 * 
	 * @param result	The summary of the test run, including all the tests that failed 
	 */
	public void testRunFinished (Result result) {
		System.out.print ("Test run finished with " + result.getFailureCount() + " Failures.\n\n");

		try {
			//Instantiate the web service proxy class
			SpiraTestExecute spiraTestExecute = new SpiraTestExecute();

			//Now we need to iterate through the vector and call the SpiraTest API to record the results
			//We need to record both passes and failures
			for (int i = 0; i < this.testRunVector.size(); i++) {
				TestRun testRun = (TestRun) this.testRunVector.elementAt(i);
			
				//Get the current date/time
				Date now = new Date();

				//Populate the web service proxy with the connection info, then execute the API method
				spiraTestExecute.url = testRun.url;
				spiraTestExecute.userName = testRun.userName;
				spiraTestExecute.password = testRun.password;
				spiraTestExecute.projectId = testRun.projectId;
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
		} catch(Exception e) {
			System.out.println(e);
		} 
	}

	/**
	 * Populates the test run object from the annotations associated with the test case (class and method)s
	 * 
	 * @param classAndMethod	The class and method name in the form 'method(class)'
	 * @param displayMessage	Should we display a message or not
	 * @param testRun		The test run object to be populated
	 */
	protected void populateTestRun (String classAndMethod, TestRun testRun, boolean displayMessage)
	{
		//Get the test class and test method names separately
		//The header contains "method(class)"
		String [] classAndMethodArray = classAndMethod.split ("[()]");
		String methodName = classAndMethodArray [0];
		String className = classAndMethodArray [1];

		//Now try and extract the metadata from the test case
		try
		{
			//Get a handle to the class and method
			Class<?> testClass = Class.forName(className);
			Method testMethod = testClass.getMethod(methodName);

			//Extract the SpiraTest test case id - if present
			if (testMethod.isAnnotationPresent(SpiraTestCase.class))
			{
				SpiraTestCase methodAnnotation = testMethod.getAnnotation (SpiraTestCase.class);
				testRun.testCaseId = methodAnnotation.testCaseId();
				if (displayMessage)
				{
					System.out.print ("Matches SpiraTest test case id: " + testRun.testCaseId + "\n\n");
				}
			}
			else
			{
				System.out.print ("SpiraTest Annotation not Found on method '" + methodName + "'!\n\n");
			}

			//Extract the SpiraTest configuration data - if present
			if (testClass.isAnnotationPresent(SpiraTestConfiguration.class))
			{
				SpiraTestConfiguration classAnnotation = testClass.getAnnotation (SpiraTestConfiguration.class);
				testRun.url = classAnnotation.url();
				testRun.userName = classAnnotation.login();
				testRun.password = classAnnotation.password();
				testRun.projectId = classAnnotation.projectId();
				testRun.releaseId = classAnnotation.releaseId();
				testRun.testSetId = classAnnotation.testSetId();
			}
			else
			{
				System.out.print ("SpiraTest Annotation not Found on class '" + className + "'!\n\n");
			}
		}
		catch(NoSuchMethodException e)
		{
			System.out.println(e);
		} 
		catch(ClassNotFoundException e)
		{
			System.out.println(e);
		} 
	}
}