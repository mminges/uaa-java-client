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

import org.cloudfoundry.identity.uaa.api.client.UaaClientOperations;
import org.cloudfoundry.identity.uaa.api.client.model.UaaClient;
import org.springframework.util.Assert;

/**
 * @author Josh Ghiloni
 *
 */
public class UaaClientOperationsImpl implements UaaClientOperations {

	private UaaConnectionHelper helper;
	
	public UaaClientOperationsImpl(UaaConnectionHelper helper) {
		this.helper = helper;
	}
	
	public void create(UaaClient client) {
		Assert.notNull(client);
		Assert.hasText(client.getClientId());
		
		helper.post(String.format("/oauth/clients/%s", client.getClientId()), client, client.getClass());
	}

	public UaaClient findById(String clientId) {
		Assert.hasText(clientId);
		return helper.get(String.format("/oauth/clients/%s", clientId), UaaClient.class);
	}

	public void update(UaaClient client) {
		Assert.notNull(client);
		Assert.hasText(client.getClientId());
		
		helper.put(String.format("/oauth/clients/%s", client.getClientId()), client, client.getClass());
	}

	public void delete(String clientId) {
		Assert.hasText(clientId);
		helper.delete(String.format("/oauth/clients/%s", clientId), UaaClient.class);
	}
}
