package com.uc3m.fs.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.uc3m.fs.storage.db.entities.DeploymentRequest;
import com.uc3m.fs.storage.db.entities.File;

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

	public FileResponse(File file) {
		uuid = file.getUuid();
		owner = file.getOwner();
		List<DeploymentRequest> dr = file.getDeploymentRequests();
		deploymentRequests = new ArrayList<DeploymentRequestResponse>(dr.size());
		for (DeploymentRequest d : dr)
			deploymentRequests.add(new DeploymentRequestResponse(d.getSite(), d.getStatus()));
	}

	@Override
	public String toString() {
		return "FileResponse [uuid=" + uuid + ", owner=" + owner + ", deploymentRequests=" + deploymentRequests + "]";
	}

}