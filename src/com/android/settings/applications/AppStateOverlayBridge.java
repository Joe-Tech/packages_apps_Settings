/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.settings.applications;

import android.Manifest;
import android.app.AppOpsManager;
import android.content.Context;
import android.util.Log;

import com.android.settingslib.applications.ApplicationsState;
import com.android.settingslib.applications.ApplicationsState.AppEntry;
import com.android.settingslib.applications.ApplicationsState.AppFilter;

/*
 * Connects info of apps that draw overlay to the ApplicationsState. Wraps around the generic
 * AppStateAppOpsBridge class to tailor to the semantics of SYSTEM_ALERT_WINDOW. Also provides app
 * filters that can use the info.
 */
public class AppStateOverlayBridge extends AppStateAppOpsBridge {

    private static final String TAG = "AppStateOverlayBridge";
    private static final int APP_OPS_OP_CODE = AppOpsManager.OP_SYSTEM_ALERT_WINDOW;
    private static final String PM_SYSTEM_ALERT_WINDOW = Manifest.permission.SYSTEM_ALERT_WINDOW;

    public AppStateOverlayBridge(Context context, ApplicationsState appState, Callback callback) {
        super(context, appState, callback, APP_OPS_OP_CODE, PM_SYSTEM_ALERT_WINDOW);
    }

    @Override
    protected void updateExtraInfo(AppEntry app, String pkg, int uid) {
        app.extraInfo = getOverlayInfo(pkg, uid);
    }

    public OverlayState getOverlayInfo(String pkg, int uid) {
        PermissionState permissionState = super.getPermissionInfo(pkg, uid);
        return new OverlayState(permissionState);
    }

    // TODO: figure out how to filter out system apps for this method
    public int getNumberOfPackagesWithPermission() {
        return super.getNumPackagesDeclaredPermission();
    }

    // TODO: figure out how to filter out system apps for this method
    public int getNumberOfPackagesCanDrawOverlay() {
        return super.getNumPackagesAllowedByAppOps();
    }

    public static class OverlayState {
        PermissionState mPermissionState;

        public OverlayState(PermissionState permissionState) {
            mPermissionState = permissionState;
        }

        public boolean isAllowed() {
            return mPermissionState.isPermissible();
        }
    }

    public static final AppFilter FILTER_SYSTEM_ALERT_WINDOW = new AppFilter() {
        @Override
        public void init() {
        }

        @Override
        public boolean filterApp(AppEntry info) {
            return info.extraInfo != null;
        }
    };
}