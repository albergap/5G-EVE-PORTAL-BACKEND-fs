package com.uc3m.fs.keycloak;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;

import com.uc3m.fs.Config;
import com.uc3m.fs.exceptions.KeycloakNotAuthenticated;

public class RequestProperties {

	private AccessToken accessToken;
	public boolean managerRole, developerRole, notAuthenticated;

	private static AccessToken getAccessToken(HttpServletRequest request) throws KeycloakNotAuthenticated {
		KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) request.getUserPrincipal();
		if (token == null) throw new KeycloakNotAuthenticated("Not authenticated");
		KeycloakPrincipal<?> principal = (KeycloakPrincipal<?>) token.getPrincipal();
		return principal.getKeycloakSecurityContext().getToken();
	}

	public RequestProperties(HttpServletRequest request) {
		try {
			accessToken = getAccessToken(request);
			managerRole = isManagerRole();
			developerRole = isDeveloperRole();
			notAuthenticated = false;
		} catch (KeycloakNotAuthenticated e) {
			managerRole = developerRole = false;
			notAuthenticated = true;
		}
	}

	public String getUserId() {
		if (notAuthenticated) return null;
		return accessToken.getEmail();
	}

	public Set<String> getUserRoles() {
		if (notAuthenticated) return null;
		return accessToken.getRealmAccess().getRoles();
	}

	private boolean isDeveloperRole() {
		return getUserRoles().contains(Config.ROLE_DEVELOPER);
	}
	private boolean isManagerRole() {
		return getUserRoles().contains(Config.ROLE_MANAGER);
	}

}