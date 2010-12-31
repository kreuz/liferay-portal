/**
 * Copyright (c) 2000-2011 Liferay, Inc. All rights reserved.
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

package com.liferay.portlet.assetpublisher.action;

import com.liferay.portal.kernel.portlet.BaseConfigurationAction;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.WebKeys;
import com.liferay.portlet.PortletPreferencesFactoryUtil;
import com.liferay.portlet.asset.AssetTagException;
import com.liferay.portlet.asset.service.AssetTagLocalServiceUtil;
import com.liferay.portlet.assetpublisher.util.AssetPublisherUtil;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

/**
 * @author Brian Wing Shun Chan
 */
public class ConfigurationActionImpl extends BaseConfigurationAction {

	public void processAction(
			PortletConfig portletConfig, ActionRequest actionRequest,
			ActionResponse actionResponse)
		throws Exception {

		String cmd = ParamUtil.getString(actionRequest, Constants.CMD);

		String portletResource = ParamUtil.getString(
			actionRequest, "portletResource");

		PortletPreferences preferences =
			PortletPreferencesFactoryUtil.getPortletSetup(
				actionRequest, portletResource);

		if (cmd.equals(Constants.UPDATE)) {
			updateDisplaySettings(actionRequest);

			String selectionStyle = getParameter(
				actionRequest, "selectionStyle");

			if (selectionStyle.equals("dynamic")) {
				updateQueryLogic(actionRequest, preferences);
			}

			super.processAction(portletConfig, actionRequest, actionResponse);
		}
		else {
			try {
				if (cmd.equals("add-selection")) {
					AssetPublisherUtil.addSelection(actionRequest, preferences);
				}
				else if (cmd.equals("move-selection-down")) {
					moveSelectionDown(actionRequest, preferences);
				}
				else if (cmd.equals("move-selection-up")) {
					moveSelectionUp(actionRequest, preferences);
				}
				else if (cmd.equals("remove-selection")) {
					removeSelection(actionRequest, preferences);
				}
				else if (cmd.equals("selection-style")) {
					setSelectionStyle(actionRequest, preferences);
				}

				if (SessionErrors.isEmpty(actionRequest)) {
					preferences.store();

					SessionMessages.add(
						actionRequest,
						portletConfig.getPortletName() + ".doConfigure");
				}

				actionResponse.sendRedirect(
					ParamUtil.getString(actionRequest, "redirect"));
			}
			catch (Exception e) {
				if (e instanceof AssetTagException) {
					SessionErrors.add(actionRequest, e.getClass().getName(), e);
				}
				else {
					throw e;
				}
			}
		}
	}

	public String render(
			PortletConfig portletConfig, RenderRequest renderRequest,
			RenderResponse renderResponse)
		throws Exception {

		return "/html/portlet/asset_publisher/configuration.jsp";
	}

	protected void moveSelectionDown(
			ActionRequest actionRequest, PortletPreferences preferences)
		throws Exception {

		int assetEntryOrder = ParamUtil.getInteger(
			actionRequest, "assetEntryOrder");

		String[] manualEntries = preferences.getValues(
			"assetEntryXml", new String[0]);

		if ((assetEntryOrder >= (manualEntries.length - 1)) ||
			(assetEntryOrder < 0)) {

			return;
		}

		String temp = manualEntries[assetEntryOrder + 1];

		manualEntries[assetEntryOrder + 1] = manualEntries[assetEntryOrder];
		manualEntries[assetEntryOrder] = temp;

		preferences.setValues("assetEntryXml", manualEntries);
	}

	protected void moveSelectionUp(
			ActionRequest actionRequest, PortletPreferences preferences)
		throws Exception {

		int assetEntryOrder = ParamUtil.getInteger(
			actionRequest, "assetEntryOrder");

		String[] manualEntries = preferences.getValues(
			"assetEntryXml", new String[0]);

		if ((assetEntryOrder >= manualEntries.length) ||
			(assetEntryOrder <= 0)) {

			return;
		}

		String temp = manualEntries[assetEntryOrder - 1];

		manualEntries[assetEntryOrder - 1] = manualEntries[assetEntryOrder];
		manualEntries[assetEntryOrder] = temp;

		preferences.setValues("assetEntryXml", manualEntries);
	}

