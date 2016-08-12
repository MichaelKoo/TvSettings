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
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.InputFilter.LengthFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;
import com.android.tv.settings.R;
import android.view.KeyEvent;

import java.util.Locale;

/**
 * BluetoothPairingDialog asks the user to enter a PIN / Passkey / simple confirmation
 * for pairing with a remote Bluetooth device. It is an activity that appears as a dialog.
 */
public final class BluetoothPairingDialog extends AlertActivity implements
        CompoundButton.OnCheckedChangeListener, DialogInterface.OnClickListener, TextWatcher {
    private static final String TAG = "BluetoothPairingDialog";

    private static final int BLUETOOTH_PIN_MAX_LENGTH = 16;
    private static final int BLUETOOTH_PASSKEY_MAX_LENGTH = 6;

    private LocalBluetoothManager mBluetoothManager;
    private CachedBluetoothDeviceManager mCachedDeviceManager;
    private BluetoothDevice mDevice;
    private int mType;
    private String mPairingKey;
    private EditText mPairingView;
    private Button mOkButton;

    /**
     * Dismiss the dialog if the bond state changes to bonded or none,
     * or if pairing was canceled for {@link #mDevice}.
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE,
                                                   BluetoothDevice.ERROR);
                if (bondState == BluetoothDevice.BOND_BONDED ||
                        bondState == BluetoothDevice.BOND_NONE) {
                    dismiss();
                }
            } else if (BluetoothDevice.ACTION_PAIRING_CANCEL.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device == null || device.equals(mDevice)) {
                    dismiss();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (!intent.getAction().equals(BluetoothDevice.ACTION_PAIRING_REQUEST))
        {
            Log.e(TAG, "Error: this activity may be started only with intent " +
                  BluetoothDevice.ACTION_PAIRING_REQUEST);
            finish();
            return;
        }

        mBluetoothManager = LocalBluetoothManager.getInstance(this);
        if (mBluetoothManager == null) {
            Log.e(TAG, "Error: BluetoothAdapter not supported by system");
            finish();
            return;
        }
        mCachedDeviceManager = mBluetoothManager.getCachedDeviceManager();

        mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        mType = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT, BluetoothDevice.ERROR);

        switch (mType) {
            case BluetoothDevice.PAIRING_VARIANT_PIN:
            case BluetoothDevice.PAIRING_VARIANT_PASSKEY:
                createUserEntryDialog();
                break;

            case BluetoothDevice.PAIRING_VARIANT_PASSKEY_CONFIRMATION:
                int passkey =
                    intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_KEY, BluetoothDevice.ERROR);
                if (passkey == BluetoothDevice.ERROR) {
                    Log.e(TAG, "Invalid Confirmation Passkey received, not showing any dialog");
                    return;
                }
                mPairingKey = String.format(Locale.US, "%06d", passkey);
                createConfirmationDialog();
                break;

            case BluetoothDevice.PAIRING_VARIANT_CONSENT:
            case BluetoothDevice.PAIRING_VARIANT_OOB_CONSENT:
                createConsentDialog();
                break;

            case BluetoothDevice.PAIRING_VARIANT_DISPLAY_PASSKEY:
            case BluetoothDevice.PAIRING_VARIANT_DISPLAY_PIN:
                int pairingKey =
                    intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_KEY, BluetoothDevice.ERROR);
                if (pairingKey == BluetoothDevice.ERROR) {
                    Log.e(TAG, "Invalid Confirmation Passkey or PIN received, not showing any dialog");
                    return;
                }
                if (mType == BluetoothDevice.PAIRING_VARIANT_DISPLAY_PASSKEY) {
                    mPairingKey = String.format("%06d", pairingKey);
                } else {
                    mPairingKey = String.format("%04d", pairingKey);
                }
                createDisplayPasskeyOrPinDialog();
                break;

            default:
                Log.e(TAG, "Incorrect pairing type received, not showing any dialog");
        }

        /*
         * Leave this registered through pause/resume since we still want to
         * finish the activity in the background if pairing is canceled.
         */
        registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_PAIRING_CANCEL));
        registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
    }

    private void createUserEntryDialog() {
        final AlertController.AlertParams p = mAlertParams;
        p.mTitle = getString(R.string.bluetooth_pairing_request);
        p.mView = createPinEntryView();
        p.mPositiveButtonText = getString(android.R.string.ok);
        p.mPositiveButtonListener = this;
        p.mNegativeButtonText = getString(android.R.string.cancel);
        p.mNegativeButtonListener = this;
        setupAlert();

        mOkButton = mAlert.getButton(BUTTON_POSITIVE);
        mOkButton.setEnabled(false);
    }

    private View createPinEntryView() {
        View view = getLayoutInflater().inflate(R.layout.bluetooth_pin_entry, null);
        TextView messageViewCaption = (TextView) view.findViewById(R.id.message_caption);
        TextView messageViewContent = (TextView) view.findViewById(R.id.message_subhead);
        TextView messageView2 = (TextView) view.findViewById(R.id.message_below_pin);
        CheckBox alphanumericPin = (CheckBox) view.findViewById(R.id.alphanumeric_pin);
        mPairingView = (EditText) view.findViewById(R.id.text);
        mPairingView.addTextChangedListener(this);
        alphanumericPin.setOnCheckedChangeListener(this);

        int messageId1;
        int messageId2;
        int maxLength;
        switch (mType) {
            case BluetoothDevice.PAIRING_VARIANT_PIN:
                messageId1 = R.string.bluetooth_enter_pin_msg;
                messageId2 = R.string.bluetooth_enter_pin_other_device;
                // Maximum of 16 characters in a PIN
                maxLength = BLUETOOTH_PIN_MAX_LENGTH;
                break;

            case BluetoothDevice.PAIRING_VARIANT_PASSKEY:
                messageId1 = R.string.bluetooth_enter_pin_msg;
                messageId2 = R.string.bluetooth_enter_passkey_other_device;
                // Maximum of 6 digits for passkey
                maxLength = BLUETOOTH_PASSKEY_MAX_LENGTH;
                alphanumericPin.setVisibility(View.GONE);
                break;

            default:
                Log.e(TAG, "Incorrect pairing type for createPinEntryView: " + mType);
                return null;
        }

        messageViewCaption.setText(messageId1);
        messageViewContent.setText(mCachedDeviceManager.getName(mDevice));
        messageView2.setText(messageId2);
        mPairingView.setInputType(InputType.TYPE_CLASS_NUMBER);
        mPairingView.setFilters(new InputFilter[] {
                new LengthFilter(maxLength) });

        return view;
    }

    private View createView() {
        View view = getLayoutInflater().inflate(R.layout.bluetooth_pin_confirm, null);
        // Escape device name to avoid HTML injection.
        String name = Html.escapeHtml(mCachedDeviceManager.getName(mDevice));
        TextView messageViewCaption = (TextView) view.findViewById(R.id.message_caption);
        TextView messageViewContent = (TextView) view.findViewById(R.id.message_subhead);
        TextView pairingViewCaption = (TextView) view.findViewById(R.id.pairing_caption);
        TextView pairingViewContent = (TextView) view.findViewById(R.id.pairing_subhead);
        TextView messagePairing = (TextView) view.findViewById(R.id.pairing_code_message);

        String messageCaption = null;
        String pairingContent = null;
        switch (mType) {
            case BluetoothDevice.PAIRING_VARIANT_DISPLAY_PASSKEY:
            case BluetoothDevice.PAIRING_VARIANT_DISPLAY_PIN:
                messagePairing.setVisibility(View.VISIBLE);
            case BluetoothDevice.PAIRING_VARIANT_PASSKEY_CONFIRMATION:
                messageCaption = getString(R.string.bluetooth_enter_pin_msg);
                pairingContent = mPairingKey;
                break;

            case BluetoothDevice.PAIRING_VARIANT_CONSENT:
            case BluetoothDevice.PAIRING_VARIANT_OOB_CONSENT:
                messagePairing.setVisibility(View.VISIBLE);
                messageCaption = getString(R.string.bluetooth_enter_pin_msg);
                break;

            default:
                Log.e(TAG, "Incorrect pairing type received, not creating view");
                return null;
        }

        if (messageViewCaption != null) {
            messageViewCaption.setText(messageCaption);
            messageViewContent.setText(name);
        }

        if (pairingContent != null) {
            pairingViewCaption.setVisibility(View.VISIBLE);
            pairingViewContent.setVisibility(View.VISIBLE);
            pairingViewContent.setText(pairingContent);
        }

        return view;
    }

    private void createConfirmationDialog() {
        final AlertController.AlertParams p = mAlertParams;
        p.mTitle = getString(R.string.bluetooth_pairing_request);
        p.mView = createView();
        p.mPositiveButtonText = getString(R.string.bluetooth_pairing_accept);
        p.mPositiveButtonListener = this;
        p.mNegativeButtonText = getString(R.string.bluetooth_pairing_decline);
        p.mNegativeButtonListener = this;
        setupAlert();
    }

    private void createConsentDialog() {
        final AlertController.AlertParams p = mAlertParams;
        p.mTitle = getString(R.string.bluetooth_pairing_request);
        p.mView = createView();
        p.mPositiveButtonText = getString(R.string.bluetooth_pairing_accept);
        p.mPositiveButtonListener = this;
        p.mNegativeButtonText = getString(R.string.bluetooth_pairing_decline);
        p.mNegativeButtonListener = this;
        setupAlert();
    }

    private void createDisplayPasskeyOrPinDialog() {
        final AlertController.AlertParams p = mAlertParams;
        p.mTitle = getString(R.string.bluetooth_pairing_request);
        p.mView = createView();
        p.mNegativeButtonText = getString(android.R.string.cancel);
        p.mNegativeButtonListener = this;
        setupAlert();

        // Since its only a notification, send an OK to the framework,
        // indicating that the dialog has been displayed.
        if (mType == BluetoothDevice.PAIRING_VARIANT_DISPLAY_PASSKEY) {
            mDevice.setPairingConfirmation(true);
        } else if (mType == BluetoothDevice.PAIRING_VARIANT_DISPLAY_PIN) {
            byte[] pinBytes = BluetoothDevice.convertPinToBytes(mPairingKey);
            mDevice.setPin(pinBytes);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    public void afterTextChanged(Editable s) {
        if (mOkButton != null) {
            mOkButton.setEnabled(s.length() > 0);
        }
    }

    private void allowPhonebookAccess() {
        CachedBluetoothDevice cachedDevice = mCachedDeviceManager.findDevice(mDevice);
        if (cachedDevice == null) {
            cachedDevice = mCachedDeviceManager.addDevice(
                    mBluetoothManager.getBluetoothAdapter(),
                    mBluetoothManager.getProfileManager(),
                    mDevice);
        }
        cachedDevice.setPhonebookPermissionChoice(CachedBluetoothDevice.ACCESS_ALLOWED);
    }

    private void onPair(String value) {
        allowPhonebookAccess();

        switch (mType) {
            case BluetoothDevice.PAIRING_VARIANT_PIN:
                byte[] pinBytes = BluetoothDevice.convertPinToBytes(value);
                if (pinBytes == null) {
                    return;
                }
                mDevice.setPin(pinBytes);
                break;

            case BluetoothDevice.PAIRING_VARIANT_PASSKEY:
                int passkey = Integer.parseInt(value);
                mDevice.setPasskey(passkey);
                break;

            case BluetoothDevice.PAIRING_VARIANT_PASSKEY_CONFIRMATION:
            case BluetoothDevice.PAIRING_VARIANT_CONSENT:
                mDevice.setPairingConfirmation(true);
                break;

            case BluetoothDevice.PAIRING_VARIANT_DISPLAY_PASSKEY:
            case BluetoothDevice.PAIRING_VARIANT_DISPLAY_PIN:
                // Do nothing.
                break;

            case BluetoothDevice.PAIRING_VARIANT_OOB_CONSENT:
                mDevice.setRemoteOutOfBandData();
                break;

            default:
                Log.e(TAG, "Incorrect pairing type received");
        }
    }

    private void onCancel() {
        mDevice.cancelPairingUserInput();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onCancel();
        }
        return super.onKeyDown(keyCode,event);
    }

    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case BUTTON_POSITIVE:
                if (mPairingView != null) {
                    onPair(mPairingView.getText().toString());
                } else {
                    onPair(null);
                }
                break;

            case BUTTON_NEGATIVE:
            default:
                onCancel();
                break;
        }
    }

    /* Not used */
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    /* Not used */
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // change input type for soft keyboard to numeric or alphanumeric
        if (isChecked) {
            mPairingView.setInputType(InputType.TYPE_CLASS_TEXT);
        } else {
            mPairingView.setInputType(InputType.TYPE_CLASS_NUMBER);
        }
    }
}
