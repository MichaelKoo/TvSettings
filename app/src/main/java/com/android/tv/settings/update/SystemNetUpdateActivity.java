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

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RecoverySystem;
import android.os.RecoverySystem.ProgressListener;
import android.os.StatFs;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.tv.settings.R;
import com.android.tv.settings.util.Tools;

public class SystemNetUpdateActivity extends Activity {

    private final static String TAG = "SystemNetUpdateActivity";

    private final static String NAME = "share_pres";

    private final static String DOWNLOAD_ADDRESS = "url";

    private final static String VERSION = "version";

    private final static int CHECK_STORAGE = 0;

    private final static int CHECK_NEW_VERSION = 1;

    private final static int DOWNLOAD_ERROR = 2;

    private final static int CHECK_UPDATE_ERROR = 3;

    private final static int UPDATE_SUCCESS = 4;

    private final static int UPDATE_PROGRESS = 5;

    private final static int CHECK_VERSION__FAIL = 6;

    private final static int NUM_STORAGE_CHECKS = 10;

    private final static String PERCENT = "percent";

    private final static String PERCENT_CHANGED = "percent_changed";

    private Button mUpdateButton;

    private TextView mUpdateInfoText;

    private String mUpdateInfo;

    private String mNewVersion;

    private boolean mHasNewVersion = false;

    private boolean mHasStorage;

    private int mRetryCount;

    private boolean mHasDownloaded = false;

    private VersionInformation mVersionInfo;

    private String mDownloadUrl;

    private LinearLayout mLayout;

    private ProgressBar mProgressBar;

    private TextView mCurrentProgressText;

    private int mCurrentProgress;

    private boolean isUpdating = false;

    private boolean mLocationFlag = false;

