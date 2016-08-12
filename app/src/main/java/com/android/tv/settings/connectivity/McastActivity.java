//<MStar Software>
//******************************************************************************
// MStar Software
// Copyright (c) 2010 - 2015 MStar Semiconductor, Inc. All rights reserved.
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

package com.android.tv.settings.connectivity;
import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import com.android.tv.settings.R;
import android.os.SystemProperties;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.net.wifi.WifiManager;
import android.content.Context;
import android.util.Log;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.widget.Toast;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.text.TextUtils;

public class McastActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "McastActivity";
    private SwitchPreference  mEnableMcast;
    private Preference  mMcastName;
    private PreferenceScreen mPreferenceScreen;
    private WifiManager mWifiManager;
    private Context mContext;
    private boolean mEnable = false;
    private IntentFilter mFilter;
    private WifiP2pDevice mThisDevice;
    private int  mCurrentStatus;

    private static final String ENABLE_MCAST = "enable_mcast";
    private static final String MCAST_NAME = "mcast_name";
    private static final String LISTEN_NAME = "listen_device";
    private static final String ENABLE_LISTEN = "net.wfd.enable";
    private static final String SWITCH_ON = "On";
    private static final String LISTEN_ON = "mcast_listen";

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e(TAG, "on Receive action: " + action);
            if (action.equals(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)) {
                mThisDevice = (WifiP2pDevice)intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
                if (mThisDevice != null) {
                    if (TextUtils.isEmpty(mThisDevice.deviceName)) {
                        mMcastName.setTitle(mThisDevice.deviceAddress);
                     } else {
                        mMcastName.setTitle(mThisDevice.deviceName);
                     }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate");
        addPreferencesFromResource(R.xml.mcast);
        mPreferenceScreen = getPreferenceScreen();
        mContext = mPreferenceScreen.getContext();
        mFilter = new IntentFilter();
        mFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        mContext.registerReceiver(mReceiver,mFilter);

        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        mEnableMcast = (SwitchPreference)findPreference(ENABLE_MCAST);
        mCurrentStatus = getMCastListenStatus();
        if (mCurrentStatus == 1) {
            mEnableMcast.setChecked(true);
            SystemProperties.set(ENABLE_LISTEN,"1");
        } else {
            mEnableMcast.setChecked(false);
            SystemProperties.set(ENABLE_LISTEN," ");
        }
        mMcastName = (Preference)findPreference(MCAST_NAME);

        mEnableMcast.setOnPreferenceChangeListener(this);
        if (!mWifiManager.isWifiEnabled()) {
            Toast.makeText(mContext, "Mcast listen peer device only when wifi open.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
     protected void onStop() {
        super.onStop();
     }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
        mCurrentStatus = getMCastListenStatus();
        if (mCurrentStatus == 1) {
            mEnableMcast.setChecked(true);
        } else {
            mEnableMcast.setChecked(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy");
        mContext.unregisterReceiver(mReceiver);
    }

    public boolean onPreferenceChange(Preference preference, Object value) {

         boolean enable = (Boolean)value;
          if (enable) {
            Log.e(TAG, "Enable Mcast listen");
            SystemProperties.set(ENABLE_LISTEN,"1");
            setMcastListenStatus(enable);
            Toast.makeText(mContext, "MCast will always listen peer device in background",Toast.LENGTH_SHORT).show();
          } else {
            Log.e(TAG, "Disable Mcast listen");
            SystemProperties.set(ENABLE_LISTEN," ");
            setMcastListenStatus(enable);
            Toast.makeText(mContext, "MCast will stop to listen peer device",Toast.LENGTH_SHORT).show();
          }
          return true;
    }

    private void setMcastListenStatus(boolean enabled) {
        Settings.Global.putInt(mContext.getContentResolver(), LISTEN_ON,enabled ? 1 : 0);
    }

    private int getMCastListenStatus() {
        int listenOn ;
        listenOn = Settings.Global.getInt(mContext.getContentResolver(), LISTEN_ON, 0);
        return listenOn;
    }
}
