package com.uc3m.fs.exceptions;

public class FSFileNotFoundException extends FSException {
	private static final long serialVersionUID = 5860169452805472044L;

	public FSFileNotFoundException(String message) {
		super(message);
	}

	public FSFileNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}