    private long mPackageSize;

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CHECK_STORAGE:
                    checkStorage();
                    break;
                case CHECK_NEW_VERSION:
                    mHasNewVersion = checkUpdateVersion();
                    if (mHasNewVersion) {
                        mUpdateInfo += getString(R.string.new_version) + mNewVersion + "\n";
                        mUpdateInfo += getString(R.string.update_list) + mVersionInfo.getUds()
                                + "\n";
                        mUpdateInfoText.setText(mUpdateInfo);
                        mUpdateButton.setText(getString(R.string.download));
                        mUpdateButton.setEnabled(true);
                    } else {
                        mUpdateInfo += getString(R.string.latest) + "\n";
                        mUpdateInfoText.setText(mUpdateInfo);
                        mUpdateButton.setText(getString(R.string.exit));
                        mUpdateButton.setEnabled(true);
                    }
                    break;
                case DOWNLOAD_ERROR:
                    mUpdateInfo += getString(R.string.download_error) + "\n";
                    mUpdateInfoText.setText(mUpdateInfo);
                    mUpdateButton.setText(getString(R.string.continue_download));
                    mUpdateButton.setEnabled(true);
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
                    mProgressBar.setProgress(mCurrentProgress);
                    mProgressBar.setVisibility(View.VISIBLE);
                    if (mCurrentProgress == 100) {
                        mUpdateInfo += getString(R.string.check_success);
                        mUpdateInfoText.setText(mUpdateInfo);
                        mProgressBar.setVisibility(View.INVISIBLE);
                        mLayout.setVisibility(View.INVISIBLE);
                    }
                    break;
                case CHECK_VERSION__FAIL:
                    mUpdateInfo += getString(R.string.check_version_fail);
                    mUpdateInfoText.setText(mUpdateInfo);
                    mUpdateButton.setText(getString(R.string.exit));
                    mUpdateButton.setEnabled(true);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.system_net_update);
        findViews();

        checkStorage();

        new Thread(new Runnable() {
            @Override
            public void run() {
                getVersionInfo();
            }
        }).start();

        registVolumeReceiver();
        registerListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCurrentProgress = getPercentData(PERCENT);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PERCENT_CHANGED);
        registerReceiver(percentReceiver, intentFilter);
        IntentFilter updateErrorFilter = new IntentFilter();
        updateErrorFilter.addAction(ACTION_ERROR);
        registerReceiver(updateErrorReceiver, updateErrorFilter);
    }

    private void registVolumeReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
        intentFilter.addDataScheme("file");
        registerReceiver(sdcardChangeReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
        unregisterReceiver(sdcardChangeReceiver);
        unregisterReceiver(percentReceiver);
        unregisterReceiver(updateErrorReceiver);
    }

    private void findViews() {
        mVersionInfo = new VersionInformation();
        mUpdateInfoText = (TextView) findViewById(R.id.net_update_info);
        TextView title = (TextView) findViewById(R.id.updateTitle);
        mUpdateButton = (Button) findViewById(R.id.immediate);
        mUpdateButton.setEnabled(false);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mCurrentProgressText = (TextView) findViewById(R.id.current_progress);
        mLayout = (LinearLayout) findViewById(R.id.show_progress);

        title.setText(getString(R.string.system_net_update));
        mUpdateInfo = getString(R.string.current_version) + Tools.getSystemVersion() + "\n";
        mUpdateInfo += getString(R.string.sdcard);
        mUpdateInfoText.setText(mUpdateInfo);
    }

    private void registerListeners() {
        mUpdateButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mHasNewVersion) {
                    if (!isNetConnected()) {
                        Toast.makeText(SystemNetUpdateActivity.this,
                                getString(R.string.not_network), Toast.LENGTH_SHORT).show();

                        return;
                    }
                    mUpdateInfo += getString(R.string.downloading) + "\n";
                    mUpdateInfoText.setText(mUpdateInfo);
                    mLayout.setVisibility(View.INVISIBLE);
                    Log.d(TAG, "downUrl, " + mVersionInfo.getUrl());
                    Log.d(TAG, "newVersion, " + mVersionInfo.getVersion());
                    Log.d(TAG, "size, " + mVersionInfo.getSize());
                    Intent intent = new Intent(SystemNetUpdateActivity.this, UpdateService.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("downUrl", mDownloadUrl);
                    bundle.putString("newVersion", mNewVersion);
                    bundle.putString("size", String.valueOf(mPackageSize));
                    intent.putExtras(bundle);
                    SystemNetUpdateActivity.this.startService(intent);
                    mUpdateButton.setEnabled(false);
                    mUpdateButton.setText(getString(R.string.download_ing));

                } else if (mHasDownloaded) {
                    mUpdateInfo += getString(R.string.check_package);
                    mUpdateInfoText.setText(mUpdateInfo);
                    mHasDownloaded = false;
                    mLayout.setVisibility(View.VISIBLE);
                    mProgressBar.setProgress(0);
                    mUpdateButton.setEnabled(false);
                    isUpdating = true;

                    new UpdateSystemThread().start();
                } else {
                    finish();
                }
            }
        });
    }

    private void checkStorage() {
        long cacheFreeSize = getCacheFreeSize();
        mPackageSize = mVersionInfo.getSize();
        if (cacheFreeSize > mPackageSize) {
            mHasStorage = true;
            mLocationFlag = false;
            mUpdateInfo += getString(R.string.cache_success) + "\n";
            mUpdateInfo += getString(R.string.check_new_version) + "\n";
            mUpdateInfoText.setText(mUpdateInfo);
        } else {
            mRetryCount++;
            mHasStorage = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
            if (!mHasStorage) {
                if (mRetryCount < NUM_STORAGE_CHECKS) {
                    handler.sendEmptyMessageDelayed(CHECK_STORAGE, 200);
                } else {
                    mUpdateInfo += getString(R.string.no_sdcard) + "\n";
                    mUpdateInfoText.setText(mUpdateInfo);
                    mUpdateButton.setText(getString(R.string.exit));
                    mUpdateButton.setEnabled(true);
                    mUpdateButton.requestFocus();
                }
            } else {
                mLocationFlag = true;
                mUpdateInfo += getString(R.string.sdcard_success) + "\n";
                mUpdateInfoText.setText(mUpdateInfo);
                mUpdateInfo += getString(R.string.check_new_version) + "\n";
                mUpdateInfoText.setText(mUpdateInfo);
            }
        }
    }

    private void startDownloadPackage() {
        if (mCurrentProgress != 100) {
            new GetJsonDataThread().start();
        } else {
            enableUpdateButton();
        }
    }

    private void init() {
        String directoryName = "";
        mPackageSize = mVersionInfo.getSize();
        long cacheFreeSize = getCacheFreeSize();
        if (cacheFreeSize > mPackageSize) {
            directoryName = "/cache/versioninfor";
        } else {
            directoryName = Environment.getExternalStorageDirectory().toString() + "/versioninfor";
        }

        Tools.string2File(JSONData.getUpgradeInfo(), directoryName);
    }

    private boolean checkUpdateVersion() {
        init();

        mNewVersion = mVersionInfo.getVersion().substring(1);
        mPackageSize = mVersionInfo.getSize();
        mDownloadUrl = mVersionInfo.getUrl();
        if (mDownloadUrl != null) {
            commitURLValue(DOWNLOAD_ADDRESS, mDownloadUrl);
            commitURLValue(VERSION, mNewVersion);
        }

        return checkVersion();
    }

    private boolean checkVersion() {
        final String systemVersion = Tools.getSystemVersion();
        if (systemVersion == null) {
            mHasNewVersion = true;

            return mHasNewVersion;
        }

        if (systemVersion.equals(mNewVersion)) {
            mHasNewVersion = false;

            return mHasNewVersion;
        }

        if (mNewVersion != null && systemVersion.contains(".")) {
            String systemOne = systemVersion.split("\\.")[0];
            String newOne = mNewVersion.split("\\.")[0];

            if (Integer.parseInt(newOne) > Integer.parseInt(systemOne)) {
                mHasNewVersion = true;

            } else if (Integer.parseInt(newOne) == Integer.parseInt(systemOne)) {
                String systemTwo = systemVersion.split("\\.")[1];
                String newTwo = mNewVersion.split("\\.")[1];
                if (Integer.parseInt(newTwo) > Integer.parseInt(systemTwo)) {
                    mHasNewVersion = true;

                } else if (Integer.parseInt(newTwo) == Integer.parseInt(systemTwo)) {
                    String systemThree = systemVersion.split("\\.")[2];
                    String newThree = mNewVersion.split("\\.")[2];
                    if (Integer.parseInt(newThree) > Integer.parseInt(systemThree)) {
                        mHasNewVersion = true;

                    } else if (Integer.parseInt(newThree) == Integer.parseInt(systemThree)) {
                        String systemFour = "";
                        String newFour = "";
                        if (systemVersion.split("\\.").length >= 4) {
                            systemFour = systemVersion.split("\\.")[3];
                        } else {
                            systemFour = "0";
                        }

                        if (mNewVersion.split("\\.").length >= 4) {
                            newFour = mNewVersion.split("\\.")[3];
                        } else {
                            newFour = "0";
                        }

                        if (Integer.parseInt(newFour) > Integer.parseInt(systemFour)) {
                            mHasNewVersion = true;
                        } else {
                            mHasNewVersion = false;
                        }
                    } else {
                        mHasNewVersion = false;
                    }
                } else {
                    mHasNewVersion = false;
                }
            } else {
                mHasNewVersion = false;
            }
        }

        return mHasNewVersion;
    }

    private void enableUpdateButton() {
        if (mCurrentProgress == 100) {
            mUpdateInfo += getString(R.string.downloaded) + "\n" + getString(R.string.to_update)
                    + "\n";
            mUpdateInfoText.setText(mUpdateInfo);
            mUpdateButton.setText(getString(R.string.update));
            mUpdateButton.setEnabled(true);
            mHasDownloaded = true;
            mHasNewVersion = false;
        }
    }

    private void updateSystem() {
        if (verify()) {
            handler.sendEmptyMessage(UPDATE_SUCCESS);
        } else {
            handler.sendEmptyMessage(CHECK_UPDATE_ERROR);
        }
    }

    @SuppressLint({
            "NewApi", "NewApi"
    })
    private boolean verify() {
        String directoryName = "";
        if (mLocationFlag) {
            directoryName = Environment.getExternalStorageDirectory().toString()
                    + "/update_signed.zip";
        } else {
            directoryName = "/cache/update_signed.zip";
        }

        RecoverySystem.ProgressListener progressListener = new ProgressListener() {
            @Override
            public void onProgress(int progress) {
                mCurrentProgress = progress;
                handler.sendEmptyMessage(UPDATE_PROGRESS);
            }
        };

        try {
            File file = new File(directoryName);
            RecoverySystem.verifyPackage(file, progressListener, null);
            commitPercentValue(PERCENT, 0);
            RecoverySystem.installPackage(this, file);
            isUpdating = false;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void getVersionInfo() {
        try {
            JSONObject reqJson = new JSONObject(JSONData.getUpgradeInfo());
            Log.d(TAG, ">>>>reqjson>>>>" + reqJson.getInt("err"));
            if (reqJson.getInt("err") == 0) {
                JSONObject jsonObject = reqJson.getJSONObject("bd");
                mVersionInfo.setDs(jsonObject.get("ds").toString());
                mVersionInfo.setVersion(jsonObject.get("ver").toString());
                mVersionInfo.setUds(jsonObject.get("uds").toString());
                mVersionInfo.setUrl(jsonObject.get("url").toString());
                mVersionInfo.setSize(jsonObject.getLong("size"));
                mVersionInfo.setMd(jsonObject.getString("md5"));
                mVersionInfo.setForce(jsonObject.getInt("fd"));

                startDownloadPackage();
            } else if (reqJson.getInt("err") == 1) {
                handler.sendEmptyMessage(CHECK_VERSION__FAIL);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            handler.sendEmptyMessage(CHECK_VERSION__FAIL);
        }
    }

    private void commitURLValue(String key, String value) {
        SharedPreferences preference = getSharedPreferences(NAME, Context.MODE_PRIVATE);
        Editor edit = preference.edit();
        edit.putString(key, value);
        edit.commit();
    }

    private void setButtonValue(int id, boolean enable) {
        mUpdateButton.setEnabled(enable);
        mUpdateButton.setText(getString(id));
    }

    private BroadcastReceiver sdcardChangeReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
                String msg = getString(R.string.sdcard_insert);
                mUpdateInfo += msg + "\n";
                mUpdateInfoText.setText(mUpdateInfo);

                new GetJsonDataThread().run();
            } else if (Intent.ACTION_MEDIA_EJECT.equals(action) && mLocationFlag) {
                String msg = getString(R.string.sdcard_remove);
                mUpdateInfo += msg + "\n";
                mUpdateInfoText.setText(mUpdateInfo);
                mHasNewVersion = false;
                setButtonValue(R.string.exit, true);
            }
        }
    };

    private BroadcastReceiver percentReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(PERCENT_CHANGED)) {
                mCurrentProgress = intent.getIntExtra(PERCENT, 0);
                Log.d(TAG, "cur_progress:" + mCurrentProgress);
                enableUpdateButton();
            }
        }
    };

    private static final String KEY_ERROR = "key_update_error";

    private static final String ACTION_ERROR = "action_update_error";

    private static final int ERROR_DOWNLOAD = 1;

    private BroadcastReceiver updateErrorReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(ACTION_ERROR)) {
                int code = intent.getIntExtra(KEY_ERROR, 0);
                Log.d(TAG, "update error code : " + code);
                if (ERROR_DOWNLOAD == code) {
                    handler.sendEmptyMessage(DOWNLOAD_ERROR);
                }
            }
        }
    };

    private boolean isNetConnected() {
        ConnectivityManager cm =
            (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private int getPercentData(String key) {
        SharedPreferences preference = getSharedPreferences(NAME, Context.MODE_PRIVATE);
        return preference.getInt(key, 0);
    }

    private void commitPercentValue(String key, int percent) {
        SharedPreferences preference = getSharedPreferences(NAME, Context.MODE_PRIVATE);
        Editor edit = preference.edit();
        edit.putInt(key, percent);
        edit.commit();
    }

    class GetJsonDataThread extends Thread {

        @Override
        public void run() {
            super.run();
            handler.sendEmptyMessage(CHECK_NEW_VERSION);
        }
    }

    class UpdateSystemThread extends Thread {

        @Override
        public void run() {
            super.run();
            updateSystem();
        }
    }

    class GetVersionInfo extends Thread {

        @Override
        public void run() {
            super.run();
            getVersionInfo();
        }
    }

    private long getCacheFreeSize() {
        StatFs sf = new StatFs("/cache");
        long blockSize = sf.getBlockSize();
        long availCount = sf.getAvailableBlocks();

        return availCount * blockSize / 1024;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (isUpdating) {
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

}
