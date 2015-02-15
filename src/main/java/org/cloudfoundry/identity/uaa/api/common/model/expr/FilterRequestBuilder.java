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
package org.cloudfoundry.identity.uaa.api.common.model.expr;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import org.springframework.util.Assert;

/**
 * @author Josh Ghiloni
 *
 */
public class FilterRequestBuilder {

	private boolean defaultAnd = true;

	private Stack<Operation> opStack = new Stack<Operation>();

	private List<String> attributes = null;

	private int start = 0;

	private int count = 0;

	private boolean built = false;

	public FilterRequestBuilder() {
		this(true);
	}

	public FilterRequestBuilder(boolean defaultAnd) {
		this.defaultAnd = defaultAnd;
	}

	public FilterRequestBuilder equals(String key, Object val) {
		verifyActive();
		opStack.push(new EqualsOperation(key, val));
		return this;
	}

	public FilterRequestBuilder lessThan(String key, Object val) {
		verifyActive();
		opStack.push(new LessThanOperation(key, val));
		return this;
	}

	public FilterRequestBuilder greaterThan(String key, Object val) {
		verifyActive();
		opStack.push(new GreaterThanOperation(key, val));
		return this;
	}

	public FilterRequestBuilder lessThanOrEquals(String key, Object val) {
		verifyActive();
		opStack.push(new LessEqualOperation(key, val));
		return this;
	}

	public FilterRequestBuilder greaterThanOrEquals(String key, Object val) {
		verifyActive();
		opStack.push(new GreaterEqualOperation(key, val));
		return this;
	}

	public FilterRequestBuilder startsWith(String key, String val) {
		verifyActive();
		opStack.push(new StartsWithOperator(key, val));
		return this;
	}

	public FilterRequestBuilder contains(String key, String val) {
		verifyActive();
		opStack.push(new ContainsOperator(key, val));
		return this;
	}

	public FilterRequestBuilder present(String key) {
		verifyActive();
		opStack.push(new PresentOperator(key));
		return this;
	}

	public FilterRequestBuilder and() {
		verifyActive();

		if (opStack.size() < 2) {
			throw new IllegalStateException("need at least two operations to join");
		}

		Operation second = opStack.pop();
		Operation first = opStack.pop();

		opStack.push(new AndOperator(first, second));
		return this;
	}

	public FilterRequestBuilder or() {
		verifyActive();

		if (opStack.size() < 2) {
			throw new IllegalStateException("need at least two operations to join");
		}

		Operation second = opStack.pop();
		Operation first = opStack.pop();

		opStack.push(new OrOperator(first, second));
		return this;
	}

	public FilterRequestBuilder precedence() {
		verifyActive();
		if (opStack.isEmpty()) {
			throw new IllegalStateException("need an operation to set precedence");
		}

		Operation op = opStack.peek();

		if (!(op instanceof PrecedenceOperator)) {
			opStack.pop();
			opStack.push(new PrecedenceOperator(op));
		}

		return this;
	}

	public FilterRequestBuilder attributes(String... attributes) {
		verifyActive();
		this.attributes = Arrays.asList(attributes);
		return this;
	}

	public FilterRequestBuilder start(int start) {
		verifyActive();
		Assert.state(start > 0);
		this.start = start;
		return this;
	}

	public FilterRequestBuilder count(int count) {
		verifyActive();
		Assert.state(count > 0);
		this.count = count;
		return this;
	}

	public static FilterRequest showAll() {
		return FilterRequest.SHOW_ALL;
	}

	public FilterRequest build() {
		verifyActive();

		Operation filter = joinAll();
		built = true;

		return new FilterRequest(filter.toString(), attributes, start, count);
	}

	private Operation joinAll() {
		if (opStack.isEmpty()) {
			return NullOperation.INSTANCE;
		}

		if (opStack.size() == 1) {
			return opStack.pop();
		}

		while (opStack.size() > 1) {
			Operation second = opStack.pop();
			Operation first = opStack.pop();

			Operation joined = defaultAnd ? new AndOperator(first, second) : new OrOperator(first, second);
			opStack.push(joined);
		}

		return opStack.pop();
	}

	private void verifyActive() {
		if (built) {
			throw new IllegalStateException("Builder already built");
		}
	}
}
