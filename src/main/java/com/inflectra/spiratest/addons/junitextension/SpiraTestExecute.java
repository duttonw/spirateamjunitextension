package com.inflectra.spiratest.addons.junitextension;

import java.util.*;
import java.text.*;


/**
 * This defines the 'SpiraTestExecute' class that provides the Java facade
 * for calling the SOAP web service exposed by SpiraTest
 * (Current implementation doesn't support SSL)
 * 
 * @author		Inflectra Corporation
 * @version		2.3.0
 *
 */
public class SpiraTestExecute {
	public static final String HTTP_PROTOCOL = "http"; 
	public static final String HTTPS_PROTOCOL = "https"; 
	private static final String HTTPS_PROTOCOL_PREFIX = HTTPS_PROTOCOL + "://";
	private static final String HTTP_PROTOCOL_PREFIX = HTTP_PROTOCOL + "://";
	static final String WEB_SERVICE_NAMESPACE = "http://www.inflectra.com/SpiraTest/Services/v2.2/";
	static final String WEB_SERVICE_URL_SUFFIX = "/Services/v2_2/ImportExport.asmx";

	public String url;
	public String userName;
	public String password;
	public int projectId;

	/**
	 * Records a test run
	 * 
		@param testerUserId			The user id of the person who's running the test (-1 for logged in user)
		@param testCaseId			The test case being executed
		@param releaseId			The release being executed against (optional)
		@param testSetId			The test set being executed against (optional)
		@param executionStatusId	The status of the test run (pass/fail/not run)
		@param runnerName			The name of the automated testing tool
		@param runnerTestName		The name of the test as stored in JUnit
		@param runnerAssertCount	The number of assertions
		@param runnerMessage		The failure message (if appropriate)
		@param runnerStackTrace		The error stack trace (if any)
		@param endDate				When the test run ended
		@param startDate			When the test run started
	 */
	public void recordTestRun(
			int testerUserId,
			int testCaseId,
			int releaseId,
			int testSetId,
			Date startDate,
			Date endDate,
			int executionStatusId,
			String runnerName,
			String runnerTestName,
			int runnerAssertCount,
			String runnerMessage,
			String runnerStackTrace) {
		String fullUrl = url;
		String protocol = null;
		String host = null;
		String port = null;
		String path = null;

		if (!WEB_SERVICE_URL_SUFFIX.startsWith("/")
		&&  !url.endsWith("/")) {
			url += "/";
		}

		fullUrl = url + WEB_SERVICE_URL_SUFFIX;

		if (fullUrl.startsWith(HTTP_PROTOCOL_PREFIX)) {
			protocol = "http";
			host = fullUrl.replaceFirst(HTTP_PROTOCOL_PREFIX, "");
			port = "80";
		} else if (fullUrl.startsWith(HTTPS_PROTOCOL_PREFIX)) {
			protocol = "https";
			host = fullUrl.replaceFirst(HTTPS_PROTOCOL_PREFIX, "");
			port = "443";
		} else {
			throw new RuntimeException("unrecognised protocol in url: " + fullUrl
									 + "please use either 'http://...' or 'https://...'");
		}

		int colonIndex = host.indexOf(':');
		int slashIndex = host.indexOf('/');

		if ((colonIndex > 0)
		&&  ((slashIndex < 0) || (colonIndex < slashIndex))) {
			port = host.substring(colonIndex + 1,
					(slashIndex < 0) ? host.length()
							         : slashIndex);

			if ((port.length() < 1)
			&&  (port.replaceAll("[^0-9]*", "").length() < 1)) {
				throw new RuntimeException("unrecognised port number in url: " + fullUrl
					       + "please use '<protocol>://<host_name>:<port_number>/...'");
			}

			host.replaceFirst(":" + port, "");
			slashIndex = host.indexOf('/');
		}

		if (slashIndex > 0) {
			path = host.substring(slashIndex);
			host = host.substring(0, slashIndex);
		}

		//Format the dates into YYYY-MM-DDTHH:MM:SS format required by SOAP
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		String startDateSerialized = dateFormat.format (startDate);
		String endDateSerialized = dateFormat.format (endDate);

		//Instantiate the new SoapRequester class and populate
		SoapRequestBuilder soapRequest = new SoapRequestBuilder();
		soapRequest.protocol = protocol;
		soapRequest.host = host;
		soapRequest.port = Integer.valueOf(port);
		soapRequest.methodName = "TestRun_RecordAutomated2";	//Sessionless version of API method
		soapRequest.xmlNamespace = WEB_SERVICE_NAMESPACE;
		soapRequest.path = path;
		soapRequest.soapAction = soapRequest.xmlNamespace + soapRequest.methodName;
		soapRequest.addParameter("userName", userName);
		soapRequest.addParameter("password", password);
		soapRequest.addParameter("projectId", Integer.toString(projectId));
		soapRequest.addParameter("testerUserId", Integer.toString(testerUserId));
		soapRequest.addParameter("testCaseId", Integer.toString(testCaseId));

		if (releaseId != -1) {
			soapRequest.addParameter("releaseId", Integer.toString(releaseId));
		}

		if (testSetId != -1) {
			soapRequest.addParameter("testSetId", Integer.toString(testSetId));
		}

		soapRequest.addParameter("startDate", startDateSerialized);
		soapRequest.addParameter("endDate", endDateSerialized);
		soapRequest.addParameter("executionStatusId", Integer.toString(executionStatusId));
		soapRequest.addParameter("runnerName", runnerName);
		soapRequest.addParameter("runnerTestName", runnerTestName);
		soapRequest.addParameter("runnerAssertCount", Integer.toString(runnerAssertCount));
		soapRequest.addParameter("runnerMessage", runnerMessage);
		soapRequest.addParameter("runnerStackTrace", runnerStackTrace);

		//Send the request and capture the response (should be the test run id)
		soapRequest.sendRequest();
	}
}