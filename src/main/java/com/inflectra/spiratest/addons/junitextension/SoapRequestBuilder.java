package com.inflectra.spiratest.addons.junitextension;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;



/**
 * This forms the SOAP packet to communicate with the SpiraTest web service
 * 
 * @author David S Hobbs (http://www.codeproject.com/soap/WSfromJava.asp)
 * @version 2.3.0 Modified by Inflectra Corporation to avoid need to hard-code
 *          content-length
 */
class SoapRequestBuilder {
	public String protocol;
	public String host = "";
	public int port = 80;
	public String path = "";
	public String soapAction = "";
	public String methodName = "";
	public String xmlNamespace = "";

	private Vector<String> ParamNames = new Vector<String>();
	private Vector<String> ParamData = new Vector<String>();

	/**
	 * Adds a parameter to the SOAP Request
	 * 
	 * @param Name
	 *            the name of the parameter
	 * @param Data
	 *            the value of the parameter
	 */
	public void addParameter(String Name, String Data) {
		ParamNames.addElement(Name);
		ParamData.addElement(escape(Data));
	}

	/**
	 * Sends the SOAP Request to the web service
	 * 
	 * @return The value (if any) returned from the web service
	 */
	public String sendRequest() {
		URL url = null;
		String retval = "";

		try {
			url = new URL(protocol + "://" + host + ":" + port + path);
		} catch (MalformedURLException ex) {
			return "Error: " + ex.getMessage();
		}

		// Create the SOAP message payload
		StringBuffer body = new StringBuffer();

		body.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
		body.append("<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n");
		body.append("<soap:Body>\n");
		body.append("<" + methodName + " xmlns=\"" + xmlNamespace
				+ "\">\n");

		// Parameters passed to the method are added here
		for (int t = 0; t < ParamNames.size(); t++) {
			String name = (String) ParamNames.elementAt(t);
			String data = (String) ParamData.elementAt(t);

			if (data.equals("")) {
				body.append("<" + name + "/>\n");
			} else {
				body.append("<" + name + ">" + data + "</" + name + ">\n");
			}
		}

		body.append("</" + methodName + ">\n");
		body.append("</soap:Body>\n");
		body.append("</soap:Envelope>");

		try {
			SpiraReportSender.getInstance().addJob(
					new SpiraReportJob(
							url.toString(),
							soapAction,
							methodName,
							body.toString()));
		} catch (Exception ex) {
			return ("Error: cannot communicate (" + ex + ")");
		}

		return retval;
	}

	/**
	 * Escapes XML reserved characters (assumes UTF-8 or UTF-16 as encoding)
	 * 
	 * @param content
	 *            The content to be escaped
	 * @return The escaped string
	 */
	protected String escape(String content) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < content.length(); i++) {
			char c = content.charAt(i);
			if (c == '<') {
				buffer.append(";&lt;");
			} else if (c == '>') {
				buffer.append(";&gt;");
			} else if (c == '&') {
				buffer.append(";&amp;");
			} else if (c == '"') {
				buffer.append(";&quot;");
			} else if (c == '\'') {
				buffer.append(";&apos;");
			} else if (c == 0x00 || c == 0x01 || c == 0x02 || c == 0x03
					|| c == 0x04 || c == 0x05 || c == 0x06 || c == 0x07
					|| c == 0x08 || c == 0x0B || c == 0x0C || c == 0x0E
					|| c == 0x0F || c == 0x10 || c == 0x11 || c == 0x12
					|| c == 0x13 || c == 0x14 || c == 0x15 || c == 0x16
					|| c == 0x17 || c == 0x18 || c == 0x19 || c == 0x1A
					|| c == 0x1B || c == 0x1C || c == 0x1D || c == 0x1E
					|| c == 0x1F) {
				// ignore such control characters
			} else {
				buffer.append(c);
			}
		}
		return buffer.toString();
	}
}
