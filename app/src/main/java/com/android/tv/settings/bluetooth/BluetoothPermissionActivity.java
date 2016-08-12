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

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.Preference;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;

import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;

import com.android.tv.settings.R;

/**
 * BluetoothPermissionActivity shows a dialog for accepting incoming
 * profile connection request from untrusted devices.
 * It is also used to show a dialogue for accepting incoming phonebook
 * read request. The request could be initiated by PBAP PCE or by HF AT+CPBR.
 */
public class BluetoothPermissionActivity extends AlertActivity implements
        DialogInterface.OnClickListener, Preference.OnPreferenceChangeListener {
    private static final String TAG = "BluetoothPermissionActivity";
    private static final boolean DEBUG = Utils.D;

    private View mView;
    private TextView messageView;
    private Button mOkButton;
    private BluetoothDevice mDevice;
    private String mReturnPackage = null;
    private String mReturnClass = null;

    private int mRequestType = 0;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothDevice.ACTION_CONNECTION_ACCESS_CANCEL)) {
                int requestType = intent.getIntExtra(BluetoothDevice.EXTRA_ACCESS_REQUEST_TYPE,
                        BluetoothDevice.REQUEST_TYPE_PHONEBOOK_ACCESS);
                if (requestType != mRequestType) return;
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (mDevice.equals(device)) dismissDialog();
            }
        }
    };
    private boolean mReceiverRegistered = false;

    private void dismissDialog() {
        this.dismiss();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = getIntent();
        String action = i.getAction();
        if (!action.equals(BluetoothDevice.ACTION_CONNECTION_ACCESS_REQUEST)) {
            Log.e(TAG, "Error: this activity may be started only with intent "
                  + "ACTION_CONNECTION_ACCESS_REQUEST");
            finish();
            return;
        }

        mDevice = i.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        mReturnPackage = i.getStringExtra(BluetoothDevice.EXTRA_PACKAGE_NAME);
        mReturnClass = i.getStringExtra(BluetoothDevice.EXTRA_CLASS_NAME);
        mRequestType = i.getIntExtra(BluetoothDevice.EXTRA_ACCESS_REQUEST_TYPE,
                                     BluetoothDevice.REQUEST_TYPE_PHONEBOOK_ACCESS);

        if(DEBUG) Log.i(TAG, "onCreate() Request type: " + mRequestType);

        if (mRequestType == BluetoothDevice.REQUEST_TYPE_PROFILE_CONNECTION) {
            showDialog(getString(R.string.bluetooth_connection_permission_request), mRequestType);
        } else if (mRequestType == BluetoothDevice.REQUEST_TYPE_PHONEBOOK_ACCESS) {
            showDialog(getString(R.string.bluetooth_phonebook_request), mRequestType);
        } else if (mRequestType == BluetoothDevice.REQUEST_TYPE_MESSAGE_ACCESS) {
            showDialog(getString(R.string.bluetooth_map_request), mRequestType);
        }
        else {
            Log.e(TAG, "Error: bad request type: " + mRequestType);
            finish();
            return;
        }
        registerReceiver(mReceiver,
                         new IntentFilter(BluetoothDevice.ACTION_CONNECTION_ACCESS_CANCEL));
        mReceiverRegistered = true;
    }


    private void showDialog(String title, int requestType)
    {
        final AlertController.AlertParams p = mAlertParams;
        p.mTitle = title;
        if(DEBUG) Log.i(TAG, "showDialog() Request type: " + mRequestType + " this: " + this);
        switch(requestType)
        {
        case BluetoothDevice.REQUEST_TYPE_PROFILE_CONNECTION:
            p.mView = createConnectionDialogView();
            break;
        case BluetoothDevice.REQUEST_TYPE_PHONEBOOK_ACCESS:
            p.mView = createPhonebookDialogView();
            break;
        case BluetoothDevice.REQUEST_TYPE_MESSAGE_ACCESS:
            p.mView = createMapDialogView();
            break;
        }
        p.mPositiveButtonText = getString(R.string.yes);
        p.mPositiveButtonListener = this;
        p.mNegativeButtonText = getString(R.string.no);
        p.mNegativeButtonListener = this;
        mOkButton = mAlert.getButton(DialogInterface.BUTTON_POSITIVE);
        setupAlert();

    }
    @Override
    public void onBackPressed() {
        /*we need an answer so ignore back button presses during auth */
        if(DEBUG) Log.i(TAG, "Back button pressed! ignoring");
        return;
    }
    private String createRemoteName()
    {
        String mRemoteName = mDevice != null ? mDevice.getAliasName() : null;

        if (mRemoteName == null) mRemoteName = getString(R.string.unknown);
        return mRemoteName;
    }

    // TODO(edjee): createConnectionDialogView, createPhonebookDialogView and createMapDialogView
    // are similar. Refactor them into one method.
    // Also, the string resources bluetooth_remember_choice and bluetooth_pb_remember_choice should
    // be removed.
    private View createConnectionDialogView() {
        String mRemoteName = createRemoteName();
        mView = getLayoutInflater().inflate(R.layout.bluetooth_access, null);
        messageView = (TextView)mView.findViewById(R.id.message);
        messageView.setText(getString(R.string.bluetooth_connection_dialog_text,
                mRemoteName));
        return mView;
    }

    private View createPhonebookDialogView() {
        String mRemoteName = createRemoteName();
        mView = getLayoutInflater().inflate(R.layout.bluetooth_access, null);
        messageView = (TextView)mView.findViewById(R.id.message);
        messageView.setText(getString(R.string.bluetooth_pb_acceptance_dialog_text,
                mRemoteName, mRemoteName));
        return mView;
    }

    private View createMapDialogView() {
        String mRemoteName = createRemoteName();
        mView = getLayoutInflater().inflate(R.layout.bluetooth_access, null);
        messageView = (TextView)mView.findViewById(R.id.message);
        messageView.setText(getString(R.string.bluetooth_map_acceptance_dialog_text,
                mRemoteName, mRemoteName));
        return mView;
    }

    private void onPositive() {
        if (DEBUG) Log.d(TAG, "onPositive");
        sendReplyIntentToReceiver(true, true);
        finish();
    }

    private void onNegative() {
        if (DEBUG) Log.d(TAG, "onNegative");

        boolean always = true;
        if (mRequestType == BluetoothDevice.REQUEST_TYPE_MESSAGE_ACCESS) {
            LocalBluetoothManager bluetoothManager = LocalBluetoothManager.getInstance(this);
            CachedBluetoothDeviceManager cachedDeviceManager =
                    bluetoothManager.getCachedDeviceManager();
            CachedBluetoothDevice cachedDevice = cachedDeviceManager.findDevice(mDevice);
            if (cachedDevice == null) {
                cachedDevice = cachedDeviceManager.addDevice(bluetoothManager.getBluetoothAdapter(),
                                                             bluetoothManager.getProfileManager(),
                                                             mDevice);
            }
            always = cachedDevice.checkAndIncreaseMessageRejectionCount();
        }

        sendReplyIntentToReceiver(false, always);
    }

    private void sendReplyIntentToReceiver(final boolean allowed, final boolean always) {
        Intent intent = new Intent(BluetoothDevice.ACTION_CONNECTION_ACCESS_REPLY);

        if (mReturnPackage != null && mReturnClass != null) {
            intent.setClassName(mReturnPackage, mReturnClass);
        }
        if (DEBUG) Log.i(TAG, "sendReplyIntentToReceiver() Request type: " + mRequestType +
                " mReturnPackage" + mReturnPackage + " mReturnClass" + mReturnClass);

        intent.putExtra(BluetoothDevice.EXTRA_CONNECTION_ACCESS_RESULT,
                        allowed ? BluetoothDevice.CONNECTION_ACCESS_YES
                                : BluetoothDevice.CONNECTION_ACCESS_NO);
        intent.putExtra(BluetoothDevice.EXTRA_ALWAYS_ALLOWED, always);
        intent.putExtra(BluetoothDevice.EXTRA_DEVICE, mDevice);
        intent.putExtra(BluetoothDevice.EXTRA_ACCESS_REQUEST_TYPE, mRequestType);
        sendBroadcast(intent, android.Manifest.permission.BLUETOOTH_ADMIN);
    }

    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                onPositive();
                break;

            case DialogInterface.BUTTON_NEGATIVE:
                onNegative();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiverRegistered) {
            unregisterReceiver(mReceiver);
            mReceiverRegistered = false;
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return true;
    }
}
