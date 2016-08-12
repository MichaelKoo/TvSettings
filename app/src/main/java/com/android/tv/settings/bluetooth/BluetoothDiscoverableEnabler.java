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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.SystemProperties;
import android.preference.Preference;
import android.text.format.DateUtils;

import com.android.tv.settings.R;

import android.text.format.Time;
import android.util.Log;

/**
 * BluetoothDiscoverableEnabler is a helper to manage the "Discoverable"
 * checkbox. It sets/unsets discoverability and keeps track of how much time
 * until the the discoverability is automatically turned off.
 */
final class BluetoothDiscoverableEnabler implements Preference.OnPreferenceClickListener {

    private static final String TAG = "BluetoothDiscoverableEnabler";

    private static final String SYSTEM_PROPERTY_DISCOVERABLE_TIMEOUT =
            "debug.bt.discoverable_time";

    private static final int DISCOVERABLE_TIMEOUT_TWO_MINUTES = 120;
    private static final int DISCOVERABLE_TIMEOUT_FIVE_MINUTES = 300;
    private static final int DISCOVERABLE_TIMEOUT_ONE_HOUR = 3600;
    static final int DISCOVERABLE_TIMEOUT_NEVER = 0;

    // Bluetooth advanced settings screen was replaced with action bar items.
    // Use the same preference key for discoverable timeout as the old ListPreference.
    private static final String KEY_DISCOVERABLE_TIMEOUT = "bt_discoverable_timeout";

    private static final String VALUE_DISCOVERABLE_TIMEOUT_TWO_MINUTES = "twomin";
    private static final String VALUE_DISCOVERABLE_TIMEOUT_FIVE_MINUTES = "fivemin";
    private static final String VALUE_DISCOVERABLE_TIMEOUT_ONE_HOUR = "onehour";
    private static final String VALUE_DISCOVERABLE_TIMEOUT_NEVER = "never";

    static final int DEFAULT_DISCOVERABLE_TIMEOUT = DISCOVERABLE_TIMEOUT_TWO_MINUTES;

    private Context mContext;
    private final Handler mUiHandler;
    private final Preference mDiscoveryPreference;

    private final LocalBluetoothAdapter mLocalAdapter;

    private final SharedPreferences mSharedPreferences;

    private boolean mDiscoverable;
    private int mNumberOfPairedDevices;

