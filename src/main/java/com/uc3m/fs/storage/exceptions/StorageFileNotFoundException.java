package com.uc3m.fs.storage.exceptions;

public class StorageFileNotFoundException extends StorageException {
	private static final long serialVersionUID = 1606394518565785842L;

	public StorageFileNotFoundException(String message) {
		super(message);
	}

	public StorageFileNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}