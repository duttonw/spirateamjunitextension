package com.inflectra.spiratest.addons.junitextension;

public class SpiraReportJob {
	private String url;
	private String action;
	private String method;
	private String report;

	public SpiraReportJob(
			String url,
			String action,
			String method,
			String report) {
		this.url = url;
		this.action = action;
		this.method = method;
		this.report = report;
	}

	public String getUrl() {
		return url;
	}

	public String getAction() {
		return action;
	}

	public String getMethod() {
		return method;
	}

	public String getReport() {
		return report;
	}
}
