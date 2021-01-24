package com.uc3m.fs.exceptions;

public class ParameterException extends Exception {
	private static final long serialVersionUID = -4546599098079230437L;

	public ParameterException(String message) {
		super(message);
	}

	public ParameterException(String message, Throwable cause) {
		super(message, cause);
	}

}