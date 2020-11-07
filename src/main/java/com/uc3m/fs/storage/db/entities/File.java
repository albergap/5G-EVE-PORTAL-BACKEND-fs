package com.uc3m.fs.storage.db.entities;

import java.io.Serializable;
import java.util.List;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name="files")
@NamedQuery(name="File.findAll", query="SELECT f FROM File f")
public class File implements Serializable {
	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private FilePK id;

	//bi-directional many-to-one association to DeploymentRequest
	@OneToMany(mappedBy="file", fetch = FetchType.EAGER)
	private List<DeploymentRequest> deploymentRequests;

	public File() {
	}
	public File(String uuid, String owner) {
		id = new FilePK(uuid, owner);
	}

	public String getOwner() {
		return this.id.getOwner();
	}

	public String getUuid() {
		return this.id.getUuid();
	}

	public FilePK getId() {
		return this.id;
	}

	public void setId(FilePK id) {
		this.id = id;
	}

	public List<DeploymentRequest> getDeploymentRequests() {
		return this.deploymentRequests;
	}

	public void setDeploymentRequests(List<DeploymentRequest> deploymentRequests) {
		this.deploymentRequests = deploymentRequests;
	}

	public DeploymentRequest addDeploymentRequest(DeploymentRequest deploymentRequest) {
		getDeploymentRequests().add(deploymentRequest);
		deploymentRequest.setFile(this);

		return deploymentRequest;
	}

	public DeploymentRequest removeDeploymentRequest(DeploymentRequest deploymentRequest) {
		getDeploymentRequests().remove(deploymentRequest);
		deploymentRequest.setFile(null);

		return deploymentRequest;
	}

	@Override
	public String toString() {
		return "File [uuid=" + id.getUuid() + ", owner=" + id.getOwner() + "]";
	}

}