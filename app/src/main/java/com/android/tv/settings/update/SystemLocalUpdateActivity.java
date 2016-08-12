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

package com.android.tv.settings.update;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RecoverySystem;
import android.os.RecoverySystem.ProgressListener;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.tv.settings.R;
import com.android.tv.settings.util.Tools;
import com.mstar.android.storage.MStorageManager;

public class SystemLocalUpdateActivity extends Activity {

    private final static String TAG = "SystemLocalUpdateActivity";

    private static final int CHECK_STORAGE = 0;

    private static final int CHECK_NEW_VERSION = 1;

    private static final int CHECK_UPDATE_ERROR = 2;

    private static final int UPDATE_SUCCESS = 3;

    private static final int UPDATE_PROGRESS = 4;

    protected static final int MSG_SELECT_UPDATE = 5;

    private static final int CHECK_STORAGE_COUNT = 10;

    private VersionInformation mVersionInfo;

    private Button mUpdateButton;

    private TextView mUpdateInfoText;

    private String mUpdateInfo;

    private int mRetryCount;

    private boolean mHasNewVerison = false;

    private boolean mIsUpdating = false;

    protected File mUpdateFile;

    private ProgressBar mProgressBar;

    private TextView mCurrentProgressText;

    private LinearLayout mLinearLayout;

    private int mCurrentProgress;

    private SelectUpdateDialog mSelectUpdateDialog;

    private StorageManager mStorageManager;

    private StorageVolume[] mVolumes;

    protected List<Map<String, String>> mMountedVolumes = new ArrayList<Map<String, String>>();

    protected List<File> mScanFiles = new ArrayList<File>();

    protected boolean mSelectedFlag = false;