    private int mTimeoutSecs = -1;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(intent.getAction())) {
                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE,
                        BluetoothAdapter.ERROR);
                if (mode != BluetoothAdapter.ERROR) {
                    handleModeChanged(mode);
                }
            }
        }
    };

    private final Runnable mUpdateCountdownSummaryRunnable = new Runnable() {
        public void run() {
            updateCountdownSummary();
        }
    };

    BluetoothDiscoverableEnabler(LocalBluetoothAdapter adapter,
            Preference discoveryPreference) {
        mUiHandler = new Handler();
        mLocalAdapter = adapter;
        mDiscoveryPreference = discoveryPreference;
        mSharedPreferences = discoveryPreference.getSharedPreferences();
        discoveryPreference.setPersistent(false);
    }

    public void resume(Context context) {
        if (mLocalAdapter == null) {
            return;
        }

        if (mContext != context) {
            mContext = context;
        }

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        mContext.registerReceiver(mReceiver, filter);
        mDiscoveryPreference.setOnPreferenceClickListener(this);
        handleModeChanged(mLocalAdapter.getScanMode());
    }

    public void pause() {
        if (mLocalAdapter == null) {
            return;
        }

        mUiHandler.removeCallbacks(mUpdateCountdownSummaryRunnable);
        mContext.unregisterReceiver(mReceiver);
        mDiscoveryPreference.setOnPreferenceClickListener(null);
    }

    public boolean onPreferenceClick(Preference preference) {
        // toggle discoverability
        mDiscoverable = !mDiscoverable;
        setEnabled(mDiscoverable);
        return true;
    }

    private void setEnabled(boolean enable) {
        if (enable) {
            int timeout = getDiscoverableTimeout();
            long endTimestamp = System.currentTimeMillis() + timeout * 1000L;
            LocalBluetoothPreferences.persistDiscoverableEndTimestamp(mContext, endTimestamp);

            mLocalAdapter.setScanMode(BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE, timeout);
            updateCountdownSummary();

            Log.d(TAG, "setEnabled(): enabled = " + enable + "timeout = " + timeout);

            if (timeout > 0) {
                BluetoothDiscoverableTimeoutReceiver.setDiscoverableAlarm(mContext, endTimestamp);
            // porting from brcm
            } else if (mDiscoverable == true) {
                 //Already discoverable and only timeout got chnaged to "Never".
                 //Cancel the running timer if any
                 BluetoothDiscoverableTimeoutReceiver.cancelDiscoverableAlarm(mContext);
            }
        } else {
            mLocalAdapter.setScanMode(BluetoothAdapter.SCAN_MODE_CONNECTABLE);
            BluetoothDiscoverableTimeoutReceiver.cancelDiscoverableAlarm(mContext);
        }
    }

    private void updateTimerDisplay(int timeout) {
        if (getDiscoverableTimeout() == DISCOVERABLE_TIMEOUT_NEVER) {
            mDiscoveryPreference.setSummary(R.string.bluetooth_is_discoverable_always);
        } else {
            String textTimeout = formatTimeRemaining(timeout);
            mDiscoveryPreference.setSummary(mContext.getString(R.string.bluetooth_is_discoverable,
                    textTimeout));
        }
    }

    private static String formatTimeRemaining(int timeout) {
        StringBuilder sb = new StringBuilder(6);    // "mmm:ss"
        int min = timeout / 60;
        sb.append(min).append(':');
        int sec = timeout - (min * 60);
        if (sec < 10) {
            sb.append('0');
        }
        sb.append(sec);
        return sb.toString();
    }

    void setDiscoverableTimeout(int index) {
        String timeoutValue;
        switch (index) {
            case 0:
            default:
                mTimeoutSecs = DISCOVERABLE_TIMEOUT_TWO_MINUTES;
                timeoutValue = VALUE_DISCOVERABLE_TIMEOUT_TWO_MINUTES;
                break;

            case 1:
                mTimeoutSecs = DISCOVERABLE_TIMEOUT_FIVE_MINUTES;
                timeoutValue = VALUE_DISCOVERABLE_TIMEOUT_FIVE_MINUTES;
                break;

            case 2:
                mTimeoutSecs = DISCOVERABLE_TIMEOUT_ONE_HOUR;
                timeoutValue = VALUE_DISCOVERABLE_TIMEOUT_ONE_HOUR;
                break;

            case 3:
                mTimeoutSecs = DISCOVERABLE_TIMEOUT_NEVER;
                timeoutValue = VALUE_DISCOVERABLE_TIMEOUT_NEVER;
                break;
        }
        mSharedPreferences.edit().putString(KEY_DISCOVERABLE_TIMEOUT, timeoutValue).apply();
        setEnabled(true);   // enable discovery and reset timer
    }

    private int getDiscoverableTimeout() {
        if (mTimeoutSecs != -1) {
            return mTimeoutSecs;
        }

        int timeout = SystemProperties.getInt(SYSTEM_PROPERTY_DISCOVERABLE_TIMEOUT, -1);
        if (timeout < 0) {
            String timeoutValue = mSharedPreferences.getString(KEY_DISCOVERABLE_TIMEOUT,
                    VALUE_DISCOVERABLE_TIMEOUT_TWO_MINUTES);

            if (timeoutValue.equals(VALUE_DISCOVERABLE_TIMEOUT_NEVER)) {
                timeout = DISCOVERABLE_TIMEOUT_NEVER;
            } else if (timeoutValue.equals(VALUE_DISCOVERABLE_TIMEOUT_ONE_HOUR)) {
                timeout = DISCOVERABLE_TIMEOUT_ONE_HOUR;
            } else if (timeoutValue.equals(VALUE_DISCOVERABLE_TIMEOUT_FIVE_MINUTES)) {
                timeout = DISCOVERABLE_TIMEOUT_FIVE_MINUTES;
            } else {
                timeout = DISCOVERABLE_TIMEOUT_TWO_MINUTES;
            }
        }
        mTimeoutSecs = timeout;
        return timeout;
    }

    int getDiscoverableTimeoutIndex() {
        int timeout = getDiscoverableTimeout();
        switch (timeout) {
            case DISCOVERABLE_TIMEOUT_TWO_MINUTES:
            default:
                return 0;

            case DISCOVERABLE_TIMEOUT_FIVE_MINUTES:
                return 1;

            case DISCOVERABLE_TIMEOUT_ONE_HOUR:
                return 2;

            case DISCOVERABLE_TIMEOUT_NEVER:
                return 3;
        }
    }

    void setNumberOfPairedDevices(int pairedDevices) {
        mNumberOfPairedDevices = pairedDevices;
        handleModeChanged(mLocalAdapter.getScanMode());
    }

    void handleModeChanged(int mode) {
        Log.d(TAG, "handleModeChanged(): mode = " + mode);
        if (mode == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            mDiscoverable = true;
            updateCountdownSummary();
        } else {
            mDiscoverable = false;
            setSummaryNotDiscoverable();
        }
    }

    private void setSummaryNotDiscoverable() {
        if (mNumberOfPairedDevices != 0) {
            mDiscoveryPreference.setSummary(R.string.bluetooth_only_visible_to_paired_devices);
        } else {
            mDiscoveryPreference.setSummary(R.string.bluetooth_not_visible_to_other_devices);
        }
    }

    private void updateCountdownSummary() {
        int mode = mLocalAdapter.getScanMode();
        if (mode != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            return;
        }

        long currentTimestamp = System.currentTimeMillis();
        long endTimestamp = LocalBluetoothPreferences.getDiscoverableEndTimestamp(mContext);

        if (currentTimestamp > endTimestamp) {
            // We're still in discoverable mode, but maybe there isn't a timeout.
            updateTimerDisplay(0);
            return;
        }

        int timeLeft = (int) ((endTimestamp - currentTimestamp) / 1000L);
        updateTimerDisplay(timeLeft);

        synchronized (this) {
            mUiHandler.removeCallbacks(mUpdateCountdownSummaryRunnable);
            mUiHandler.postDelayed(mUpdateCountdownSummaryRunnable, 1000);
        }
    }
}
