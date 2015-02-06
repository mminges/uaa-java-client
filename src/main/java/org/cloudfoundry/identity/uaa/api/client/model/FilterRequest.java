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
package org.cloudfoundry.identity.uaa.api.client.model;

import java.util.List;

/**
 * @author Josh Ghiloni
 *
 */
public class FilterRequest {
	private String filter;
	
	private List<String> attributes;

	private int start;

	private int count;

	public FilterRequest(String filter, List<String> attributes, int start, int count) {
		this.filter = filter;
		this.attributes = attributes;
		this.start = start;
		this.count = count;
	}

	public String getFilter() {
		return filter;
	}

	public int getStart() {
		return start;
	}

	public int getCount() {
		return count;
	}

	public List<String> getAttributes() {
		return attributes;
	}
	
	public static final FilterRequest SHOW_ALL = new FilterRequest(null, null, 0, 0);
}
