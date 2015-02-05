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
import org.cloudfoundry.identity.uaa.api.client.UaaConnection;
import org.cloudfoundry.identity.uaa.api.client.UaaListOperations;

/**
 * @author Josh Ghiloni
 *
 */
public class UaaConnectionImpl implements UaaConnection {

	private UaaConnectionHelper helper;

	public UaaConnectionImpl(UaaConnectionHelper helper) {
		this.helper = helper;
	}

	public UaaClientOperations clientOperations() {
		return new UaaClientOperationsImpl(helper);
	}

	public UaaListOperations listOperations() {
		return new UaaListOperationsImpl(helper);
	}
}