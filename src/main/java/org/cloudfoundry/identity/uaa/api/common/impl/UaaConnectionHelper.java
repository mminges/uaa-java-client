/*
 * Copyright 2015 ECS Team, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cloudfoundry.identity.uaa.api.common.impl;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.cloudfoundry.identity.uaa.api.common.model.FilterRequest;
import org.cloudfoundry.identity.uaa.api.common.model.ScimMetaObject;
import org.cloudfoundry.identity.uaa.api.common.model.UaaCredentials;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.AccessTokenProvider;
import org.springframework.security.oauth2.client.token.AccessTokenProviderChain;
import org.springframework.security.oauth2.client.token.DefaultAccessTokenRequest;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.security.oauth2.client.token.grant.implicit.ImplicitAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.implicit.ImplicitResourceDetails;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordResourceDetails;
import org.springframework.security.oauth2.common.AuthenticationScheme;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

/**
 * @author Josh Ghiloni
 *
 */
public class UaaConnectionHelper {
	private static final AccessTokenProviderChain CHAIN = new AccessTokenProviderChain(
			Arrays.<AccessTokenProvider> asList(new ClientCredentialsAccessTokenProvider(),
					new ImplicitAccessTokenProvider(), new ResourceOwnerPasswordAccessTokenProvider()));

	private OAuth2AccessToken token;

	private URL url;

	private UaaCredentials creds;

	public UaaConnectionHelper(URL url, UaaCredentials creds) {
		this.url = url;
		this.creds = creds;
	}

	public <ResponseType> ResponseType get(String uri, Class<ResponseType> responseType, Object... uriVariables) {
		return exchange(HttpMethod.GET, null, uri, responseType, uriVariables);
	}

	public <ResponseType> ResponseType delete(String uri, Class<ResponseType> responseType, Object... uriVariables) {
		return exchange(HttpMethod.DELETE, null, uri, responseType, uriVariables);
	}

	public <RequestType, ResponseType> ResponseType post(String uri, RequestType body,
			Class<ResponseType> responseType, Object... uriVariables) {
		return exchange(HttpMethod.POST, body, uri, responseType, uriVariables);
	}

	public <RequestType, ResponseType> ResponseType put(String uri, RequestType body, Class<ResponseType> responseType,
			Object... uriVariables) {
		return exchange(HttpMethod.PUT, body, uri, responseType, uriVariables);
	}

	public <RequestType extends ScimMetaObject, ResponseType> ResponseType putScimObject(String uri, RequestType body,
			Class<ResponseType> responseType, Object... uriVariables) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("if-match", body.getMeta().get("version"));

