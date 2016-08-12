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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.tv.settings.R;

/**
 * Dialog fragment for renaming the local Bluetooth device.
 */
public final class BluetoothNameDialogFragment extends DialogFragment implements TextWatcher {
    private static final int BLUETOOTH_NAME_MAX_LENGTH_BYTES = 248;

    private AlertDialog mAlertDialog;
    private Button mOkButton;

    // accessed from inner class (not private to avoid thunks)
    static final String TAG = "BluetoothNameDialogFragment";
    final LocalBluetoothAdapter mLocalAdapter;
    EditText mDeviceNameView;

    // This flag is set when the name is updated by code, to distinguish from user changes
    private boolean mDeviceNameUpdated;

    // This flag is set when the user edits the name (preserved on rotation)
    private boolean mDeviceNameEdited;

    // Key to save the edited name and edit status for restoring after rotation
    private static final String KEY_NAME = "device_name";
    private static final String KEY_NAME_EDITED = "device_name_edited";

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED)) {
                updateDeviceName();
            } else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED) &&
                    (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR) ==
                            BluetoothAdapter.STATE_ON)) {
                updateDeviceName();
            }
        }
    };

    public BluetoothNameDialogFragment() {
        LocalBluetoothManager localManager = LocalBluetoothManager.getInstance(getActivity());
        mLocalAdapter = localManager.getBluetoothAdapter();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String deviceName = mLocalAdapter.getName();
        if (savedInstanceState != null) {
            deviceName = savedInstanceState.getString(KEY_NAME, deviceName);
            mDeviceNameEdited = savedInstanceState.getBoolean(KEY_NAME_EDITED, false);
        }
        mAlertDialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.bluetooth_rename_device)
                .setView(createDialogView(deviceName))
                .setPositiveButton(R.string.bluetooth_rename_button,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String deviceName = mDeviceNameView.getText().toString();
                                setDeviceName(deviceName);
                            }
                        })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        mAlertDialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        return mAlertDialog;
    }

    private void setDeviceName(String deviceName) {
        Log.d(TAG, "Setting device name to " + deviceName);
        mLocalAdapter.setName(deviceName);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(KEY_NAME, mDeviceNameView.getText().toString());
        outState.putBoolean(KEY_NAME_EDITED, mDeviceNameEdited);
    }

    private View createDialogView(String deviceName) {
        final LayoutInflater layoutInflater = (LayoutInflater)getActivity()
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.dialog_edittext, null);
        mDeviceNameView = (EditText) view.findViewById(R.id.edittext);
        mDeviceNameView.setFilters(new InputFilter[] {
                new Utf8ByteLengthFilter(BLUETOOTH_NAME_MAX_LENGTH_BYTES)
        });
        mDeviceNameView.setText(deviceName);    // set initial value before adding listener
        mDeviceNameView.addTextChangedListener(this);
        mDeviceNameView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    setDeviceName(v.getText().toString());
                    mAlertDialog.dismiss();
                    return true;    // action handled
                } else {
                    return false;   // not handled
                }
            }
        });
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAlertDialog = null;
        mDeviceNameView = null;
        mOkButton = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mOkButton == null) {
            mOkButton = mAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            mOkButton.setEnabled(mDeviceNameEdited);    // Ok button enabled after user edits
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED);
        getActivity().registerReceiver(mReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mReceiver);
    }

    void updateDeviceName() {
        if (mLocalAdapter != null && mLocalAdapter.isEnabled()) {
            mDeviceNameUpdated = true;
            mDeviceNameEdited = false;
            mDeviceNameView.setText(mLocalAdapter.getName());
        }
    }

    public void afterTextChanged(Editable s) {
        if (mDeviceNameUpdated) {
            // Device name changed by code; disable Ok button until edited by user
            mDeviceNameUpdated = false;
            mOkButton.setEnabled(false);
        } else {
            mDeviceNameEdited = true;
            if (mOkButton != null) {
                mOkButton.setEnabled(s.length() != 0);
            }
        }
    }

    /* Not used */
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    /* Not used */
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }
}
