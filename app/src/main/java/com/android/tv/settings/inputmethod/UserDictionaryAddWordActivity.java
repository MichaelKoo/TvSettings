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

package com.android.tv.settings.inputmethod;

import com.android.tv.settings.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.View;

public class UserDictionaryAddWordActivity extends Activity {

    public static final String MODE_EDIT_ACTION = "com.android.tv.settings.USER_DICTIONARY_EDIT";
    public static final String MODE_INSERT_ACTION = "com.android.tv.settings.USER_DICTIONARY_INSERT";

    /* package */ static final int CODE_WORD_ADDED = 0;
    /* package */ static final int CODE_CANCEL = 1;
    /* package */ static final int CODE_ALREADY_PRESENT = 2;

    private UserDictionaryAddWordContents mContents;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_dictionary_add_word);
        final Intent intent = getIntent();
        final String action = intent.getAction();
        final int mode;
        if (MODE_EDIT_ACTION.equals(action)) {
            mode = UserDictionaryAddWordContents.MODE_EDIT;
        } else if (MODE_INSERT_ACTION.equals(action)) {
            mode = UserDictionaryAddWordContents.MODE_INSERT;
        } else {
            // Can never come here because we only support these two actions in the manifest
            throw new RuntimeException("Unsupported action: " + action);
        }

        // The following will get the EXTRA_WORD and EXTRA_LOCALE fields that are in the intent.
        // We do need to add the action by hand, because UserDictionaryAddWordContents expects
        // it to be in the bundle, in the EXTRA_MODE key.
        final Bundle args = intent.getExtras();
        args.putInt(UserDictionaryAddWordContents.EXTRA_MODE, mode);

        if (null != savedInstanceState) {
            // Override options if we have a saved state.
            args.putAll(savedInstanceState);
        }

        mContents = new UserDictionaryAddWordContents(getWindow().getDecorView(), args);
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        mContents.saveStateIntoBundle(outState);
    }

    private void reportBackToCaller(final int resultCode, final Bundle result) {
        final Intent senderIntent = getIntent();
        final Object listener = senderIntent.getExtras().get("listener");
        if (!(listener instanceof Messenger)) return; // This will work if listener is null too.
        final Messenger messenger = (Messenger)listener;

        final Message m = Message.obtain();
        m.obj = result;
        m.what = resultCode;
        try {
            messenger.send(m);
        } catch (RemoteException e) {
            // Couldn't report back, but there is nothing we can do to fix it
        }
    }

    public void onClickCancel(final View v) {
        reportBackToCaller(CODE_CANCEL, null);
        finish();
    }

    public void onClickConfirm(final View v) {
        final Bundle parameters = new Bundle();
        final int resultCode = mContents.apply(this, parameters);
        reportBackToCaller(resultCode, parameters);
        finish();
    }
}
