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
package org.cloudfoundry.identity.uaa.api.client.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudfoundry.identity.uaa.api.client.UaaClientOperations;
import org.cloudfoundry.identity.uaa.api.client.model.FilterRequest;
import org.cloudfoundry.identity.uaa.api.client.model.UaaClient;
import org.cloudfoundry.identity.uaa.api.client.model.UaaClientsResults;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * @author Josh Ghiloni
 *
 */
public class UaaClientOperationsImpl implements UaaClientOperations {

	private UaaConnectionHelper helper;

	public UaaClientOperationsImpl(UaaConnectionHelper helper) {
		this.helper = helper;
	}

	public UaaClient create(UaaClient client) {
		Assert.notNull(client);
		Assert.hasText(client.getClientId());

		return helper.post("/oauth/clients", client, UaaClient.class);
	}

	public UaaClient findById(String clientId) {
		Assert.hasText(clientId);
		return helper.get("/oauth/clients/{id}", UaaClient.class, clientId);
	}

	public UaaClient update(UaaClient client) {
		Assert.notNull(client);
		Assert.hasText(client.getClientId());

		return helper.put("/oauth/clients/{id}", client, UaaClient.class, client.getClientId());
	}

	public UaaClient delete(String clientId) {
		Assert.hasText(clientId);
		return helper.delete("/oauth/clients/{id}", UaaClient.class, clientId);
	}

	public UaaClientsResults getClients(FilterRequest request) {
		Assert.notNull(request);
		StringBuilder uriBuilder = new StringBuilder("/oauth/clients?");

		boolean hasAttr = false;
		boolean hasFilter = false;
		boolean hasStart = false;
		boolean hasCount = false;
		boolean hasParams = false;

		if (request.getAttributes() != null && !request.getAttributes().isEmpty()) {
			uriBuilder.append("attributes={attributes}");

			// remove the last comma
			uriBuilder.deleteCharAt(uriBuilder.length() - 1);
			hasAttr = true;
			hasParams = true;
		}

		if (StringUtils.hasText(request.getFilter())) {
			if (hasParams) {
				uriBuilder.append("&");
			}

			uriBuilder.append("filter={filter}");
			hasFilter = true;
			hasParams = true;
		}

		if (request.getStart() > 0) {
			if (hasParams) {
				uriBuilder.append("&");
			}

			uriBuilder.append("startIndex={start}");
			hasStart = true;
			hasParams = true;
		}

		if (request.getCount() > 0) {
			if (hasParams) {
				uriBuilder.append("&");
			}

			uriBuilder.append("count={count}");
			hasCount = true;
			hasParams = true;
		}

		List<Object> varArgList = new ArrayList<Object>();

		if (hasAttr) {
			varArgList.add(joinList(request.getAttributes(), ","));
		}

		if (hasFilter) {
			varArgList.add(request.getFilter());
		}

		if (hasStart) {
			varArgList.add(request.getStart());
		}

		if (hasCount) {
			varArgList.add(request.getCount());
		}

		return helper.get(uriBuilder.toString(), UaaClientsResults.class, varArgList.toArray());
	}

	public boolean changeClientSecret(String clientId, String oldSecret, String newSecret) {
		Map<String, String> body = new HashMap<String, String>(2);
		body.put("oldSecret", oldSecret);
		body.put("secret", newSecret);

		String result = helper.put("/oauth/clients/{id}/secret", body, String.class, clientId);
		System.out.println(result);
		
		return (result != null);
	}

	private String joinList(List<?> list, String joiner) {
		Assert.state(list != null && !list.isEmpty());

		if (joiner == null) {
			joiner = "";
		}

		StringBuilder b = new StringBuilder();
		for (Object o : list) {
			b.append(o).append(joiner);
		}

		// delete the last instance of joiner
		if (joiner.length() > 0) {
			b.delete(b.length() - joiner.length(), joiner.length());
		}

		return b.toString();
	}
}
