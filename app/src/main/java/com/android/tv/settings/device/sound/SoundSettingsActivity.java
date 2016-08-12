///<MStar Software>
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

package com.android.tv.settings.device.sound;

import static android.provider.Settings.Secure.SCREENSAVER_ENABLED;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;

import com.android.tv.settings.dialog.SettingsLayoutActivity;
import com.android.tv.settings.dialog.Layout;
import com.android.tv.settings.dialog.Layout.Header;
import com.android.tv.settings.dialog.Layout.Action;
import com.android.tv.settings.dialog.Layout.Status;
import com.android.tv.settings.dialog.Layout.Static;
import com.android.tv.settings.dialog.Layout.StringGetter;
import com.android.tv.settings.dialog.Layout.LayoutGetter;
import com.android.tv.settings.util.Tools;
import com.android.tv.settings.R;

import com.android.tv.settings.tvos.util.TvUtil;
import com.android.tv.settings.tvos.AudioSettingsDialog;
import com.android.tv.settings.tvos.AudioSettingsController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
/**
 * Activity allowing the management of sound settings.
 * 
 * 声音设置
 */
public class SoundSettingsActivity extends SettingsLayoutActivity {
    private static final boolean DEBUG = false;
    private static final String TAG = "SoundSettingsActivity";
    private static final int REQUEST_CODE_ADVANCED_OPTIONS = 1;

    private static final int ACTION_VIDEO_HIGH_VOICE_SETTINGS = 1;
    private static final int ACTION_VIDEO_LOW_VOICE_SETTINGS = 2;
    private static final int ACTION_VIDEO_BALANCE_SETTINGS = 3;
    private static final int ACTION_VIDEO_SPDIF_SETTINGS = 4;

    private TvUtil mTvUtil;
    private Resources mRes;
    private ContentResolver mContentResolver;
    private AudioSettingsDialog mAudioSettingsDialog;
    private Handler mHandler = new Handler();
    private SPDIFPickerListener mPickerListener;
    private DialogInterface.OnClickListener mPositiveButtonListener;
    private DialogInterface.OnClickListener mNegativeButtonListener;
    private static int mWhich;
    private static int mTmpWhich;
    private static String[] mSpdif_array;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mRes = getResources();
        mContentResolver = getContentResolver();
        if (Tools.isSN()) {
            mTvUtil = new TvUtil();
            mWhich = mTvUtil.getSPDIF();
            mTmpWhich = 0;
            mSpdif_array = mRes.getStringArray(R.array.spdif);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    LayoutGetter mHighVoiceLayout = new LayoutGetter() {
        @Override
        public Layout get() {
            Layout layout = new Layout();
            layout.add(new Action.Builder(mRes, ACTION_VIDEO_HIGH_VOICE_SETTINGS)
                    .title(R.string.high_voice)
                    .description(String.valueOf(mTvUtil.getHigh())).build());
            return layout;
        }
    };

    LayoutGetter mLowVoiceLayout = new LayoutGetter() {
        @Override
        public Layout get() {
            Layout layout = new Layout();
            layout.add(new Action.Builder(mRes, ACTION_VIDEO_LOW_VOICE_SETTINGS)
                    .title(R.string.low_voice)
                    .description(String.valueOf(mTvUtil.getLow())).build());
            return layout;
        }
    };

    LayoutGetter mBalanceLayout = new LayoutGetter() {
        @Override
        public Layout get() {
            Layout layout = new Layout();
            layout.add(new Action.Builder(mRes, ACTION_VIDEO_BALANCE_SETTINGS)
                    .title(R.string.balance)
                    .description(String.valueOf(mTvUtil.getBalance())).build());
            return layout;
        }
    };

    LayoutGetter mSPDIFLayout = new LayoutGetter() {
        @Override
        public Layout get() {
            Layout layout = new Layout();
            layout.add(new Action.Builder(mRes, ACTION_VIDEO_SPDIF_SETTINGS)
                    .title(R.string.spdif)
                    .description(mSpdif_array[mWhich]).build());
            return layout;
        }
    };

    @Override
    public Layout createLayout() {
        if (Tools.isSN()) {
            return
                new Layout()
                    .breadcrumb(getString(R.string.header_category_device))//设备
                    .add(new Header.Builder(mRes)
                            .icon(R.drawable.ic_settings_sound_on)
                            .title(R.string.device_sound_effects)
                            .build()//系统声音
                        .add(new Action.Builder(mRes,
                                 new Intent(this, SoundActivity.class))
                                .title(R.string.device_sound_effects)
                                .build()
                        )
                        .add(new Header.Builder(mRes)
                                .title(R.string.audio_settings)
                                .build()
                            .add(mHighVoiceLayout)
                            .add(mLowVoiceLayout)
                            .add(mBalanceLayout)
                            .add(mSPDIFLayout)
                        )
                    );
        } else {
            return
                new Layout()
                    .breadcrumb(getString(R.string.header_category_device))
                    .add(new Header.Builder(mRes)
                            .icon(R.drawable.ic_settings_sound_on)
                            .title(R.string.device_sound_effects)
                            .build()
                        .add(new Action.Builder(mRes,
                                 new Intent(this, SoundActivity.class))
                                .title(R.string.device_sound_effects)
                                .build()
                        )
                    );
        }
    }

    @Override
    public void onActionClicked(Action action) {
        switch (action.getId()) {
            case Action.ACTION_INTENT:
                startActivityForResult(action.getIntent(), REQUEST_CODE_ADVANCED_OPTIONS);
                break;
            case ACTION_VIDEO_HIGH_VOICE_SETTINGS:
                mAudioSettingsDialog = new AudioSettingsDialog(this, AudioSettingsController.HIGH);
                mAudioSettingsDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mHighVoiceLayout.refreshView();
                            }
                        });
                        mAudioSettingsDialog = null;
                    }
                });
                mAudioSettingsDialog.show();
                break;
            case ACTION_VIDEO_LOW_VOICE_SETTINGS:
                mAudioSettingsDialog = new AudioSettingsDialog(this, AudioSettingsController.LOW);
                mAudioSettingsDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mLowVoiceLayout.refreshView();
                            }
                        });
                        mAudioSettingsDialog = null;
                    }
                });
                mAudioSettingsDialog.show();
                break;
            case ACTION_VIDEO_BALANCE_SETTINGS:
                mAudioSettingsDialog = new AudioSettingsDialog(this, AudioSettingsController.BALANCE);
                mAudioSettingsDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mBalanceLayout.refreshView();
                            }
                        });
                        mAudioSettingsDialog = null;
                    }
                });
                mAudioSettingsDialog.show();
                break;
            case ACTION_VIDEO_SPDIF_SETTINGS:
                Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.spdif));
                mPickerListener = new SPDIFPickerListener();
                builder.setSingleChoiceItems(R.array.spdif, mWhich, mPickerListener);
                mPositiveButtonListener =
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            try {
                                mWhich = mTmpWhich;
                                mTvUtil.setSPDIF(mWhich);
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mSPDIFLayout.refreshView();
                                    }
                                });
                            } catch (RemoteException e) {
                            }
                        }
                    };
                mNegativeButtonListener =
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                        }
                    };

                builder.setPositiveButton(R.string.title_ok, mPositiveButtonListener);
                builder.setNegativeButton(R.string.title_cancel, mNegativeButtonListener);
                builder.create();
                builder.show();
                break;
        }
    }

    private class SPDIFPickerListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialogInterface, int which) {
            mTmpWhich = which;
        }
    }
}