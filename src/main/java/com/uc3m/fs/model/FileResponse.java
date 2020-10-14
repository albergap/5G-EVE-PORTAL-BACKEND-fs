package com.uc3m.fs.model;

import java.util.ArrayList;

public class FileResponse {
	public String uuid, owner;
	public ArrayList<String> sites;

	public FileResponse(String uuid, String owner, ArrayList<String> sites) {
		this.uuid = uuid;
		this.owner = owner;
		this.sites = sites;
	}
}