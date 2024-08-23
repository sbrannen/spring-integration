/*
 * Copyright 2013-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;

import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.IndexAccessor;
import org.springframework.expression.TypedValue;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * A SpEL {@link IndexAccessor} that knows how to read indexes from JSON arrays, using
 * Jackson's {@link ArrayNode} API.
 *
 * <p>Supports indexes supplied as either an integer literal (e.g., {@code [1]}) or a string
 * literal representing an integer (e.g., {@code ['1']}).
 *
 * @author Sam Brannen
 * @author Eric Bottard
 * @author Artem Bilan
 * @author Paul Martin
 * @author Gary Russell
 * @author Pierre Lakreb
 * @author Vladislav Fefelov
 * @see JsonPropertyAccessor
 */
public class JsonIndexAccessor implements IndexAccessor {

	private static final Class<?>[] SUPPORTED_CLASSES = { ArrayNode.class };

	@Override
	public Class<?>[] getSpecificTargetClasses() {
		return SUPPORTED_CLASSES;
	}

	@Override
	public boolean canRead(EvaluationContext context, Object target, Object index) {
		return (target instanceof ArrayNode &&
				(index instanceof Integer || isNumericString(index)));
	}

	@Override
	public TypedValue read(EvaluationContext context, Object target, Object index) throws AccessException {
		ArrayNode arrayNode = (ArrayNode) target;
		Integer intIndex = (index instanceof Integer integer ? integer : Integer.valueOf((String) index));
		return typedValue(arrayNode.get(intIndex));
	}

	@Override
	public boolean canWrite(EvaluationContext context, Object target, Object index) {
		return false;
	}

	@Override
	public void write(EvaluationContext context, Object target, Object index, @Nullable Object newValue) {
		throw new UnsupportedOperationException("Write is not supported");
	}

	/**
	 * Determine if the supplied index is a string containing only numeric digits.
	 */
	private static boolean isNumericString(Object index) {
		if (!(index instanceof String str && StringUtils.hasLength(str))) {
			return false;
		}
		int length = str.length();
		for (int i = 0; i < length; i++) {
			if (!Character.isDigit(str.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	private static TypedValue typedValue(JsonNode jsonNode) throws AccessException {
		if (jsonNode == null || jsonNode instanceof NullNode) {
			return TypedValue.NULL;
		}
		else if (jsonNode.isValueNode()) {
			return new TypedValue(JsonPropertyAccessor.getValue(jsonNode));
		}
		return new TypedValue(jsonNode);
	}

}
