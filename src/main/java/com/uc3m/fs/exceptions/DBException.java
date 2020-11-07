package com.uc3m.fs.exceptions;

public class DBException extends Exception {
	private static final long serialVersionUID = 2964118918417431948L;

	public DBException(String message) {
		super(message);
	}

	public DBException(String message, Throwable cause) {
		super(message, cause);
	}

}