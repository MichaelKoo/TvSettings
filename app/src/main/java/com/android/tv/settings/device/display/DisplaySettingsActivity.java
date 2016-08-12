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

package com.android.tv.settings.device.display;

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
import android.provider.Settings;
import android.util.Log;

import com.android.tv.settings.device.display.daydream.DaydreamActivity;
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

import com.android.tv.settings.tvos.display.util.TvDispAreaUtil;
import com.android.tv.settings.tvos.display.util.TvDispAreaUtil.Orientation;
import com.android.tv.settings.tvos.display.util.TvResolutionUtil;
import com.android.tv.settings.tvos.display.DispAreaSettingsDialog;
import com.android.tv.settings.tvos.util.TvUtil;
import com.android.tv.settings.tvos.VideoSettingsDialog;
import com.android.tv.settings.tvos.VideoSettingsController;
import com.android.tv.settings.tvos.display.vo.ReproduceRate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
/**
 * Activity allowing the management of display settings.
 */
public class DisplaySettingsActivity extends SettingsLayoutActivity {
    private static final boolean DEBUG = false;
    private static final String TAG = "DisplaySettingsActivity";
    private static final int REQUEST_CODE_ADVANCED_OPTIONS = 1;

    private static final int ACTION_VIDEO_BRIGHTNESS_SETTINGS = 1;
    private static final int ACTION_VIDEO_CONTRAST_SETTINGS = 2;
    private static final int ACTION_VIDEO_COLOR_SETTINGS = 3;
    private static final int ACTION_VIDEO_SHARPNESS_SETTINGS = 4;

    private static final int ACTION_DISPLAY_TOP_SETTINGS = 5;
    private static final int ACTION_DISPLAY_BOTTOM_SETTINGS = 6;
    private static final int ACTION_DISPLAY_LEFT_SETTINGS = 7;
    private static final int ACTION_DISPLAY_RIGHT_SETTINGS = 8;
    private static final int ACTION_DISPLAY_DEFAULT_SETTINGS = 9;
    private static final int ACTION_DISPLAY_RESOLUTION_SETTINGS = 10;

