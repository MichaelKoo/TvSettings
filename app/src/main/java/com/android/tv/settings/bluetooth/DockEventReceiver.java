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

import android.app.Service;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

public final class DockEventReceiver extends BroadcastReceiver {

    private static final boolean DEBUG = DockService.DEBUG;

    private static final String TAG = "DockEventReceiver";

    public static final String ACTION_DOCK_SHOW_UI =
        "com.android.tv.settings.bluetooth.action.DOCK_SHOW_UI";

    private static final int EXTRA_INVALID = -1234;

    private static final Object sStartingServiceSync = new Object();

    private static PowerManager.WakeLock sStartingService;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null)
            return;

        int state = intent.getIntExtra(Intent.EXTRA_DOCK_STATE, intent.getIntExtra(
                BluetoothAdapter.EXTRA_STATE, EXTRA_INVALID));
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        if (DEBUG) {
            Log.d(TAG, "Action: " + intent.getAction() + " State:" + state + " Device: "
                    + (device == null ? "null" : device.getAliasName()));
        }

        if (Intent.ACTION_DOCK_EVENT.equals(intent.getAction())
                || ACTION_DOCK_SHOW_UI.endsWith(intent.getAction())) {
            if ((device == null) && (ACTION_DOCK_SHOW_UI.endsWith(intent.getAction()) ||
                    ((state != Intent.EXTRA_DOCK_STATE_UNDOCKED) &&
                     (state != Intent.EXTRA_DOCK_STATE_LE_DESK)))) {
                if (DEBUG) Log.d(TAG,
                        "Wrong state: "+state+" or intent: "+intent.toString()+" with null device");
                return;
            }

            switch (state) {
                case Intent.EXTRA_DOCK_STATE_UNDOCKED:
                case Intent.EXTRA_DOCK_STATE_CAR:
                case Intent.EXTRA_DOCK_STATE_DESK:
                case Intent.EXTRA_DOCK_STATE_LE_DESK:
                case Intent.EXTRA_DOCK_STATE_HE_DESK:
                    Intent i = new Intent(intent);
                    i.setClass(context, DockService.class);
                    beginStartingService(context, i);
                    break;
                default:
                    Log.e(TAG, "Unknown state: " + state);
                    break;
            }
        } else if (BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED.equals(intent.getAction()) ||
                   BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED.equals(intent.getAction())) {
            int newState = intent.getIntExtra(BluetoothProfile.EXTRA_STATE,
                    BluetoothProfile.STATE_CONNECTED);
            int oldState = intent.getIntExtra(BluetoothProfile.EXTRA_PREVIOUS_STATE, 0);

            /*
             *  Reconnect to the dock if:
             *  1) it is a dock
             *  2) it is disconnected
             *  3) the disconnect is initiated remotely
             *  4) the dock is still docked (check can only be done in the Service)
             */
            if (device == null) {
                if (DEBUG) Log.d(TAG, "Device is missing");
                return;
            }

            if (newState == BluetoothProfile.STATE_DISCONNECTED &&
                    oldState != BluetoothProfile.STATE_DISCONNECTING) {
                // Too bad, the dock state can't be checked from a BroadcastReceiver.
                Intent i = new Intent(intent);
                i.setClass(context, DockService.class);
                beginStartingService(context, i);
            }

        } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
            int btState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            if (btState != BluetoothAdapter.STATE_TURNING_ON) {
                Intent i = new Intent(intent);
                i.setClass(context, DockService.class);
                beginStartingService(context, i);
            }
        }
    }

    /**
     * Start the service to process the current event notifications, acquiring
     * the wake lock before returning to ensure that the service will run.
     */
    private static void beginStartingService(Context context, Intent intent) {
        synchronized (sStartingServiceSync) {
            if (sStartingService == null) {
                PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                sStartingService = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                        "StartingDockService");
            }

            sStartingService.acquire();

            if (context.startService(intent) == null) {
                Log.e(TAG, "Can't start DockService");
            }
        }
    }

    /**
     * Called back by the service when it has finished processing notifications,
     * releasing the wake lock if the service is now stopping.
     */
    public static void finishStartingService(Service service, int startId) {
        synchronized (sStartingServiceSync) {
            if (sStartingService != null) {
                if (DEBUG) Log.d(TAG, "stopSelf id = " + startId);
                if (service.stopSelfResult(startId)) {
                    Log.d(TAG, "finishStartingService: stopping service");
                    sStartingService.release();
                }
            }
        }
    }
}
