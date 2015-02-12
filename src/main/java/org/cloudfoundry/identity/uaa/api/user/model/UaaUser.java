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
package org.cloudfoundry.identity.uaa.api.user.model;

import java.util.Collection;

import org.cloudfoundry.identity.uaa.api.common.model.ScimMetaObject;
import org.cloudfoundry.identity.uaa.api.common.model.ValueObject;
import org.cloudfoundry.identity.uaa.api.group.model.UaaGroupMember;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

/**
 * @author Josh Ghiloni
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = Inclusion.NON_NULL)
public class UaaUser extends ScimMetaObject {

	private String userName;

	private Name name;
	
	private String password;

	private Collection<ValueObject> emails;

	private Collection<ValueObject> phoneNumbers;

	private Collection<UaaGroupMember> groups;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Name getName() {
		return name;
	}

	public void setName(Name name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Collection<ValueObject> getEmails() {
		return emails;
	}

	public void setEmails(Collection<ValueObject> emails) {
		this.emails = emails;
	}

	public Collection<ValueObject> getPhoneNumbers() {
		return phoneNumbers;
	}

	public void setPhoneNumbers(Collection<ValueObject> phoneNumbers) {
		this.phoneNumbers = phoneNumbers;
	}

	public Collection<UaaGroupMember> getGroups() {
		return groups;
	}

	public void setGroups(Collection<UaaGroupMember> groups) {
		this.groups = groups;
	}

	public static class Name {
		private String formatted;

		private String familyName;

		private String givenName;

		public String getFormatted() {
			return formatted;
		}

		public void setFormatted(String formatted) {
			this.formatted = formatted;
		}

		public String getFamilyName() {
			return familyName;
		}

		public void setFamilyName(String familyName) {
			this.familyName = familyName;
		}

		public String getGivenName() {
			return givenName;
		}

		public void setGivenName(String givenName) {
			this.givenName = givenName;
		}
	}
}