	protected void removeSelection(
			ActionRequest actionRequest, PortletPreferences preferences)
		throws Exception {

		int assetEntryOrder = ParamUtil.getInteger(
			actionRequest, "assetEntryOrder");

		String[] manualEntries = preferences.getValues(
			"assetEntryXml", new String[0]);

		if (assetEntryOrder >= manualEntries.length) {
			return;
		}

		String[] newEntries = new String[manualEntries.length -1];

		int i = 0;
		int j = 0;

		for (; i < manualEntries.length; i++) {
			if (i != assetEntryOrder) {
				newEntries[j++] = manualEntries[i];
			}
		}

		preferences.setValues("assetEntryXml", newEntries);
	}

	protected void setSelectionStyle(
			ActionRequest actionRequest, PortletPreferences preferences)
		throws Exception {

		String selectionStyle = getParameter(actionRequest, "selectionStyle");
		String displayStyle = getParameter(actionRequest, "displayStyle");

		preferences.setValue("selectionStyle", selectionStyle);

		if (selectionStyle.equals("manual") ||
			selectionStyle.equals("view-count")) {

			preferences.setValue("showQueryLogic", String.valueOf(false));
		}

		if (!selectionStyle.equals("view-count") &&
			displayStyle.equals("view-count-details")) {

			preferences.setValue("displayStyle", "full-content");
		}
	}

	protected void updateDisplaySettings(ActionRequest actionRequest)
		throws Exception {

		String[] classNameIds = StringUtil.split(
			getParameter(actionRequest, "classNameIds"));

		String[] extensions = actionRequest.getParameterValues("extensions");

		String[] scopeIds = StringUtil.split(
			getParameter(actionRequest, "scopeIds"));

		setPreference(actionRequest, "classNameIds", classNameIds);
		setPreference(actionRequest, "extensions", extensions);
		setPreference(actionRequest, "scopeIds", scopeIds);
	}

	protected void updateQueryLogic(
			ActionRequest actionRequest, PortletPreferences preferences)
		throws Exception {

		ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		long userId = themeDisplay.getUserId();
		long groupId = themeDisplay.getScopeGroupId();

		int[] queryRulesIndexes = StringUtil.split(
			ParamUtil.getString(actionRequest, "queryLogicIndexes"), 0);

		int i = 0;

		for (int queryRulesIndex : queryRulesIndexes) {
			boolean contains = ParamUtil.getBoolean(
				actionRequest, "queryContains" + queryRulesIndex);
			boolean andOperator = ParamUtil.getBoolean(
				actionRequest, "queryAndOperator" + queryRulesIndex);
			String name = ParamUtil.getString(
				actionRequest, "queryName" + queryRulesIndex);

			String[] values = null;

			if (name.equals("assetTags")) {
				values = StringUtil.split(ParamUtil.getString(
					actionRequest, "queryTagNames" + queryRulesIndex));

				AssetTagLocalServiceUtil.checkTags(userId, groupId, values);
			}
			else {
				values = StringUtil.split(ParamUtil.getString(
					actionRequest, "queryCategoryIds" + queryRulesIndex));
			}

			setPreference(
				actionRequest, "queryContains" + i, String.valueOf(contains));
			setPreference(
				actionRequest, "queryAndOperator" + i,
				String.valueOf(andOperator));
			setPreference(actionRequest, "queryName" + i, name);
			setPreference(actionRequest, "queryValues" + i, values);

			i++;
		}

		// Clear previous preferences that are now blank

		String[] values = preferences.getValues(
			"queryValues" + i, new String[0]);

		while (values.length > 0) {
			setPreference(actionRequest, "queryContains" + i, StringPool.BLANK);
			setPreference(
				actionRequest, "queryAndOperator" + i, StringPool.BLANK);
			setPreference(actionRequest, "queryName" + i, StringPool.BLANK);
			setPreference(actionRequest, "queryValues" + i, new String[0]);

			i++;

			values = preferences.getValues("queryValues" + i, new String[0]);
		}
	}

}