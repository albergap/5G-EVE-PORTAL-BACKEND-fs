package com.uc3m.fs.model;

import java.util.Date;

public class DeploymentRequestResponse {

	public String site, status;
	public Date date_request;

	public DeploymentRequestResponse(String site, String status, Date date_request) {
		this.site = site;
		this.status = status;
		this.date_request = date_request;
	}

	@Override
	public String toString() {
		return "DeploymentRequest [site=" + site + ", status=" + status + ", date_request=" + date_request + "]";
	}

}