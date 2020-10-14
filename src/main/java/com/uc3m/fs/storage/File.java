package com.uc3m.fs.storage;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Entity(name = "files")
public class File {

	@EmbeddedId
	private FileId fileId;
	private String sites;

	public File() {
	}
	public File(FileId fileId, String sites) {
		this.fileId = fileId;
		this.sites = sites;
	}

	public String getUuid() {
		return fileId.uuid;
	}
	public void setUuid(String uuid) {
		this.fileId.uuid = uuid;
	}
	public String getOwner() {
		return fileId.owner;
	}
	public void setOwner(String owner) {
		this.fileId.owner = owner;
	}
	public String getSites() {
		return sites;
	}
	public void setSites(String sites) {
		this.sites = sites;
	}

	@Override
	public String toString() {
		return "File [uuid=" + fileId.uuid + ", owner=" + fileId.owner + ", sites=" + sites + "]";
	}

}