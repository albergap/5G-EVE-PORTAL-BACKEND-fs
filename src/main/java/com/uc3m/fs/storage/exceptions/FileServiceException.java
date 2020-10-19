package com.uc3m.fs.storage.exceptions;

public class FileServiceException extends RuntimeException {
	private static final long serialVersionUID = 2964118918417431948L;

	public FileServiceException(String message) {
		super(message);
	}

	public FileServiceException(String message, Throwable cause) {
		super(message, cause);
	}

}