    private TvUtil mTvUtil;
    private TvDispAreaUtil mTvDispAreaUtil;
    private TvResolutionUtil mTvResolutionUtil;
    private ReproduceRate mReproducceRate;
    private Resources mRes;
    private ContentResolver mContentResolver;
    private VideoSettingsDialog mVideoSettingsDialog;
    private DispAreaSettingsDialog mDispAreaSettingsDialog;
    private static String[] mResolutionArray;
    private static int mTempResolutionIndex, mResolutionIndex;
    private ResolutionPickerListener mPickerListener;
    private DialogInterface.OnClickListener mPositiveButtonListener;
    private DialogInterface.OnClickListener mNegativeButtonListener;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mRes = getResources();
        mContentResolver = getContentResolver();
        if (Tools.isSN()) {
            mTvUtil = new TvUtil();
            mTvDispAreaUtil = new TvDispAreaUtil();
            mTvResolutionUtil = TvResolutionUtil.getInstance();
            mReproducceRate = mTvDispAreaUtil.getReproduceRate();
            mResolutionIndex = mTvResolutionUtil.getCurrentResolution();
            mResolutionArray = mTvResolutionUtil.getResolutionArray();
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

    LayoutGetter mBrightnessLayout = new LayoutGetter() {
        @Override
        public Layout get() {
            Layout layout = new Layout();
            layout.add(new Action.Builder(mRes, ACTION_VIDEO_BRIGHTNESS_SETTINGS)
                    .title(R.string.brightness)
                    .description(String.valueOf(mTvUtil.getBrightness())).build());
            return layout;
        }
    };

    LayoutGetter mContrastLayout = new LayoutGetter() {
        @Override
        public Layout get() {
            Layout layout = new Layout();
            layout.add(new Action.Builder(mRes, ACTION_VIDEO_CONTRAST_SETTINGS)
                    .title(R.string.contrast)
                    .description(String.valueOf(mTvUtil.getContrast())).build());
            return layout;
        }
    };

    LayoutGetter mColorLayout = new LayoutGetter() {
        @Override
        public Layout get() {
            Layout layout = new Layout();
            layout.add(new Action.Builder(mRes, ACTION_VIDEO_COLOR_SETTINGS)
                    .title(R.string.color)
                    .description(String.valueOf(mTvUtil.getColor())).build());
            return layout;
        }
    };

    LayoutGetter mSharpnessLayout = new LayoutGetter() {
        @Override
        public Layout get() {
            Layout layout = new Layout();
            layout.add(new Action.Builder(mRes, ACTION_VIDEO_SHARPNESS_SETTINGS)
                    .title(R.string.sharpness)
                    .description(String.valueOf(mTvUtil.getSharpness())).build());
            return layout;
        }
    };

    LayoutGetter mDisplayTopLayout = new LayoutGetter() {
        @Override
        public Layout get() {
            Layout layout = new Layout();
            layout.add(new Action.Builder(mRes, ACTION_DISPLAY_TOP_SETTINGS)
                    .title(R.string.display_position_top)
                    .description(String.valueOf(mReproducceRate._reduceRate.topRate)).build());
            return layout;
        }
    };

    LayoutGetter mDisplayBottomLayout = new LayoutGetter() {
        @Override
        public Layout get() {
            Layout layout = new Layout();
            layout.add(new Action.Builder(mRes, ACTION_DISPLAY_BOTTOM_SETTINGS)
                    .title(R.string.display_position_bottom)
                    .description(String.valueOf(mReproducceRate._reduceRate.bottomRate)).build());
            return layout;
        }
    };

    LayoutGetter mDisplayLeftLayout = new LayoutGetter() {
        @Override
        public Layout get() {
            Layout layout = new Layout();
            layout.add(new Action.Builder(mRes, ACTION_DISPLAY_LEFT_SETTINGS)
                    .title(R.string.display_position_left)
                    .description(String.valueOf(mReproducceRate._reduceRate.leftRate)).build());
            return layout;
        }
    };

    LayoutGetter mDisplayRightLayout = new LayoutGetter() {
        @Override
        public Layout get() {
            Layout layout = new Layout();
            layout.add(new Action.Builder(mRes, ACTION_DISPLAY_RIGHT_SETTINGS)
                    .title(R.string.display_position_right)
                    .description(String.valueOf(mReproducceRate._reduceRate.rightRate)).build());
            return layout;
        }
    };

    LayoutGetter mDisplayDefaultLayout = new LayoutGetter() {
        @Override
        public Layout get() {
            Layout layout = new Layout();
            layout.add(new Action.Builder(mRes, ACTION_DISPLAY_DEFAULT_SETTINGS)
                    .title(R.string.display_position_default)
                    .build());
            return layout;
        }
    };

    LayoutGetter mDisplayResolutionLayout = new LayoutGetter() {
        @Override
        public Layout get() {
            Layout layout = new Layout();
            layout.add(new Action.Builder(mRes, ACTION_DISPLAY_RESOLUTION_SETTINGS)
                    .title(R.string.resolution)
                    .description(mResolutionArray[mResolutionIndex]).build());
            return layout;
        }
    };

    @Override
    public Layout createLayout() {
        if (Tools.isBox()) {
            return new Layout()
                .breadcrumb(getString(R.string.header_category_device))
                .add(new Header.Builder(mRes)
                        .icon(R.drawable.ic_settings_display)
                        .title(R.string.device_display)
                        .build()//显示
                    .add(new Action.Builder(mRes,
                             new Intent(this, DaydreamActivity.class))
                            .title(R.string.device_daydream)
                            .build()//互动屏保
                    )
                    .add(new Header.Builder(mRes)
                            .title(R.string.video_settings)
                            .build()//视频设置
                        .add(mBrightnessLayout)
                        .add(mContrastLayout)
                        .add(mColorLayout)
                        .add(mSharpnessLayout)
                    )
                    .add(new Header.Builder(mRes)
                            .title(R.string.display_setting)
                            .build()
                        .add(mDisplayTopLayout)
                        .add(mDisplayBottomLayout)
                        .add(mDisplayLeftLayout)
                        .add(mDisplayRightLayout)
                        .add(mDisplayDefaultLayout)
                    )
                    .add(mDisplayResolutionLayout)
                );
        } else if (Tools.isSN()) {
            return new Layout()
                .breadcrumb(getString(R.string.header_category_device))
                .add(new Header.Builder(mRes)
                        .icon(R.drawable.ic_settings_display)
                        .title(R.string.device_display)
                        .build()
                    .add(new Action.Builder(mRes,
                             new Intent(this, DaydreamActivity.class))
                            .title(R.string.device_daydream)
                            .build()
                    )
                    .add(new Header.Builder(mRes)
                            .title(R.string.video_settings)
                            .build()
                        .add(mBrightnessLayout)
                        .add(mContrastLayout)
                        .add(mColorLayout)
                        .add(mSharpnessLayout)
                    )
                );

        } else {
            return new Layout()
                .breadcrumb(getString(R.string.header_category_device))
                .add(new Header.Builder(mRes)
                        .icon(R.drawable.ic_settings_display)
                        .title(R.string.device_display)
                        .build()
                    .add(new Action.Builder(mRes,
                             new Intent(this, DaydreamActivity.class))
                            .title(R.string.device_daydream)
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
            case ACTION_VIDEO_BRIGHTNESS_SETTINGS:
                mVideoSettingsDialog = new VideoSettingsDialog(this, VideoSettingsController.BRIGHTNESS);
                mVideoSettingsDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mBrightnessLayout.refreshView();
                            }
                        });
                        mVideoSettingsDialog = null;
                    }
                });
                mVideoSettingsDialog.show();
                break;
            case ACTION_VIDEO_CONTRAST_SETTINGS:
                mVideoSettingsDialog = new VideoSettingsDialog(this, VideoSettingsController.CONTRAST);
                mVideoSettingsDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mContrastLayout.refreshView();
                            }
                        });
                        mVideoSettingsDialog = null;
                    }
                });
                mVideoSettingsDialog.show();
                break;
            case ACTION_VIDEO_COLOR_SETTINGS:
                mVideoSettingsDialog = new VideoSettingsDialog(this, VideoSettingsController.COLOR);
                mVideoSettingsDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mColorLayout.refreshView();
                            }
                        });
                        mVideoSettingsDialog = null;
                    }
                });
                mVideoSettingsDialog.show();
                break;
            case ACTION_VIDEO_SHARPNESS_SETTINGS:
                mVideoSettingsDialog = new VideoSettingsDialog(this, VideoSettingsController.SHARPNESS);
                mVideoSettingsDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mSharpnessLayout.refreshView();
                            }
                        });
                        mVideoSettingsDialog = null;
                    }
                });
                mVideoSettingsDialog.show();
                break;
            case ACTION_DISPLAY_TOP_SETTINGS:
                mDispAreaSettingsDialog = new DispAreaSettingsDialog(this, Orientation.REPRODUCE_ADJUST_TOP);
                mDispAreaSettingsDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mReproducceRate = mTvDispAreaUtil.getReproduceRate();
                                mDisplayTopLayout.refreshView();
                            }
                        });
                        mDispAreaSettingsDialog = null;
                    }
                });
                mDispAreaSettingsDialog.show();
                break;
            case ACTION_DISPLAY_BOTTOM_SETTINGS:
                mDispAreaSettingsDialog = new DispAreaSettingsDialog(this, Orientation.REPRODUCE_ADJUST_BOTTOM);
                mDispAreaSettingsDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mReproducceRate = mTvDispAreaUtil.getReproduceRate();
                                mDisplayBottomLayout.refreshView();
                            }
                        });
                        mDispAreaSettingsDialog = null;
                    }
                });
                mDispAreaSettingsDialog.show();
                break;
            case ACTION_DISPLAY_LEFT_SETTINGS:
                mDispAreaSettingsDialog = new DispAreaSettingsDialog(this, Orientation.REPRODUCE_ADJUST_LEFT);
                mDispAreaSettingsDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mReproducceRate = mTvDispAreaUtil.getReproduceRate();
                                mDisplayLeftLayout.refreshView();
                            }
                        });
                        mDispAreaSettingsDialog = null;
                    }
                });
                mDispAreaSettingsDialog.show();
                break;
            case ACTION_DISPLAY_RIGHT_SETTINGS:
                mDispAreaSettingsDialog = new DispAreaSettingsDialog(this, Orientation.REPRODUCE_ADJUST_RIGHT);
                mDispAreaSettingsDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mReproducceRate = mTvDispAreaUtil.getReproduceRate();
                                mDisplayRightLayout.refreshView();
                            }
                        });
                        mDispAreaSettingsDialog = null;
                    }
                });
                mDispAreaSettingsDialog.show();
                break;
            case ACTION_DISPLAY_DEFAULT_SETTINGS:
                mTvDispAreaUtil.setReproduceRateDefault();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mReproducceRate = mTvDispAreaUtil.getReproduceRate();
                        mDisplayTopLayout.refreshView();
                        mDisplayBottomLayout.refreshView();
                        mDisplayLeftLayout.refreshView();
                        mDisplayRightLayout.refreshView();
                    }
                });
                break;
            case ACTION_DISPLAY_RESOLUTION_SETTINGS:
                Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.resolution));
                mPickerListener = new ResolutionPickerListener();
                mResolutionIndex = mTvResolutionUtil.getCurrentResolution();
                builder.setSingleChoiceItems(mResolutionArray, mResolutionIndex, mPickerListener);
                mPositiveButtonListener =
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            mResolutionIndex = mTempResolutionIndex;
                            mTvResolutionUtil.setCurrentResolution(mResolutionIndex, getApplicationContext());
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mDisplayResolutionLayout.refreshView();
                                }
                            });
                        }
                    };

                mNegativeButtonListener =
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                        }
                    };

                builder.setPositiveButton(R.string.okay, mPositiveButtonListener);
                builder.setNegativeButton(R.string.cancel, mNegativeButtonListener);
                builder.create();
                builder.show();
                break;
        }
    }

    private class ResolutionPickerListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialogInterface, int which) {
            mTempResolutionIndex = which;
        }
    }
}