    protected Handler myHandler = new Handler() {

        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case CHECK_STORAGE:
                    checkStorage();
                    break;
                case CHECK_NEW_VERSION:
                    mUpdateInfo += getString(R.string.check_new_version) + "\n";
                    mUpdateInfoText.setText(mUpdateInfo);
                    scanUpdateFile();
                    break;
                case CHECK_UPDATE_ERROR:
                    mUpdateInfo += getString(R.string.check_failure) + "\n";
                    mUpdateButton.setEnabled(true);
                    mUpdateButton.setText(getString(R.string.exit));
                    mUpdateInfoText.setText(mUpdateInfo);
                    break;
                case UPDATE_SUCCESS:
                    mUpdateInfo += getString(R.string.updated);
                    mUpdateInfoText.setText(mUpdateInfo);
                    break;
                case UPDATE_PROGRESS:
                    mCurrentProgressText.setText(mCurrentProgress + "%");
                    break;
                case MSG_SELECT_UPDATE:
                    mLinearLayout.setVisibility(View.VISIBLE);
                    mIsUpdating = true;
                    mUpdateButton.setEnabled(false);
                    mUpdateInfo += getString(R.string.check_package) + "\n";
                    mUpdateInfoText.setText(mUpdateInfo);
                    // start to upgrade
                    new UpdateSystemThread().start();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.system_local_update);
        mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);

        // init control
        findViews();
        //
        Message msg = myHandler.obtainMessage();
        msg.what = CHECK_STORAGE;
        myHandler.sendMessage(msg);

        // register control event
        registerListeners();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
        intentFilter.addDataScheme("file");
        // register storage change event
        registerReceiver(storageChangeReceiver, intentFilter);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mIsUpdating) {
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        finish();
        unregisterReceiver(storageChangeReceiver);
        super.onDestroy();
    }

    private void findViews() {
        mVersionInfo = new VersionInformation();
        mUpdateButton = (Button) findViewById(R.id.local_immediate);
        mUpdateInfoText = (TextView) findViewById(R.id.local_update_info);
        TextView title = (TextView) findViewById(R.id.local_updateTitle);
        title.setText(getString(R.string.system_local_update));
        mUpdateInfo = getString(R.string.current_version) + Tools.getSystemVersion() + "\n";
        mUpdateInfo += getString(R.string.sdcard) + "\n";
        mUpdateInfoText.setText(mUpdateInfo);
        mUpdateButton.setEnabled(false);
        mCurrentProgressText = (TextView) findViewById(R.id.local_current_progress);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mLinearLayout = (LinearLayout) findViewById(R.id.local_show_progress);
    }

    private void registerListeners() {
        mUpdateButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mHasNewVerison) {
                    if (mScanFiles.size() == 1) {
                        mUpdateFile = mScanFiles.get(0);
                        mLinearLayout.setVisibility(View.VISIBLE);
                        mIsUpdating = true;
                        mUpdateButton.setEnabled(false);
                        mUpdateInfo += getString(R.string.check_package) + "\n";
                        mUpdateInfoText.setText(mUpdateInfo);
                        new UpdateSystemThread().start();
                    } else if (mScanFiles.size() > 1) {
                        mSelectUpdateDialog = new SelectUpdateDialog(SystemLocalUpdateActivity.this);
                        mSelectUpdateDialog.show();
                    }
                } else {
                    finish();
                }
            }
        });
    }

    private void checkStorage() {
        mRetryCount++;
        boolean hasStorage = hasStorageDevice();
        if (hasStorage) {
            myHandler.sendEmptyMessage(CHECK_NEW_VERSION);
        } else {
            if (mRetryCount < CHECK_STORAGE_COUNT) {
                myHandler.sendEmptyMessageDelayed(CHECK_STORAGE, 200);
            } else {
                mUpdateInfo += getString(R.string.no_sdcard) + "\n";
                mUpdateInfoText.setText(mUpdateInfo);
                mUpdateButton.setText(getString(R.string.exit));
                mUpdateButton.setEnabled(true);
            }
        }
    }

    private boolean hasStorageDevice() {
        boolean ret = false;
        mMountedVolumes.clear();
        MStorageManager sm = MStorageManager.getInstance(SystemLocalUpdateActivity.this);
        for (StorageVolume volume : mStorageManager.getVolumeList()) {
            String path = volume.getPath();
            if (Environment.MEDIA_MOUNTED.equals(sm.getVolumeState(path))) {
                Map<String, String> map = new HashMap<String, String>();
                map.put("volume_path", path);
                ret = true;

                String label = sm.getVolumeLabel(path);
                if (TextUtils.isEmpty(label)) {
                    if (Environment.getExternalStorageDirectory().getAbsolutePath().equals(path)) {
                        map.put("volume_lable", getString(R.string.sdcard_lable));
                    } else {
                        map.put("volume_lable", getString(R.string.mobile_stoarge_device));
                    }
                } else {
                    map.put("volume_lable", label);
                }

                mUpdateFile = new File(map.get("volume_path"), "update_signed.zip");
                if (mUpdateFile.exists()) {
                    mMountedVolumes.add(map);
                }
            }
        }
        return ret;
    }

    private void scanUpdateFile() {
        mScanFiles.clear();
        for (Map<String, String> map : mMountedVolumes) {
            mUpdateFile = new File(map.get("volume_path"), "update_signed.zip");
            if (mUpdateFile.exists()) {
                mScanFiles.add(mUpdateFile);
            }
        }

        if (mScanFiles.size() > 0) {
            mHasNewVerison = true;
            mUpdateInfo += getString(R.string.check_updatezip) + "\n";
            mUpdateInfoText.setText(mUpdateInfo);
            mUpdateButton.setText(getString(R.string.update));
        } else {
            mHasNewVerison = false;
            mUpdateInfo += getString(R.string.no_update_file) + "\n";
            mUpdateInfoText.setText(mUpdateInfo);
            mUpdateButton.setText(getString(R.string.exit));
        }
        mUpdateButton.setEnabled(true);
    }

    @SuppressLint("NewApi")
    private boolean verifyPackage() {
        // recovery listener
        RecoverySystem.ProgressListener progressListener = new ProgressListener() {

            @Override
            public void onProgress(int progress) {
                mCurrentProgress = progress;
                myHandler.sendEmptyMessage(UPDATE_PROGRESS);
            }
        };

        // call system interface to update system
        try {
            RecoverySystem.verifyPackage(mUpdateFile, progressListener, null);
            RecoverySystem.installPackage(this, mUpdateFile);

            return true;
        } catch (Exception e) {
            Log.e(TAG, "verifyPackage exception, " + e.getMessage());
            e.printStackTrace();

            return false;
        }
    }

    private void updateSystem() {
        mHasNewVerison = false;
        if (verifyPackage()) {
            myHandler.sendEmptyMessage(UPDATE_SUCCESS);
        } else {
            myHandler.sendEmptyMessage(CHECK_UPDATE_ERROR);
        }
    }

    private BroadcastReceiver storageChangeReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "action, " + action);
            if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
                hasStorageDevice();
                scanUpdateFile();

                mUpdateInfo += getString(R.string.sdcard_insert) + "\n";
                mUpdateInfoText.setText(mUpdateInfo);
            } else if (Intent.ACTION_MEDIA_EJECT.equals(action)) {
                if (mSelectUpdateDialog != null) {
                    mSelectUpdateDialog.dismiss();
                }
                mUpdateInfo += getString(R.string.sdcard_remove) + "\n";
                mUpdateInfoText.setText(mUpdateInfo);
                mUpdateButton.setEnabled(true);

                hasStorageDevice();
                scanUpdateFile();
                if (mScanFiles.size() < 1) {
                    mUpdateButton.setText(getString(R.string.exit));
                } else {
                    mUpdateButton.setText(getString(R.string.update));
                }
            }
        }
    };

    private class UpdateSystemThread extends Thread {

        @Override
        public void run() {
            super.run();

            updateSystem();
        }
    }

}
