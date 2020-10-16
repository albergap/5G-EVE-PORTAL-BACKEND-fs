package com.uc3m.fs.keycloak;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;

import com.uc3m.fs.Config;

public class KeycloakUtil {

	public static String getIdUser(HttpServletRequest request) {
		KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) request.getUserPrincipal();        
		KeycloakPrincipal<?> principal = (KeycloakPrincipal<?>) token.getPrincipal();
		AccessToken accessToken = principal.getKeycloakSecurityContext().getToken();
		return accessToken.getEmail();
	}

	private static Set<String> getUserRoles(HttpServletRequest request) {
		KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) request.getUserPrincipal();        
		KeycloakPrincipal<?> principal = (KeycloakPrincipal<?>) token.getPrincipal();
		AccessToken accessToken = principal.getKeycloakSecurityContext().getToken();
		return accessToken.getRealmAccess().getRoles();
	}

	public static boolean isUserRole(HttpServletRequest request) {
		Set<String> roles = getUserRoles(request);
		return roles.contains(Config.ROLE_USER);
	}

	public static boolean isManagerRole(HttpServletRequest request) {
		Set<String> roles = getUserRoles(request);
		return roles.contains(Config.ROLE_SITE_MANAGER);
	}

}