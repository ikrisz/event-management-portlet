package com.pfiks.intelligus.events.constants;

import com.liferay.portal.security.permission.ActionKeys;

public enum PrivacySettingConstants {

    GUEST_PERMISSIONS_PUBLIC(new String[] { ActionKeys.VIEW }), //
    GUEST_PERMISSIONS_GROUP(new String[0]), //
    GROUP_PERMISSIONS_PUBLIC(new String[] { ActionKeys.VIEW, ActionKeys.ADD_DISCUSSION }), //
    GROUP_PERMISSIONS_GROUP(new String[] { ActionKeys.VIEW, ActionKeys.ADD_DISCUSSION });

    private final String[] actionKeys;

    private PrivacySettingConstants(final String[] settingActionKeys) {
	actionKeys = settingActionKeys;
    }

    public String[] getActionKeys() {
	return actionKeys;
    }

}
