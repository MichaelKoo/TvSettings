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

import android.content.Context;
import android.util.Log;

/**
 * LocalBluetoothManager provides a simplified interface on top of a subset of
 * the Bluetooth API. Note that {@link #getInstance} will return null
 * if there is no Bluetooth adapter on this device, and callers must be
 * prepared to handle this case.
 */
public final class LocalBluetoothManager {
    private static final String TAG = "LocalBluetoothManager";

    /** Singleton instance. */
    private static LocalBluetoothManager sInstance;

    private final Context mContext;

    /** If a BT-related activity is in the foreground, this will be it. */
    private Context mForegroundActivity;

    private BluetoothDiscoverableEnabler mDiscoverableEnabler;

    private final LocalBluetoothAdapter mLocalAdapter;

    private final CachedBluetoothDeviceManager mCachedDeviceManager;

    /** The Bluetooth profile manager. */
    private final LocalBluetoothProfileManager mProfileManager;

    /** The broadcast receiver event manager. */
    private final BluetoothEventManager mEventManager;

    public static synchronized LocalBluetoothManager getInstance(Context context) {
        if (sInstance == null) {
            LocalBluetoothAdapter adapter = LocalBluetoothAdapter.getInstance();
            if (adapter == null) {
                return null;
            }
            // This will be around as long as this process is
            Context appContext = context.getApplicationContext();
            sInstance = new LocalBluetoothManager(adapter, appContext);
        }

        return sInstance;
    }

    public void setDiscoverableEnabler(BluetoothDiscoverableEnabler discoverableEnabler) {
        mDiscoverableEnabler = discoverableEnabler;
    }

    public BluetoothDiscoverableEnabler getDiscoverableEnabler() {
        return mDiscoverableEnabler;
    }

    private LocalBluetoothManager(LocalBluetoothAdapter adapter, Context context) {
        mContext = context;
        mLocalAdapter = adapter;

        // proting from brcm
        mCachedDeviceManager = new CachedBluetoothDeviceManager(context,
                        LocalBluetoothManager.this,
                        adapter);
        mEventManager = new BluetoothEventManager(mLocalAdapter,
                mCachedDeviceManager, context);
        mProfileManager = new LocalBluetoothProfileManager(context,
                mLocalAdapter, mCachedDeviceManager, mEventManager);
    }

    public LocalBluetoothAdapter getBluetoothAdapter() {
        return mLocalAdapter;
    }

    public Context getContext() {
        return mContext;
    }

    public Context getForegroundActivity() {
        return mForegroundActivity;
    }

    boolean isForegroundActivity() {
        return mForegroundActivity != null;
    }

    synchronized void setForegroundActivity(Context context) {
        if (context != null) {
            Log.d(TAG, "setting foreground activity to non-null context");
            mForegroundActivity = context;
        } else {
            if (mForegroundActivity != null) {
                Log.d(TAG, "setting foreground activity to null");
                mForegroundActivity = null;
            }
        }
    }

    CachedBluetoothDeviceManager getCachedDeviceManager() {
        return mCachedDeviceManager;
    }

    BluetoothEventManager getEventManager() {
        return mEventManager;
    }

    LocalBluetoothProfileManager getProfileManager() {
        return mProfileManager;
    }
}
