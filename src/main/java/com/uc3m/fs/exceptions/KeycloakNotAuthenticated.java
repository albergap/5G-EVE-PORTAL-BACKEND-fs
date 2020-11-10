package com.uc3m.fs.exceptions;

public class KeycloakNotAuthenticated extends Exception {
	private static final long serialVersionUID = -5876506288454182002L;

	public KeycloakNotAuthenticated(String message) {
		super(message);
	}

	public KeycloakNotAuthenticated(String message, Throwable cause) {
		super(message, cause);
	}

}