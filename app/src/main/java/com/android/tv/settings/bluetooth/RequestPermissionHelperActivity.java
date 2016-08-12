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

import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;
import com.android.tv.settings.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * RequestPermissionHelperActivity asks the user whether to enable discovery.
 * This is usually started by RequestPermissionActivity.
 */
public class RequestPermissionHelperActivity extends AlertActivity implements
        DialogInterface.OnClickListener {
    private static final String TAG = "RequestPermissionHelperActivity";

    public static final String ACTION_INTERNAL_REQUEST_BT_ON =
        "com.android.tv.settings.bluetooth.ACTION_INTERNAL_REQUEST_BT_ON";

    public static final String ACTION_INTERNAL_REQUEST_BT_ON_AND_DISCOVERABLE =
        "com.android.tv.settings.bluetooth.ACTION_INTERNAL_REQUEST_BT_ON_AND_DISCOVERABLE";

    private LocalBluetoothAdapter mLocalAdapter;

    private int mTimeout;

    // True if requesting BT to be turned on
    // False if requesting BT to be turned on + discoverable mode
    private boolean mEnableOnly;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Note: initializes mLocalAdapter and returns true on error
        if (parseIntent()) {
            finish();
            return;
        }

        createDialog();

        if (getResources().getBoolean(R.bool.auto_confirm_bluetooth_activation_dialog) == true) {
            // dismiss dialog immediately if settings say so
            onClick(null, BUTTON_POSITIVE);
            dismiss();
        }
    }

    void createDialog() {
        final AlertController.AlertParams p = mAlertParams;

        if (mEnableOnly) {
            p.mMessage = getString(R.string.bluetooth_ask_enablement);
        } else {
            if (mTimeout == BluetoothDiscoverableEnabler.DISCOVERABLE_TIMEOUT_NEVER) {
                p.mMessage = getString(R.string.bluetooth_ask_enablement_and_lasting_discovery);
            } else {
                p.mMessage = getString(R.string.bluetooth_ask_enablement_and_discovery, mTimeout);
            }
        }

        p.mPositiveButtonText = getString(R.string.allow);
        p.mPositiveButtonListener = this;
        p.mNegativeButtonText = getString(R.string.deny);
        p.mNegativeButtonListener = this;

        setupAlert();
    }

    public void onClick(DialogInterface dialog, int which) {
        int returnCode;
        // FIXME: fix this ugly switch logic!
        switch (which) {
            case BUTTON_POSITIVE:
                int btState = 0;

                try {
                    // TODO There's a better way.
                    int retryCount = 30;
                    do {
                        btState = mLocalAdapter.getBluetoothState();
                        Thread.sleep(100);
                    } while (btState == BluetoothAdapter.STATE_TURNING_OFF && --retryCount > 0);
                } catch (InterruptedException ignored) {
                    // don't care
                }

                if (btState == BluetoothAdapter.STATE_TURNING_ON
                        || btState == BluetoothAdapter.STATE_ON
                        || mLocalAdapter.enable()) {
                    returnCode = RequestPermissionActivity.RESULT_BT_STARTING_OR_STARTED;
                } else {
                    returnCode = RESULT_CANCELED;
                }
                break;

            case BUTTON_NEGATIVE:
                returnCode = RESULT_CANCELED;
                break;
            default:
                return;
        }
        setResult(returnCode);
    }

    /**
     * Parse the received Intent and initialize mLocalBluetoothAdapter.
     * @return true if an error occurred; false otherwise
     */
    private boolean parseIntent() {
        Intent intent = getIntent();
        if (intent != null && intent.getAction().equals(ACTION_INTERNAL_REQUEST_BT_ON)) {
            mEnableOnly = true;
        } else if (intent != null
                && intent.getAction().equals(ACTION_INTERNAL_REQUEST_BT_ON_AND_DISCOVERABLE)) {
            mEnableOnly = false;
            // Value used for display purposes. Not range checking.
            mTimeout = intent.getIntExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,
                    BluetoothDiscoverableEnabler.DEFAULT_DISCOVERABLE_TIMEOUT);
        } else {
            setResult(RESULT_CANCELED);
            return true;
        }

        LocalBluetoothManager manager = LocalBluetoothManager.getInstance(this);
        if (manager == null) {
            Log.e(TAG, "Error: there's a problem starting Bluetooth");
            setResult(RESULT_CANCELED);
            return true;
        }
        mLocalAdapter = manager.getBluetoothAdapter();

        return false;
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}
