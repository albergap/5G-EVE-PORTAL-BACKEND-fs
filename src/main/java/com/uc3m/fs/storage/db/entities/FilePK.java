package com.uc3m.fs.storage.db.entities;

import java.io.Serializable;

import javax.persistence.Embeddable;

@Embeddable
public class FilePK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;

	private String uuid;

	private String owner;

	public FilePK() {
	}
	public FilePK(String uuid, String owner) {
		this.uuid = uuid;
		this.owner = owner;
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

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof FilePK)) {
			return false;
		}
		FilePK castOther = (FilePK)other;
		return 
				this.uuid.equals(castOther.uuid)
				&& this.owner.equals(castOther.owner);
	}

	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.uuid.hashCode();
		hash = hash * prime + this.owner.hashCode();

		return hash;
	}

	@Override
	public String toString() {
		return "FilePK [uuid=" + uuid + ", owner=" + owner + "]";
	}

}