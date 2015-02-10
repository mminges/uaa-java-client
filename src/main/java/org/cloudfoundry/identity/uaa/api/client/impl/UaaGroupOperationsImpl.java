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
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.cloudfoundry.identity.uaa.api.client.UaaGroupOperations;
import org.cloudfoundry.identity.uaa.api.client.model.group.ScimMetaObject;
import org.cloudfoundry.identity.uaa.api.client.model.group.UaaGroup;
import org.cloudfoundry.identity.uaa.api.client.model.group.UaaGroupMapping;
import org.cloudfoundry.identity.uaa.api.client.model.group.UaaGroupMappingIdentifier;
import org.cloudfoundry.identity.uaa.api.client.model.group.UaaGroupMappingsResults;
import org.cloudfoundry.identity.uaa.api.client.model.group.UaaGroupMember;
import org.cloudfoundry.identity.uaa.api.client.model.group.UaaGroupsResults;
import org.cloudfoundry.identity.uaa.api.client.model.list.FilterRequest;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * @author Josh Ghiloni
 *
 */
public class UaaGroupOperationsImpl implements UaaGroupOperations {

	private UaaConnectionHelper helper;

	private static final List<String> SCHEMAS = Arrays.asList("urn:scim:schemas:core:1.0");

	public UaaGroupOperationsImpl(UaaConnectionHelper helper) {
		this.helper = helper;
	}

	public UaaGroup createGroup(UaaGroup group) {
		Assert.notNull(group);
		Assert.hasText(group.getDisplayName());

		group.setSchemas(SCHEMAS);

		return helper.post("/Groups", group, UaaGroup.class);
	}

	public void deleteGroup(String groupId) {
		Assert.hasText(groupId);

		helper.delete("/Groups/{id}", Object.class, groupId);
	}

	public UaaGroupsResults getGroups(FilterRequest request) {
		Assert.notNull(request);

		return helper.get(helper.buildScimFilterUrl("/Groups", request), UaaGroupsResults.class);
	}

	public UaaGroupMapping createGroupMapping(UaaGroupMappingIdentifier type, String identifier, String externalGroupDn) {
		Assert.notNull(type);
		Assert.hasText(identifier);
		Assert.hasText(externalGroupDn);

		Map<String, Object> request = new LinkedHashMap<String, Object>(3);

		request.put("schemas", SCHEMAS);
		request.put(type.jsonKey(), identifier);
		request.put("externalGroup", externalGroupDn);

		return helper.post("/Groups/External", request, UaaGroupMapping.class);
	}

	public void deleteGroupMapping(UaaGroupMapping mapping) {
		Assert.notNull(mapping);

		String id = null;
		UaaGroupMappingIdentifier type = null;
		String external = mapping.getExternalGroup();

		if (StringUtils.hasText(mapping.getGroupId())) {
			id = mapping.getGroupId();
			type = UaaGroupMappingIdentifier.GROUP_ID;
		}
		else {
			id = mapping.getDisplayName();
			type = UaaGroupMappingIdentifier.DISPLAY_NAME;
		}

		helper.delete("/Groups/External/{type}/{id}/externalGroup/{externalGroup}", String.class, type, id, external);
	}

	public UaaGroupMappingsResults getGroupMappings(FilterRequest request) {
		Assert.notNull(request);
		return helper.get(helper.buildScimFilterUrl("/Groups/External", request), UaaGroupMappingsResults.class);
	}

	public UaaGroup updateGroupName(String groupId, String newName) {
		UaaGroup group = getGroupById(groupId);
		group.setDisplayName(newName);

		UaaModificationGroup modGroup = new UaaModificationGroup(group);
		
		return updateGroup(modGroup);
	}

	public UaaGroup addMember(String groupId, String memberUserName) {
		Assert.hasText(memberUserName);

		UaaGroup group = getGroupById(groupId);		
		UaaModificationGroup modGroup = new UaaModificationGroup(group);

		String memberId = getUserId(memberUserName);

		Collection<String> members = modGroup.getMembers();
		if (members == null) {
			members = new ArrayList<String>(1);
		}

		members.add(memberId);
		modGroup.setMembers(members);

		return updateGroup(modGroup);
	}

	public UaaGroup deleteMember(String groupId, String memberUserName) {
		Assert.hasText(memberUserName);

		UaaGroup group = getGroupById(groupId);
		UaaModificationGroup modGroup = new UaaModificationGroup(group);
		
		String memberId = getUserId(memberUserName);

		Collection<String> members = modGroup.getMembers();
		if (members != null && !members.isEmpty()) {
			for (Iterator<String> iter = members.iterator(); iter.hasNext();) {
				String member = iter.next();

				if (memberId.equals(member)) {
					iter.remove();
					break;
				}
			}
		}

		return updateGroup(modGroup);
	}

	private UaaGroup getGroupById(String groupId) {
		FilterRequest request = new FilterRequest(String.format("id eq \"%s\"", groupId), null, 0, 0);
		UaaGroupsResults results = getGroups(request);

		if (results.getTotalResults() > 0) {
			return results.getResources().iterator().next();
		}

		return null;
	}

	private UaaGroup updateGroup(UaaModificationGroup group) {
		Assert.notNull(group);

		HttpHeaders headers = new HttpHeaders();
		headers.set("if-match", group.getMeta().get("version"));

		return helper.exchange(HttpMethod.PUT, headers, group, "/Groups/{id}", UaaGroup.class, group.getId());
	}

	// TODO move to user services or helper once available, if possible?
	@SuppressWarnings("unchecked")
	private String getUserId(String memberName) {
		FilterRequest request = new FilterRequest(String.format("userName eq \"%s\"", memberName), Arrays.asList("id"),
				0, 0);

		String uri = helper.buildScimFilterUrl("/Users", request);

		try {

			Map<String, Object> retval = helper.exchange(HttpMethod.GET, null, uri, Map.class);
			Collection<Map<String, Object>> resources = (Collection<Map<String, Object>>) retval.get("resources");
			Map<String, Object> first = resources.iterator().next();
			return (String) first.get("id");
		}
		catch (Throwable t) {
			t.printStackTrace();
			return null;
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonSerialize(include = Inclusion.NON_NULL)
	public static class UaaModificationGroup extends ScimMetaObject {

		private String displayName;

		private String groupId;

		private Collection<String> members;
		
		UaaModificationGroup(UaaGroup clone) {
			setDisplayName(clone.getDisplayName());
			setGroupId(clone.getGroupId());
			setSchemas(clone.getSchemas());
			setId(clone.getId());
			setMeta(clone.getMeta());
			
			Collection<UaaGroupMember> members = clone.getMembers();
			if (members != null) {
				List<String> memberIds = new ArrayList<String>(members.size());
				for (UaaGroupMember member : members) {
					memberIds.add(member.getValue());
				}
				
				setMembers(memberIds);
			}
		}

		public String getDisplayName() {
			return displayName;
		}

		public void setDisplayName(String displayName) {
			this.displayName = displayName;
		}

		public String getGroupId() {
			return groupId;
		}

		public void setGroupId(String groupId) {
			this.groupId = groupId;
		}

		public Collection<String> getMembers() {
			return members;
		}

		public void setMembers(Collection<String> members) {
			this.members = members;
		}
	}
}
