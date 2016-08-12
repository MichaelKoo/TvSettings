//<MStar Software>
//******************************************************************************
// MStar Software
// Copyright (c) 2010 - 2014 MStar Semiconductor, Inc. All rights reserved.
// All software, firmware and related documentation herein ("MStar Software") are
// intellectual property of MStar Semiconductor, Inc. ("MStar") and protected by
// law, including, but not limited to, copyright law and international treaties.
// Any use, modification, reproduction, retransmission, or republication of all
// or part of MStar Software is expressly prohibited, unless prior written
// permission has been granted by MStar.
//
// By accessing, browsing and/or using MStar Software, you acknowledge that you
// have read, understood, and agree, to be bound by below terms ("Terms") and to
// comply with all applicable laws and regulations:
//
// 1. MStar shall retain any and all right, ownership and interest to MStar
//    Software and any modification/derivatives thereof.
//    No right, ownership, or interest to MStar Software and any
//    modification/derivatives thereof is transferred to you under Terms.
//
// 2. You understand that MStar Software might include, incorporate or be
//    supplied together with third party's software and the use of MStar
//    Software may require additional licenses from third parties.
//    Therefore, you hereby agree it is your sole responsibility to separately
//    obtain any and all third party right and license necessary for your use of
//    such third party's software.
//
// 3. MStar Software and any modification/derivatives thereof shall be deemed as
//    MStar's confidential information and you agree to keep MStar's
//    confidential information in strictest confidence and not disclose to any
//    third party.
//
// 4. MStar Software is provided on an "AS IS" basis without warranties of any
//    kind. Any warranties are hereby expressly disclaimed by MStar, including
//    without limitation, any warranties of merchantability, non-infringement of
//    intellectual property rights, fitness for a particular purpose, error free
//    and in conformity with any international standard.  You agree to waive any
//    claim against MStar for any loss, damage, cost or expense that you may
//    incur related to your use of MStar Software.
//    In no event shall MStar be liable for any direct, indirect, incidental or
//    consequential damages, including without limitation, lost of profit or
//    revenues, lost or damage of data, and unauthorized system use.
//    You agree that this Section 4 shall still apply without being affected
//    even if MStar Software has been modified by MStar in accordance with your
//    request or instruction for your use, except otherwise agreed by both
//    parties in writing.
//
// 5. If requested, MStar may from time to time provide technical supports or
//    services in relation with MStar Software to you for your use of
//    MStar Software in conjunction with your or your customer's product
//    ("Services").
//    You understand and agree that, except otherwise agreed by both parties in
//    writing, Services are provided on an "AS IS" basis and the warranty
//    disclaimer set forth in Section 4 above shall apply.
//
// 6. Nothing contained herein shall be construed as by implication, estoppels
//    or otherwise:
//    (a) conferring any license or right to use MStar name, trademark, service
//        mark, symbol or any other identification;
//    (b) obligating MStar or any of its affiliates to furnish any person,
//        including without limitation, you and your customers, any assistance
//        of any kind whatsoever, or any information; or
//    (c) conferring any license or right under any intellectual property right.
//
// 7. These terms shall be governed by and construed in accordance with the laws
//    of Taiwan, R.O.C., excluding its conflict of law rules.
//    Any and all dispute arising out hereof or related hereto shall be finally
//    settled by arbitration referred to the Chinese Arbitration Association,
//    Taipei in accordance with the ROC Arbitration Law and the Arbitration
//    Rules of the Association by three (3) arbitrators appointed in accordance
//    with the said Rules.
//    The place of arbitration shall be in Taipei, Taiwan and the language shall
//    be English.
//    The arbitration award shall be final and binding to both parties.
//
//******************************************************************************
//<MStar Software>

package com.android.tv.settings.bluetooth;

import android.app.QueuedWork;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.util.Log;

/**
 * LocalBluetoothPreferences provides an interface to the preferences
 * related to Bluetooth.
 */
final class LocalBluetoothPreferences {
    private static final String TAG = "LocalBluetoothPreferences";
    private static final boolean DEBUG = Utils.D;
    private static final String SHARED_PREFERENCES_NAME = "bluetooth_settings";

    // If a device was picked from the device picker or was in discoverable mode
    // in the last 60 seconds, show the pairing dialogs in foreground instead
    // of raising notifications
    private static final int GRACE_PERIOD_TO_SHOW_DIALOGS_IN_FOREGROUND = 60 * 1000;

    private static final String KEY_DISCOVERING_TIMESTAMP = "last_discovering_time";

    private static final String KEY_LAST_SELECTED_DEVICE = "last_selected_device";

    private static final String KEY_LAST_SELECTED_DEVICE_TIME = "last_selected_device_time";

