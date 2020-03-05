package com.uc3m.fs.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FileUploadRequest {

	@NotNull
	@NotBlank
	private String dzuuid;
	@NotNull
	@JsonProperty("List<site>")
	private String sites;
	@NotNull
	private int dzchunkindex, dztotalfilesize, dzchunksize, dztotalchunkcount, dzchunkbyteoffset;

	public FileUploadRequest(String dzuuid, String sites, int dzchunkindex, int dztotalfilesize, int dzchunksize,
			int dztotalchunkcount, int dzchunkbyteoffset) {
		this.dzuuid = dzuuid;
		this.sites = sites;
		this.dzchunkindex = dzchunkindex;
		this.dztotalfilesize = dztotalfilesize;
		this.dzchunksize = dzchunksize;
		this.dztotalchunkcount = dztotalchunkcount;
		this.dzchunkbyteoffset = dzchunkbyteoffset;
	}

	public String getDzuuid() {
		return this.dzuuid;
	}

	public void setDzuuid(String dzuuid) {
		this.dzuuid = dzuuid;
	}

	public String getSites() {
		return this.sites;
	}

	public void setSites(String sites) {
		this.sites = sites;
	}

	public int getDzchunkindex() {
		return this.dzchunkindex;
	}

	public void setDzchunkindex(int dzchunkindex) {
		this.dzchunkindex = dzchunkindex;
	}

	public int getDztotalfilesize() {
		return this.dztotalfilesize;
	}

	public void setDztotalfilesize(int dztotalfilesize) {
		this.dztotalfilesize = dztotalfilesize;
	}

	public int getDzchunksize() {
		return this.dzchunksize;
	}

	public void setDzchunksize(int dzchunksize) {
		this.dzchunksize = dzchunksize;
	}

	public int getDztotalchunkcount() {
		return this.dztotalchunkcount;
	}

	public void setDztotalchunkcount(int dztotalchunkcount) {
		this.dztotalchunkcount = dztotalchunkcount;
	}

	public int getDzchunkbyteoffset() {
		return this.dzchunkbyteoffset;
	}

	public void setDzchunkbyteoffset(int dzchunkbyteoffset) {
		this.dzchunkbyteoffset = dzchunkbyteoffset;
	}

	@Override
	public String toString() {
		return "{" + "dzuuid='" + getDzuuid() + "'" + ", sites='" + getSites() + "'" + ", dzchunkindex='"
				+ getDzchunkindex() + "'" + ", dztotalfilesize='" + getDztotalfilesize() + "'" + ", dzchunksize='"
				+ getDzchunksize() + "'" + ", dztotalchunkcount='" + getDztotalchunkcount() + "'"
				+ ", dzchunkbyteoffset='" + getDzchunkbyteoffset() + "'" + "}";
	}

}