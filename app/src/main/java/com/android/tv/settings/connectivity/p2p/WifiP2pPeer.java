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

package com.android.tv.settings.connectivity.p2p;

import com.android.tv.settings.R;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.preference.Preference;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

public class WifiP2pPeer extends Preference {

    private static final int[] STATE_SECURED = {R.attr.state_encrypted};
    public WifiP2pDevice device;

    private final int mRssi;
    private ImageView mSignal;

    private static final int SIGNAL_LEVELS = 4;

    public WifiP2pPeer(Context context, WifiP2pDevice dev) {
        super(context);
        device = dev;
        setWidgetLayoutResource(R.layout.preference_widget_wifi_signal);
        mRssi = 60; //TODO: fix
    }

    @Override
    protected void onBindView(View view) {
        if (TextUtils.isEmpty(device.deviceName)) {
            setTitle(device.deviceAddress);
        } else {
            setTitle(device.deviceName);
        }
        mSignal = (ImageView) view.findViewById(R.id.signal);
        if (mRssi == Integer.MAX_VALUE) {
            mSignal.setImageDrawable(null);
        } else {
            mSignal.setImageResource(R.drawable.wifi_signal);
            mSignal.setImageState(STATE_SECURED,  true);
        }
        refresh();
        super.onBindView(view);
    }

    @Override
    public int compareTo(Preference preference) {
        if (!(preference instanceof WifiP2pPeer)) {
            return 1;
        }
        WifiP2pPeer other = (WifiP2pPeer) preference;

        // devices go in the order of the status
        if (device.status != other.device.status) {
            return device.status < other.device.status ? -1 : 1;
        }

        // Sort by name/address
        if (device.deviceName != null) {
            return device.deviceName.compareToIgnoreCase(other.device.deviceName);
        }

        return device.deviceAddress.compareToIgnoreCase(other.device.deviceAddress);
    }

    int getLevel() {
        if (mRssi == Integer.MAX_VALUE) {
            return -1;
        }
        return WifiManager.calculateSignalLevel(mRssi, SIGNAL_LEVELS);
    }

    private void refresh() {
        if (mSignal == null) {
            return;
        }
        Context context = getContext();
        mSignal.setImageLevel(getLevel());
        String[] statusArray = context.getResources().getStringArray(R.array.wifi_p2p_status);
        setSummary(statusArray[device.status]);
    }
}
