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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.mstar.android.pppoe.PPPOE_STA;
import com.mstar.android.pppoe.PppoeManager;

import com.android.tv.settings.R;

public class PPPoEDialog extends AlertDialog implements View.OnClickListener,
        TextWatcher, AdapterView.OnItemSelectedListener {

    private static final String PPPOE_SHOW_PASSWORD = "pppoe_show_password";

    private static final String PPPOE_SAVE_ACCOUNT_AND_PASSWORD = "pppoe_save_account_and_password";

    private static final int CHECKED = 1;

    private final DialogInterface.OnClickListener mListener;

    private View mView;
    private TextView mSsid;
    private EditText mPassword;
    private EditText mUsername;
    private CheckBox mShowPassword;
    private CheckBox mSaveAccountPasswd;
    private PPPoEDialer mPPPoEDialer;
    private Context mContext;

    public PPPoEDialog(Context context, PPPoEDialer pppoedialer) {
        super(context);

        mContext = context;
        mPPPoEDialer = pppoedialer;
        mListener = new DialogInterface.OnClickListener () {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    if (mPPPoEDialer.isConnected()) {
                        mPPPoEDialer.hangup();
                    } else {
                        String user = mUsername.getText().toString().trim();
                        String passwd = mPassword.getText().toString().trim();
                        mPPPoEDialer.dial(user, passwd);
                    }
                }
            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        mView = getLayoutInflater().inflate(R.layout.pppoe_dialog, null);

        setView(mView);
        setInverseBackgroundForced(true);

        mView.findViewById(R.id.type).setVisibility(View.VISIBLE);
        mUsername = (EditText) mView.findViewById(R.id.username);
        mPassword = (EditText) mView.findViewById(R.id.password);
        mSaveAccountPasswd = (CheckBox) mView.findViewById(R.id.save_account_passwd);
        mShowPassword = (CheckBox) mView.findViewById(R.id.show_password);

        if(mPPPoEDialer.isConnected()) {
            setButton(DialogInterface.BUTTON_POSITIVE, mContext.getString(R.string.pppoe_disconnect), mListener);
        } else {
            setButton(DialogInterface.BUTTON_POSITIVE, mContext.getString(R.string.pppoe_connect), mListener);
        }
        setButton(DialogInterface.BUTTON_NEGATIVE, mContext.getString(R.string.pppoe_cancel), mListener);

        if (mPPPoEDialer.isConnected()) {
            mUsername.setText(mPPPoEDialer.getUser());
            mPassword.setText(mPPPoEDialer.getPasswd());
            mUsername.setEnabled(false);
            mPassword.setEnabled(false);
        } else {
            if (Settings.Global.getInt(mContext.getContentResolver(), PPPOE_SAVE_ACCOUNT_AND_PASSWORD, 0) == CHECKED) {
                mUsername.setText(mPPPoEDialer.getUser());
                mPassword.setText(mPPPoEDialer.getPasswd());
            }
            mUsername.addTextChangedListener(this);
            mPassword.addTextChangedListener(this);
        }

        mSaveAccountPasswd.setOnClickListener(this);
        mShowPassword.setOnClickListener(this);

        super.onCreate(savedInstanceState);

        showSecurityFields();
        validate();

        if (Settings.Global.getInt(mContext.getContentResolver(), PPPOE_SAVE_ACCOUNT_AND_PASSWORD, 0) == CHECKED) {
            mSaveAccountPasswd.setChecked(true);
        }

        if (Settings.Global.getInt(mContext.getContentResolver(), PPPOE_SHOW_PASSWORD, 0) == CHECKED) {
            mShowPassword.setChecked(true);
            mPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        }
    }

    private void validate() {
        if (mPassword.length() > 0 && mUsername.length() > 0) {
            getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
        } else {
            getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
        }
    }

    public void onClick(View view) {
        if (view.getId() == R.id.show_password) {
            mPassword.setInputType(
                    InputType.TYPE_CLASS_TEXT | (((CheckBox) view).isChecked() ?
                    InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                    InputType.TYPE_TEXT_VARIATION_PASSWORD));
            Settings.Global.putInt(mContext.getContentResolver(), PPPOE_SHOW_PASSWORD, (((CheckBox) view).isChecked() ? 1 : 0));
        } else {
            Settings.Global.putInt(mContext.getContentResolver(), PPPOE_SAVE_ACCOUNT_AND_PASSWORD, (((CheckBox) view).isChecked() ? 1 : 0));
            if (((CheckBox) view).isChecked()) {
                mPPPoEDialer.setUser(mUsername.getText().toString().trim());
                mPPPoEDialer.setPasswd(mPassword.getText().toString().trim());
            }
        }
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void afterTextChanged(Editable editable) {
        validate();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        showSecurityFields();
        validate();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    private void showSecurityFields() {
        mView.findViewById(R.id.fields).setVisibility(View.VISIBLE);
    }
}
