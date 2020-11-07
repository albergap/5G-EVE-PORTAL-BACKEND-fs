package com.uc3m.fs.exceptions;

public class FSException extends Exception {
	private static final long serialVersionUID = 1606394518565785842L;

	public FSException(String message) {
		super(message);
	}

	public FSException(String message, Throwable cause) {
		super(message, cause);
	}

}