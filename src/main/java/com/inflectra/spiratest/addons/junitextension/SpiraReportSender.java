package com.inflectra.spiratest.addons.junitextension;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Vector;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SpiraReportSender {
	private static SpiraReportSender instance;
	private static Vector<SpiraReportJob> jobQueue = null;

	static {
		instance = new SpiraReportSender();
		jobQueue = new Vector<SpiraReportJob>();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				SpiraReportJob job = null;
				int reportCount = 0; 
				int reportNotSubmittedCount = 0;
				while (!jobQueue.isEmpty()) {
					job = jobQueue.firstElement();
					jobQueue.remove(job);
					
					String returnValue = getInstance().sendSpiraReport(
							job.getUrl(),
							job.getAction(),
							job.getMethod(),
							job.getReport());
					if (returnValue.contains("Error")){
						++reportNotSubmittedCount;
					} else {
						++reportCount;
					}
				}

				System.out.println("" + reportCount + " report[s] submitted");
				System.out.println("" + reportNotSubmittedCount + " report[s] NOT submitted");
			}
		});
	}

	public static SpiraReportSender getInstance() {
		return instance;
	}

	public void addJob(SpiraReportJob job) {
		jobQueue.add(job);
	}

	private String sendSpiraReport(
			final String spiraUrl,
			final String spiraAction,
			final String spiraMethod,
			final String spiraReport) {
		URL url = null;
		HttpURLConnection connection = null;
		String retval = null;

		try {
			url = new URL(spiraUrl);
			connection = (HttpURLConnection) url.openConnection();

			if (url.getProtocol().equals(SpiraTestExecute.HTTPS_PROTOCOL)) {
				connection = setupSSLTrustAll(connection);
			}

			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Host", url.getHost());
			connection.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
			connection.setRequestProperty("Content-Length", String.valueOf(spiraReport.length()));
			connection.setRequestProperty("SOAPAction", "\"" + spiraAction + "\"");

			OutputStream out = connection.getOutputStream();
			PrintWriter writer = new PrintWriter(out, true);
			writer.print(spiraReport);
			writer.flush();

			int response = connection.getResponseCode();
			if (response == 500){
				return "Error: Connection 500 Returned";
			}
			InputStream in = connection.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));

			// Read the response from the server ... times out if the response
			// takes more than 10 seconds
			String inputLine;
			StringBuffer sb = new StringBuffer(1000);

			int wait_seconds = 10; // change if you find that your server is too slow
			boolean timeout = false;
			long m = System.currentTimeMillis();

			while ((inputLine = reader.readLine()) != null && !timeout) {
				sb.append(inputLine + "\n");

				if ((System.currentTimeMillis() - m) > (1000 * wait_seconds))
					timeout = true;
			}

			reader.close();

			// The StringBuffer sb now contains the complete result from the
			// webservice in XML format. You can parse this XML if you want to
			// get more complicated results than a single value.

			if (!timeout) {
				// Locate the return parameter
				String returnparam = spiraMethod + "Result";
				int start = sb.toString().indexOf("<" + returnparam + ">")
													+ returnparam.length() + 2;
				int end = sb.toString().indexOf("</" + returnparam + ">");

				// If we have no return value, display the error message
				if (end == -1) {
					// Check to see if we have a mismatching namespace
					if (sb.toString().indexOf("host did not recognize the value of HTTP Header SOAPAction") != -1) {
						System.out.println("The version of the web service being connected to does not match\n");
					} else {
						// Print out the error message for other errors
						System.out.println(sb.toString());
					}

					retval = "";
				} else {
					// Extract a single return parameter
					retval = sb.toString().substring(start, end);
				}
			} else {
				retval = "Error: response timed out.";
			}

			connection.disconnect();
		} catch (Throwable t) {
			t.printStackTrace();
			throw new RuntimeException("couldn't send report");
		}

		return retval;
	}
	
	private HttpURLConnection setupSSLTrustAll(HttpURLConnection urlCon) {
		try {
		    // Create a trust manager that does not validate certificate chains
		    final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				public void checkServerTrusted(X509Certificate[] arg0, String arg1)
						throws CertificateException {}
				public void checkClientTrusted(X509Certificate[] arg0, String arg1)
						throws CertificateException {}
		    }};    
		    // Install the all-trusting trust manager
		    final SSLContext sslContext = SSLContext.getInstance( "SSL" );
		    sslContext.init( null, trustAllCerts, new java.security.SecureRandom() );
		    // Create an ssl socket factory with our all-trusting manager
		    final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
		    // Tell the url connection object to use our socket factory which bypasses security checks
		    ( (HttpsURLConnection) urlCon ).setSSLSocketFactory( sslSocketFactory );  
		} catch ( final Exception e ) {
		    e.printStackTrace();
		}

		return urlCon;
	}
}
