package com.uc3m.fs.model;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FileResponse {

	public String uuid, owner;
	@JsonProperty("deployment_requests")
	public ArrayList<DeploymentRequestResponse> deploymentRequests;

	public FileResponse() {
	}

	public FileResponse(String uuid, String owner, ArrayList<DeploymentRequestResponse> deploymentRequests) {
		this.uuid = uuid;
		this.owner = owner;
		this.deploymentRequests = deploymentRequests;
	}

	@Override
	public String toString() {
		return "FileResponse [uuid=" + uuid + ", owner=" + owner + ", deploymentRequests=" + deploymentRequests + "]";
	}

}