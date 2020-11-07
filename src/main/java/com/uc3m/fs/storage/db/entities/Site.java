package com.uc3m.fs.storage.db.entities;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
@Entity
@Table(name="sites")
@NamedQuery(name="Site.findAll", query="SELECT s FROM Site s")
public class Site implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private String site;

	//bi-directional many-to-one association to DeploymentRequest
	@OneToMany(mappedBy="siteBean", fetch = FetchType.LAZY)
	private List<DeploymentRequest> deploymentRequests;

	public Site() {
	}

	public Site(String site) {
		this.site = site;
	}

	public String getSite() {
		return this.site;
	}

	public void setSite(String site) {
		this.site = site;
	}

	public List<DeploymentRequest> getDeploymentRequests() {
		return this.deploymentRequests;
	}

	public void setDeploymentRequests(List<DeploymentRequest> deploymentRequests) {
		this.deploymentRequests = deploymentRequests;
	}

	public DeploymentRequest addDeploymentRequest(DeploymentRequest deploymentRequest) {
		getDeploymentRequests().add(deploymentRequest);
		deploymentRequest.setSiteBean(this);

		return deploymentRequest;
	}

	public DeploymentRequest removeDeploymentRequest(DeploymentRequest deploymentRequest) {
		getDeploymentRequests().remove(deploymentRequest);
		deploymentRequest.setSiteBean(null);

		return deploymentRequest;
	}

	@Override
	public String toString() {
		return "Site [site=" + site + "]";
	}

}