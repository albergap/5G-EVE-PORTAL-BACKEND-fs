package com.uc3m.fs.storage.db.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class DeploymentRequestPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;

	@Column(insertable=false, updatable=false)
	private String uuid;

	@Column(insertable=false, updatable=false)
	private String owner;

	@Column(insertable=false, updatable=false)
	private String site;

	public DeploymentRequestPK() {
	}
	public DeploymentRequestPK(String uuid, String owner, String site) {
		this.uuid = uuid;
		this.owner = owner;
		this.site = site;
	}
	public String getUuid() {
		return this.uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getOwner() {
		return this.owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getSite() {
		return this.site;
	}
	public void setSite(String site) {
		this.site = site;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof DeploymentRequestPK)) {
			return false;
		}
		DeploymentRequestPK castOther = (DeploymentRequestPK)other;
		return 
				this.uuid.equals(castOther.uuid)
				&& this.owner.equals(castOther.owner)
				&& this.site.equals(castOther.site);
	}

	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.uuid.hashCode();
		hash = hash * prime + this.owner.hashCode();
		hash = hash * prime + this.site.hashCode();

		return hash;
	}

	@Override
	public String toString() {
		return "DeploymentRequestPK [uuid=" + uuid + ", owner=" + owner + ", site=" + site + "]";
	}

}