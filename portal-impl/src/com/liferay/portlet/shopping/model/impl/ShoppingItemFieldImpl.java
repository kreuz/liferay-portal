/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portlet.shopping.model.impl;

import com.liferay.portal.kernel.util.StringUtil;

/**
 * @author Brian Wing Shun Chan
 */
public class ShoppingItemFieldImpl extends ShoppingItemFieldBaseImpl {

	public ShoppingItemFieldImpl() {
	}

	@Override
	public String[] getValuesArray() {
		return _valuesArray;
	}

	@Override
	public void setValues(String values) {
		_valuesArray = StringUtil.split(values);

		super.setValues(values);
	}

	@Override
	public void setValuesArray(String[] valuesArray) {
		_valuesArray = valuesArray;

		super.setValues(StringUtil.merge(valuesArray));
	}

	private String[] _valuesArray;

}