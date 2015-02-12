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
package org.cloudfoundry.identity.uaa.api.group;

import org.cloudfoundry.identity.uaa.api.common.model.FilterRequest;
import org.cloudfoundry.identity.uaa.api.common.model.PagedResult;
import org.cloudfoundry.identity.uaa.api.group.model.UaaGroup;
import org.cloudfoundry.identity.uaa.api.group.model.UaaGroupMapping;
import org.cloudfoundry.identity.uaa.api.group.model.UaaGroupMappingIdentifier;

/**
 * @author Josh Ghiloni
 *
 */
public interface UaaGroupOperations {
	public UaaGroup createGroup(UaaGroup group);

	public UaaGroup updateGroupName(String groupId, String newName);
	
	public UaaGroup addMember(String groupId, String memberName);
	
	public UaaGroup deleteMember(String groupId, String memberName);

	public void deleteGroup(String groupId);

	public PagedResult<UaaGroup> getGroups(FilterRequest request);
	
	public UaaGroupMapping createGroupMapping(UaaGroupMappingIdentifier type, String identifier, String externalGroupDn);
	
	public void deleteGroupMapping(UaaGroupMapping mapping);
	
	public PagedResult<UaaGroupMapping> getGroupMappings(FilterRequest request);
}