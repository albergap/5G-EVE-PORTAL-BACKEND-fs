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
	public boolean managerRole, developerRole, authenticated;

	private static AccessToken getAccessToken(HttpServletRequest request) throws KeycloakNotAuthenticated {
		KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) request.getUserPrincipal();
		if (token == null) throw new KeycloakNotAuthenticated("Not authenticated");
		KeycloakPrincipal<?> principal = (KeycloakPrincipal<?>) token.getPrincipal();
		return principal.getKeycloakSecurityContext().getToken();
	}

	public RequestProperties(HttpServletRequest request) {
		try {
			accessToken = getAccessToken(request);
			// If the AccessToken is created -> user authenticated on keycloak
			authenticated = true;

			managerRole = getUserRoles().contains(Config.ROLE_MANAGER);
			developerRole = getUserRoles().contains(Config.ROLE_DEVELOPER);
		} catch (KeycloakNotAuthenticated e) {
			authenticated = managerRole = developerRole = false;
		}
	}

	public String getUserId() {
		if (!authenticated) return null;
		return accessToken.getEmail();
	}

	public Set<String> getUserRoles() {
		if (!authenticated) return null;
		return accessToken.getRealmAccess().getRoles();
	}

}