package com.uc3m.fs.exceptions;

public class DBFileNotFoundException extends DBException {
	private static final long serialVersionUID = -7826647641817043899L;

	public DBFileNotFoundException(String message) {
		super(message);
	}

	public DBFileNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}