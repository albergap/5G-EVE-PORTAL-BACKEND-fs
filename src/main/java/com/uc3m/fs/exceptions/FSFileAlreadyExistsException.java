package com.uc3m.fs.exceptions;

public class FSFileAlreadyExistsException extends FSException {
	private static final long serialVersionUID = -970845392233190967L;

	public FSFileAlreadyExistsException(String message) {
		super(message);
	}

	public FSFileAlreadyExistsException(String message, Throwable cause) {
		super(message, cause);
	}

}