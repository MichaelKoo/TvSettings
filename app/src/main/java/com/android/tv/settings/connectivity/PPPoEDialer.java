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

package com.android.tv.settings.connectivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.mstar.android.pppoe.PPPOE_STA;
import com.mstar.android.pppoe.PppoeManager;

import com.android.tv.settings.R;

public class PPPoEDialer {

    private static final String TAG = "PPPoEDialer";

    private Context mContext;

    private Handler mHandler;

    public static final int PPPOE_STATE_NONE = -1;

    public static final int PPPOE_STATE_CONNECT = 0;

    public static final int PPPOE_STATE_DISCONNECT = 1;

    public static final int PPPOE_STATE_CONNECTING = 2;

    public static final int PPPOE_STATE_AUTHFAILED = 3;

    public static final int PPPOE_STATE_FAILED = 4;

    public PPPoEDialer(Context context, Handler handler) {
        super();
        mContext = context;
        mHandler = handler;
    }

    public void exit() {
        mContext.unregisterReceiver(mPppoeReceiver);
    }

    public void dial(String user, String passwd) {
        PppoeManager mPppoeManager = PppoeManager.getInstance(mContext);
        if (PPPOE_STA.CONNECTING == mPppoeManager.PppoeGetStatus()) {
            Log.d(TAG, "CONNECTING.....");
            return;
        }

        // check username and password
        if (TextUtils.isEmpty(user) || TextUtils.isEmpty(passwd)) {
            return;
        } else {
            String ifName = "eth0";
            Log.d(TAG, "wire pppoe");
            mPppoeManager.PppoeSetInterface(ifName);
            mPppoeManager.PppoeSetUser(user);
            mPppoeManager.PppoeSetPW(passwd);
            mPppoeManager.PppoeDialup();
        }
    }

    public boolean isConnected() {
        PppoeManager mPppoeManager = PppoeManager.getInstance(mContext);
        if(mPppoeManager.getPppoeStatus().equals(mPppoeManager.PPPOE_STATE_CONNECT))
            return true;
        else
            return false;
    }

    public String getUser() {
        PppoeManager mPppoeManager = PppoeManager.getInstance(mContext);
        return mPppoeManager.PppoeGetUser();
    }

    public String getPasswd() {
        PppoeManager mPppoeManager = PppoeManager.getInstance(mContext);
        return mPppoeManager.PppoeGetPW();
    }

    public void setUser(String user) {
        PppoeManager mPppoeManager = PppoeManager.getInstance(mContext);
        mPppoeManager.PppoeSetUser(user);
    }

    public void setPasswd(String passwd) {
        PppoeManager mPppoeManager = PppoeManager.getInstance(mContext);
        mPppoeManager.PppoeSetPW(passwd);
    }

    public void hangup() {
        PppoeManager mPppoeManager = PppoeManager.getInstance(mContext);
        mPppoeManager.PppoeHangUp();
        Log.d(TAG, "ppppoe hang up");
    }

    private BroadcastReceiver mPppoeReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "action, " + action);
            if (!action.equals("com.mstar.android.pppoe.PPPOE_STATE_ACTION")) {
                return;
            }

            String status = intent.getStringExtra(PppoeManager.PPPOE_STATE_STATUE);
            Log.d(TAG, "#pppoestatus=" + status);
            if (null == status)
                return;

            Message message = new Message();
            message.what = PPPOE_STATE_NONE;
            if (status.equals(PppoeManager.PPPOE_STATE_CONNECT)) {
                Log.d(TAG, "@pppoe_connect");
                message.what = PPPOE_STATE_CONNECT;
            } else if (status.equals(PppoeManager.PPPOE_STATE_DISCONNECT)) {
                Log.d(TAG, "@pppoe_disconnect");
                message.what = PPPOE_STATE_DISCONNECT;
            } else if (status.equals(PppoeManager.PPPOE_STATE_CONNECTING)) {
                Log.d(TAG, "@pppoe_connecting");
                message.what = PPPOE_STATE_CONNECTING;
            } else if (status.equals(PppoeManager.PPPOE_STATE_AUTHFAILED)) {
                Log.d(TAG, "@pppoe_authfailed");
                message.what = PPPOE_STATE_AUTHFAILED;
            } else if (status.equals(PppoeManager.PPPOE_STATE_FAILED)) {
                Log.d(TAG, "@pppoe_failed");
                message.what = PPPOE_STATE_FAILED;
            }
            mHandler.sendMessage(message);
        }
    };

    public void registerPPPoEReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PppoeManager.PPPOE_STATE_ACTION);
        mContext.registerReceiver(mPppoeReceiver, intentFilter);
    }
}
