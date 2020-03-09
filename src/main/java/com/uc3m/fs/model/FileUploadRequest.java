package com.uc3m.fs.model;

import java.util.Arrays;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FileUploadRequest {

	@NotNull
	@NotBlank
	private String dzuuid;
	@NotNull
	@JsonProperty("List<site>")
	private String[] sites;
	/*@NotNull
	private int dzchunkindex, dztotalfilesize, dzchunksize, dztotalchunkcount, dzchunkbyteoffset;*/

	public FileUploadRequest(@NotNull @NotBlank String dzuuid, @NotNull String[] sites) {
		super();
		this.dzuuid = dzuuid;
		this.sites = sites;
	}

	public String getDzuuid() {
		return this.dzuuid;
	}

	public void setDzuuid(String dzuuid) {
		this.dzuuid = dzuuid;
	}

	public String[] getSites() {
		return this.sites;
	}

	public void setSites(String[] sites) {
		this.sites = sites;
	}

	@Override
	public String toString() {
		return "FileUploadRequest [dzuuid=" + dzuuid + ", sites=" + Arrays.toString(sites) + "]";
	}

}