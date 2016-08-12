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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.StatFs;
import android.util.Log;
import android.widget.RemoteViews;

import com.android.tv.settings.R;

public class UpdateService extends Service {

    private static final String TAG = "MSettings.UpdateService";

    private final static int DOWNLOADING = 0;

    private final static int DOWNLOAD_ERROR = 3;

    private final static String NAME = "share_pres";

    private final static String PERCENT = "percent";

    private final static String PERCENT_CHANGED = "percent_changed";

    private Notification mNotification;

    private NotificationManager mNotificationManager;

    private PendingIntent mPendingIntent;

    private RemoteViews mRemoteViews;

    private String mDownloadUrl;

    private String mNewVersion;

    private int mDownloadPercent;

    private String mSize;

    private Handler mHandler = new Handler() {

        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            Log.d(TAG, "mHandler msg " + msg.what);
            if (msg.what <= 100) {
                if (msg.what == 0) {
                    mNotification.contentView = mRemoteViews;
                    mNotification.contentIntent = mPendingIntent;
                    mNotificationManager.notify(DOWNLOADING, mNotification);
                }
                if (msg.what == 100) {
                    commitPercentValue(PERCENT, 100);
                    mNotificationManager.cancel(DOWNLOADING);
                    mNotification.flags |= Notification.FLAG_AUTO_CANCEL;
                    showNotification(R.drawable.appwidget_bg_holo, R.string.downloading, msg.what);
                }
                mRemoteViews.setTextViewText(R.id.task_percent, msg.what + "%");
                mRemoteViews.setProgressBar(R.id.task_progressbar, 100, msg.what, false);
                sendPercentData();
            }
            mNotification.contentView = mRemoteViews;
            mNotification.contentIntent = mPendingIntent;
            mNotificationManager.notify(DOWNLOADING, mNotification);
        };
    };

    private Handler mErrorHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == DOWNLOAD_ERROR) {
                commitPercentValue(PERCENT, mDownloadPercent);
                onUpdateError(ERROR_DOWNLOAD);
            }
        };
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mDownloadPercent = getPercentData(PERCENT);
        showNotification(R.drawable.appwidget_bg_holo, R.string.downloading, mDownloadPercent);

        Message msg = mHandler.obtainMessage();
        msg.what = mDownloadPercent;
        mHandler.sendMessage(msg);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mDownloadUrl = intent.getStringExtra("downUrl");
        mNewVersion = intent.getStringExtra("newVersion");
        mSize = intent.getStringExtra("size");
        Log.d(TAG, "UpdateService-downUrl, " + mDownloadUrl);
        Log.d(TAG, "UpdateService-newVersion, " + mNewVersion);
        Log.d(TAG, "UpdateService-mSize, " + mSize);
        startDownload();

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * start download the update package.
     */
    private void startDownload() {
        String directoryName = "";
        long cacheFreeSize = getCacheFreeSize();
        if (cacheFreeSize > Long.parseLong(mSize)) {
            directoryName = "/cache/update_signed.zip";
        } else {
            directoryName = Environment.getExternalStorageDirectory().toString()
                    + "/update_signed.zip";
        }

        IDownloadProgressListener downloadProgressListener = new IDownloadProgressListener() {

            @Override
            public void onDownloadSizeChange(int percent) {
                mDownloadPercent = percent;
                Log.d(TAG, "percent:" + percent);
                Message msg = mHandler.obtainMessage();
                msg.what = percent;
                mHandler.sendMessage(msg);
            }
        };

        UpgradeTask upgradeTask = new UpgradeTask(UpdateService.this, mDownloadUrl, directoryName,
                mNewVersion, downloadProgressListener, mErrorHandler);

        new Thread(upgradeTask).start();
    }

    private void showNotification(int drawbale, int titleId, int percent) {
        mNotification = new Notification(drawbale, getString(R.string.update_packages_download),
                System.currentTimeMillis());
        mRemoteViews = new RemoteViews(getApplication().getPackageName(),
                R.layout.download_progress);
        Intent intent = new Intent();
        if (percent == 100) {
            intent.setClass(this, SystemNetUpdateActivity.class);
            Log.d(TAG, "showNotification 100");
        } else {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            Log.d(TAG, "showNotification downloadPercent, " + mDownloadPercent);
        }
        mPendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void commitPercentValue(String key, int percent) {
        SharedPreferences preference = getSharedPreferences(NAME, Context.MODE_PRIVATE);
        Editor edit = preference.edit();
        edit.putInt(key, percent);
        edit.commit();
    }

    private int getPercentData(String key) {
        SharedPreferences preference = getSharedPreferences(NAME, Context.MODE_PRIVATE);
        return preference.getInt(key, 0);
    }

    private void sendPercentData() {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putInt(PERCENT, mDownloadPercent);
        intent.setAction(PERCENT_CHANGED);
        intent.putExtras(bundle);
        sendBroadcast(intent);
    }

    private long getCacheFreeSize() {
        StatFs sf = new StatFs("/cache");
        long blockSize = sf.getBlockSize();
        long availCount = sf.getAvailableBlocks();

        return availCount * blockSize;
    }

    private static final String KEY_ERROR = "key_update_error";

    private static final String ACTION_ERROR = "action_update_error";

    private static final int ERROR_DOWNLOAD = 1;

    private void onUpdateError(int errorCode) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_ERROR, errorCode);
        intent.setAction(ACTION_ERROR);
        intent.putExtras(bundle);
        sendBroadcast(intent);
    }
}
