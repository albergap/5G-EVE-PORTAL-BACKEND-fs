package com.uc3m.fs.storage;

import java.io.Serializable;

import javax.persistence.Embeddable;

@Embeddable
@SuppressWarnings("serial")
public class FileId implements Serializable {

	String uuid;
	String owner;

	public FileId() {
	}
	public FileId(String uuid, String owner) {
		this.uuid = uuid;
		this.owner = owner;
	}

}