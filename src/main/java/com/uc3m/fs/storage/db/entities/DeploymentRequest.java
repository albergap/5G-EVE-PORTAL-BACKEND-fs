package com.uc3m.fs.storage.db.entities;

import java.io.Serializable;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name="deployment_requests")
@NamedQuery(name="DeploymentRequest.findAll", query="SELECT d FROM DeploymentRequest d")
public class DeploymentRequest implements Serializable {
	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private DeploymentRequestPK id;

	private String status;

	//bi-directional many-to-one association to File
	@ManyToOne
	@JoinColumns({
		@JoinColumn(name="owner", referencedColumnName="owner", insertable = false, updatable = false),
		@JoinColumn(name="uuid", referencedColumnName="uuid", insertable = false, updatable = false)
	})
	private File file;

	//bi-directional many-to-one association to Site
	@ManyToOne
	@JoinColumn(name="site", insertable = false, updatable = false)
	private Site siteBean;

	public DeploymentRequest() {
	}
	public DeploymentRequest(DeploymentRequestPK id, File file, Site site, String status) {
		this.id = id;
		this.file = file;
		this.siteBean = site;
		this.status = status;
	}

	public DeploymentRequestPK getId() {
		return this.id;
	}

	public void setId(DeploymentRequestPK id) {
		this.id = id;
	}

	public String getStatus() {
		return this.status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public File getFile() {
		return this.file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public Site getSiteBean() {
		return this.siteBean;
	}

	public String getSite() {
		return id.getSite();
	}

	public void setSiteBean(Site siteBean) {
		this.siteBean = siteBean;
	}

	@Override
	public String toString() {
		return "DeploymentRequest [id=" + id + ", status=" + status + ", file=" + file + ", siteBean=" + siteBean + "]";
	}

}