package com.uc3m.fs.model;

public class DeploymentRequestResponse {

	public String site, status;

	public DeploymentRequestResponse(String site, String status) {
		this.site = site;
		this.status = status;
	}

	@Override
	public String toString() {
		return "DeploymentRequest [site=" + site + ", status=" + status + "]";
	}

}