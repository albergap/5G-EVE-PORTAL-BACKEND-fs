package com.uc3m.fs.storage;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "files")
public class File {

	@Id
	private String uuid;
	private String owner;
	private String sites;

	public File() {
	}
	public File(String uuid, String owner, String sites) {
		super();
		this.uuid = uuid;
		this.owner = owner;
		this.sites = sites;
	}

	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getSites() {
		return sites;
	}
	public void setSites(String sites) {
		this.sites = sites;
	}

	@Override
	public String toString() {
		return "File [uuid=" + uuid + ", owner=" + owner + ", sites=" + sites + "]";
	}

}