    private static final String KEY_DOCK_AUTO_CONNECT = "auto_connect_to_dock";

    private static final String KEY_DISCOVERABLE_END_TIMESTAMP = "discoverable_end_timestamp";

    private LocalBluetoothPreferences() {
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    static long getDiscoverableEndTimestamp(Context context) {
        return getSharedPreferences(context).getLong(
                KEY_DISCOVERABLE_END_TIMESTAMP, 0);
    }

    static boolean shouldShowDialogInForeground(Context context,
            String deviceAddress) {
        LocalBluetoothManager manager = LocalBluetoothManager.getInstance(context);
        if (manager == null) {
            if(DEBUG) Log.v(TAG, "manager == null - do not show dialog.");
            return false;
        }

        // If Bluetooth Settings is visible
        if (manager.isForegroundActivity()) {
            return true;
        }

        // If in appliance mode, do not show dialog in foreground.
        if ((context.getResources().getConfiguration().uiMode &
                Configuration.UI_MODE_TYPE_APPLIANCE) == Configuration.UI_MODE_TYPE_APPLIANCE) {
            if (DEBUG) Log.v(TAG, "in appliance mode - do not show dialog.");
            return false;
        }

        long currentTimeMillis = System.currentTimeMillis();
        SharedPreferences sharedPreferences = getSharedPreferences(context);

        // If the device was in discoverABLE mode recently
        long lastDiscoverableEndTime = sharedPreferences.getLong(
                KEY_DISCOVERABLE_END_TIMESTAMP, 0);
        if ((lastDiscoverableEndTime + GRACE_PERIOD_TO_SHOW_DIALOGS_IN_FOREGROUND)
                > currentTimeMillis) {
            return true;
        }

        // If the device was discoverING recently
        LocalBluetoothAdapter adapter = manager.getBluetoothAdapter();
        if (adapter != null && adapter.isDiscovering()) {
            return true;
        } else if ((sharedPreferences.getLong(KEY_DISCOVERING_TIMESTAMP, 0) +
                GRACE_PERIOD_TO_SHOW_DIALOGS_IN_FOREGROUND) > currentTimeMillis) {
            return true;
        }

        // If the device was picked in the device picker recently
        if (deviceAddress != null) {
            String lastSelectedDevice = sharedPreferences.getString(
                    KEY_LAST_SELECTED_DEVICE, null);

            if (deviceAddress.equals(lastSelectedDevice)) {
                long lastDeviceSelectedTime = sharedPreferences.getLong(
                        KEY_LAST_SELECTED_DEVICE_TIME, 0);
                if ((lastDeviceSelectedTime + GRACE_PERIOD_TO_SHOW_DIALOGS_IN_FOREGROUND)
                        > currentTimeMillis) {
                    return true;
                }
            }
        }
        if (DEBUG) Log.v(TAG, "Found no reason to show the dialog - do not show dialog.");
        return false;
    }

    static void persistSelectedDeviceInPicker(Context context, String deviceAddress) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(KEY_LAST_SELECTED_DEVICE,
                deviceAddress);
        editor.putLong(KEY_LAST_SELECTED_DEVICE_TIME,
                System.currentTimeMillis());
        editor.apply();
    }

    static void persistDiscoverableEndTimestamp(Context context, long endTimestamp) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putLong(KEY_DISCOVERABLE_END_TIMESTAMP, endTimestamp);
        editor.apply();
    }

    static void persistDiscoveringTimestamp(final Context context) {
        // Load the shared preferences and edit it on a background
        // thread (but serialized!).
        QueuedWork.singleThreadExecutor().submit(new Runnable() {
                public void run() {
                    SharedPreferences.Editor editor = getSharedPreferences(context).edit();
                    editor.putLong(
                            KEY_DISCOVERING_TIMESTAMP,
                        System.currentTimeMillis());
                    editor.apply();
                }
            });
    }

    static boolean hasDockAutoConnectSetting(Context context, String addr) {
        return getSharedPreferences(context).contains(KEY_DOCK_AUTO_CONNECT + addr);
    }

    static boolean getDockAutoConnectSetting(Context context, String addr) {
        return getSharedPreferences(context).getBoolean(KEY_DOCK_AUTO_CONNECT + addr,
                false);
    }

    static void saveDockAutoConnectSetting(Context context, String addr, boolean autoConnect) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(KEY_DOCK_AUTO_CONNECT + addr, autoConnect);
        editor.apply();
    }

    static void removeDockAutoConnectSetting(Context context, String addr) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.remove(KEY_DOCK_AUTO_CONNECT + addr);
        editor.apply();
    }
}
