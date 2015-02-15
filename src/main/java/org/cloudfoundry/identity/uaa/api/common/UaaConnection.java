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
package org.cloudfoundry.identity.uaa.api.common;

import org.cloudfoundry.identity.uaa.api.client.UaaClientOperations;
import org.cloudfoundry.identity.uaa.api.group.UaaGroupOperations;
import org.cloudfoundry.identity.uaa.api.user.UaaUserOperations;

/**
 * A collection of objects to access different parts of the API
 * 
 * @author Josh Ghiloni
 *
 */
public interface UaaConnection {
	/**
	 * @return an entry point for client APIs
	 */
	public UaaClientOperations clientOperations();

	/**
	 * @return an entry point for group APIS
	 */
	public UaaGroupOperations groupOperations();

	/**
	 * @return an entry point for user APIs
	 */
	public UaaUserOperations userOperations();
}
