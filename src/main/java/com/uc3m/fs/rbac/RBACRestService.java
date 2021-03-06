package com.uc3m.fs.rbac;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.uc3m.fs.Config;

public class RBACRestService {

	public static String[] getSitesOfUser(String bearerToken) throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, bearerToken);
		RestTemplate rest = new RestTemplate();
		ResponseEntity<ResponseGet> res = rest.exchange(Config.PATH_RBAC_MANAGED_SITES, HttpMethod.GET, new HttpEntity<>(headers),
				new ParameterizedTypeReference<ResponseGet>(){});

		if (res.getStatusCode() != HttpStatus.OK) throw new Exception("Error calling RBAC");

		return res.getBody().sites;
	}

}