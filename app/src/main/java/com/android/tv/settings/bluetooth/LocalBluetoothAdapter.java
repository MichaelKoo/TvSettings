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

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.Set;

/**
 * LocalBluetoothAdapter provides an interface between the Settings app
 * and the functionality of the local {@link BluetoothAdapter}, specifically
 * those related to state transitions of the adapter itself.
 *
 * <p>Connection and bonding state changes affecting specific devices
 * are handled by {@link CachedBluetoothDeviceManager},
 * {@link BluetoothEventManager}, and {@link LocalBluetoothProfileManager}.
 */
public final class LocalBluetoothAdapter {
    private static final String TAG = "LocalBluetoothAdapter";

    /** This class does not allow direct access to the BluetoothAdapter. */
    private final BluetoothAdapter mAdapter;

    private LocalBluetoothProfileManager mProfileManager;

    private static LocalBluetoothAdapter sInstance;

    private int mState = BluetoothAdapter.ERROR;

    private static final int SCAN_EXPIRATION_MS = 5 * 60 * 1000; // 5 mins

    private long mLastScan;

    private LocalBluetoothAdapter(BluetoothAdapter adapter) {
        mAdapter = adapter;
    }

    void setProfileManager(LocalBluetoothProfileManager manager) {
        mProfileManager = manager;
    }

    /**
     * Get the singleton instance of the LocalBluetoothAdapter. If this device
     * doesn't support Bluetooth, then null will be returned. Callers must be
     * prepared to handle a null return value.
     * @return the LocalBluetoothAdapter object, or null if not supported
     */
    static synchronized LocalBluetoothAdapter getInstance() {
        if (sInstance == null) {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter != null) {
                sInstance = new LocalBluetoothAdapter(adapter);
            }
        }

        return sInstance;
    }

    // Pass-through BluetoothAdapter methods that we can intercept if necessary

    void cancelDiscovery() {
        mAdapter.cancelDiscovery();
    }

    boolean enable() {
        return mAdapter.enable();
    }

    boolean disable() {
        return mAdapter.disable();
    }

    void getProfileProxy(Context context,
            BluetoothProfile.ServiceListener listener, int profile) {
        mAdapter.getProfileProxy(context, listener, profile);
    }

    Set<BluetoothDevice> getBondedDevices() {
        return mAdapter.getBondedDevices();
    }

    String getName() {
        return mAdapter.getName();
    }

    int getScanMode() {
        return mAdapter.getScanMode();
    }

    int getState() {
        return mAdapter.getState();
    }

    ParcelUuid[] getUuids() {
        return mAdapter.getUuids();
    }

    boolean isDiscovering() {
        return mAdapter.isDiscovering();
    }

    boolean isEnabled() {
        return mAdapter.isEnabled();
    }

    void setDiscoverableTimeout(int timeout) {
        mAdapter.setDiscoverableTimeout(timeout);
    }

    void setName(String name) {
        mAdapter.setName(name);
    }

    void setScanMode(int mode) {
        mAdapter.setScanMode(mode);
    }

    boolean setScanMode(int mode, int duration) {
        return mAdapter.setScanMode(mode, duration);
    }

    void startScanning(boolean force) {
        // Only start if we're not already scanning
        if (!mAdapter.isDiscovering()) {
            if (!force) {
                // Don't scan more than frequently than SCAN_EXPIRATION_MS,
                // unless forced
                if (mLastScan + SCAN_EXPIRATION_MS > System.currentTimeMillis()) {
                    return;
                }

                // If we are playing music, don't scan unless forced.
                A2dpProfile a2dp = mProfileManager.getA2dpProfile();
                if (a2dp != null && a2dp.isA2dpPlaying()) {
                    return;
                }
            }

            if (mAdapter.startDiscovery()) {
                mLastScan = System.currentTimeMillis();
            }
        }
    }

    void stopScanning() {
        if (mAdapter.isDiscovering()) {
            mAdapter.cancelDiscovery();
        }
    }

    public synchronized int getBluetoothState() {
        // Always sync state, in case it changed while paused
        syncBluetoothState();
        return mState;
    }

    synchronized void setBluetoothStateInt(int state) {
        mState = state;

        if (state == BluetoothAdapter.STATE_ON) {
            // if mProfileManager hasn't been constructed yet, it will
            // get the adapter UUIDs in its constructor when it is.
            if (mProfileManager != null) {
                mProfileManager.setBluetoothStateOn();
            }
        }
    }

    // Returns true if the state changed; false otherwise.
    boolean syncBluetoothState() {
        int currentState = mAdapter.getState();
        if (currentState != mState) {
            setBluetoothStateInt(mAdapter.getState());
            return true;
        }
        return false;
    }

    public void setBluetoothEnabled(boolean enabled) {
        boolean success = enabled
                ? mAdapter.enable()
                : mAdapter.disable();

        if (success) {
            setBluetoothStateInt(enabled
                ? BluetoothAdapter.STATE_TURNING_ON
                : BluetoothAdapter.STATE_TURNING_OFF);
        } else {
            if (Utils.V) {
                Log.v(TAG, "setBluetoothEnabled call, manager didn't return " +
                        "success for enabled: " + enabled);
            }

            syncBluetoothState();
        }
    }
}