		return exchange(HttpMethod.PUT, headers, body, uri, responseType, uriVariables);
	}

	public String getUserIdByName(String userName) {
		FilterRequest request = new FilterRequest(String.format("userName eq \"%s\"", userName), Arrays.asList("id"),
				0, 0);

		String uri = buildScimFilterUrl("/Users", request);

		try {

			@SuppressWarnings("unchecked")
			Map<String, Object> retval = exchange(HttpMethod.GET, null, uri, Map.class);

			@SuppressWarnings("unchecked")
			Collection<Map<String, Object>> resources = (Collection<Map<String, Object>>) retval.get("resources");

			Map<String, Object> first = resources.iterator().next();
			return (String) first.get("id");
		}
		catch (Throwable t) {
			t.printStackTrace();
			return null;
		}
	}

	private <RequestType, ResponseType> ResponseType exchange(HttpMethod method, RequestType body, String uri,
			Class<ResponseType> responseType, Object... uriVariables) {
		return exchange(method, new HttpHeaders(), body, uri, responseType, uriVariables);
	}

	private <RequestType, ResponseType> ResponseType exchange(HttpMethod method, HttpHeaders headers, RequestType body,
			String uri, Class<ResponseType> responseType, Object... uriVariables) {
		getHeaders(headers);

		RestTemplate template = new RestTemplate();
		template.setInterceptors(LoggerInterceptor.INTERCEPTOR);

		HttpEntity<RequestType> requestEntity = null;
		if (method == HttpMethod.GET || body == null) {
			requestEntity = new HttpEntity<RequestType>(headers);
		}
		else {
			requestEntity = new HttpEntity<RequestType>(body, headers);
		}

		// combine url into the varargs
		List<Object> varList = new ArrayList<Object>();
		varList.add(url);
		if (uriVariables != null && uriVariables.length > 0) {
			varList.addAll(Arrays.asList(uriVariables));
		}

		ResponseEntity<ResponseType> responseEntity = template.exchange("{base}" + uri, method, requestEntity,
				responseType, varList.toArray());

		if (HttpStatus.Series.SUCCESSFUL.equals(responseEntity.getStatusCode().series())) {
			return responseEntity.getBody();
		}
		else {
			return null;
		}
	}

	public String buildScimFilterUrl(String baseUrl, FilterRequest request) {
		StringBuilder uriBuilder = new StringBuilder(baseUrl);

		boolean hasParams = false;

		if (request.getAttributes() != null && !request.getAttributes().isEmpty()) {
			uriBuilder.append("?attributes=").append(
					StringUtils.collectionToCommaDelimitedString(request.getAttributes()));

			hasParams = true;
		}

		if (StringUtils.hasText(request.getFilter())) {
			if (hasParams) {
				uriBuilder.append("&");
			}
			else {
				uriBuilder.append("?");
			}

			uriBuilder.append("filter=").append(request.getFilter());
			hasParams = true;
		}

		if (request.getStart() > 0) {
			if (hasParams) {
				uriBuilder.append("&");
			}
			else {
				uriBuilder.append("?");
			}

			uriBuilder.append("startIndex=").append(request.getStart());
			hasParams = true;
		}

		if (request.getCount() > 0) {
			if (hasParams) {
				uriBuilder.append("&");
			}
			else {
				uriBuilder.append("?");
			}

			uriBuilder.append("count=").append(request.getCount());
			hasParams = true;
		}

		return uriBuilder.toString();
	}

	private void getHeaders(HttpHeaders headers) {
		OAuth2AccessToken token = getAccessToken();
		headers.add("Authorization", token.getTokenType() + " " + token.getValue());

		if (headers.getContentType() == null) {
			headers.setContentType(MediaType.APPLICATION_JSON);
		}

		if (headers.getAccept() == null || headers.getAccept().size() == 0) {
			headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		}
	}

	private OAuth2AccessToken getAccessToken() {
		if (token == null) {
			OAuth2ProtectedResourceDetails details = getResourceDetails(url, creds);
			token = CHAIN.obtainAccessToken(details, new DefaultAccessTokenRequest());
		}
		else if (token.isExpired()) {
			refreshAccessToken();
		}

		return token;
	}

	private void refreshAccessToken() {
		Assert.notNull(token);

		OAuth2ProtectedResourceDetails details = getResourceDetails(url, creds);
		token = CHAIN.refreshAccessToken(details, token.getRefreshToken(), new DefaultAccessTokenRequest());
	}

	private OAuth2ProtectedResourceDetails getResourceDetails(URL url, UaaCredentials creds) {
		Assert.notNull(url);
		Assert.notNull(creds);
		Assert.notNull(creds.getClientId());

		OAuth2ProtectedResourceDetails details = null;
		if (StringUtils.hasText(creds.getUserId()) && StringUtils.hasText(creds.getPassword())) {
			ResourceOwnerPasswordResourceDetails tokenDetails = new ResourceOwnerPasswordResourceDetails();
			tokenDetails.setClientAuthenticationScheme(AuthenticationScheme.header);
			tokenDetails.setUsername(creds.getUserId());
			tokenDetails.setPassword(creds.getPassword());
			tokenDetails.setClientId(creds.getClientId());
			tokenDetails.setClientSecret(creds.getClientSecret());
			tokenDetails.setAccessTokenUri(url + "/oauth/token");

			details = tokenDetails;
		}
		else if (StringUtils.hasText(creds.getClientSecret())) {
			ClientCredentialsResourceDetails tokenDetails = new ClientCredentialsResourceDetails();
			tokenDetails.setClientAuthenticationScheme(AuthenticationScheme.header);
			tokenDetails.setClientId(creds.getClientId());
			tokenDetails.setClientSecret(creds.getClientSecret());
			tokenDetails.setAccessTokenUri(url + "/oauth/token");

			details = tokenDetails;
		}
		else {
			ImplicitResourceDetails tokenDetails = new ImplicitResourceDetails();
			tokenDetails.setClientAuthenticationScheme(AuthenticationScheme.header);
			tokenDetails.setClientId(creds.getClientId());
			tokenDetails.setAccessTokenUri(url + "/oauth/token");

			details = tokenDetails;
		}

		return details;
	}

	private static class LoggerInterceptor implements ClientHttpRequestInterceptor {
		public static final List<ClientHttpRequestInterceptor> INTERCEPTOR = Arrays
				.<ClientHttpRequestInterceptor> asList(new LoggerInterceptor());

		public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
				throws IOException {
			System.out.println(new String(body, "UTF-8"));
			return execution.execute(request, body);
		}
	}
}
