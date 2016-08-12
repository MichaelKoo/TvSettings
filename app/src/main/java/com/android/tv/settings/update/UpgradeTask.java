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
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

public class UpgradeTask implements Runnable {

    private final static String TAG = "MSettings.UpgradeTask";

    private final static String DOWNLOAD_ADDRESS = "url";

    private final static String VERSION = "version";

    private final static String NAME = "share_pres";

    private final static int DOWNLOAD_ERROR = 3;

    private long mDownloadedSize = 0;

    private long mTotalSize;

    private int mDownloadPercent;

    private IDownloadProgressListener mDownloadProgressListener;

    private String mLocalPath;

    private String mUpgradeURL;

    private String mVersion;

    private Context mContext;

    private Handler mHandler;

    public UpgradeTask(Context context, String upgradeURL, String localPath, String version,
            IDownloadProgressListener dpListener, Handler handler) {
        this.mDownloadProgressListener = dpListener;
        this.mLocalPath = localPath;
        this.mUpgradeURL = upgradeURL;
        this.mVersion = version;
        this.mContext = context;
        this.mHandler = handler;
    }

    private void prepare() {
        File file = new File(mLocalPath);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        String versionString = getStringData(VERSION);
        if (versionString.equals(mVersion)) {
            mUpgradeURL = getStringData(DOWNLOAD_ADDRESS);
        } else {
            Log.d(TAG, "delete file");
            file.delete();
        }
    }

    /*
     * @see
     * com.jrm.core.container.cmps.upgrade.task.BaseUpgradeTask#onDownload()
     */
    protected boolean download() {
        File file = new File(mLocalPath);
        if (file.exists()) {
            mDownloadedSize = file.length();
        } else {
            mDownloadedSize = 0;
        }
        Log.d(TAG, "mUpgradeURL, " + mUpgradeURL + " downloadedSize, " + mDownloadedSize);

        HttpURLConnection httpConnection = null;
        URL url = null;
        try {
            url = new URL(mUpgradeURL);
            httpConnection = (HttpURLConnection) url.openConnection();
            mTotalSize = httpConnection.getContentLength();
            Log.d(TAG, "totalSize, " + mTotalSize);
            if (mDownloadedSize == mTotalSize && this.mDownloadProgressListener != null) {
                mDownloadProgressListener.onDownloadSizeChange(100);
                return true;
            } else if (mDownloadedSize > mTotalSize) {
                if (!file.delete()) {
                    return false;
                }
            }
            httpConnection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (httpConnection != null) {
                    httpConnection.disconnect();
                }
            } catch (Exception e) {
            }
        }

        InputStream inStream = null;
        RandomAccessFile randomAccessFile = null;
        try {
            httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestProperty("Accept", "image/gif, " + "image/jpeg, "
                    + "image/pjpeg, " + "image/pjpeg, " + "application/x-shockwave-flash, "
                    + "application/xaml+xml, " + "application/vnd.ms-xpsdocument, "
                    + "application/x-ms-xbap, " + "application/x-ms-application, "
                    + "application/vnd.ms-excel, " + "application/vnd.ms-powerpoint, "
                    + "application/msword, " + "*/*");
            httpConnection.setRequestProperty("Accept-Language", "zh-CN");
            httpConnection.setRequestProperty("Referer", mUpgradeURL);
            httpConnection.setRequestProperty("Charset", "UTF-8");
            httpConnection.setRequestProperty("Range", "bytes=" + mDownloadedSize + "-");
            httpConnection.setRequestProperty("Connection", "Keep-Alive");

            inStream = httpConnection.getInputStream();

            File saveFile = new File(mLocalPath);
            randomAccessFile = new RandomAccessFile(saveFile, "rwd");
            randomAccessFile.seek(mDownloadedSize);

            int offset = 0;
            int count = 0;
            int perUnit = (int) mTotalSize / 1024 / 100;
            byte[] buffer = new byte[1024];
            while ((offset = inStream.read(buffer, 0, 1024)) != -1) {
                randomAccessFile.write(buffer, 0, offset);
                count++;
                if (count == perUnit && mDownloadedSize < mTotalSize) {
                    mDownloadPercent = (int) (mDownloadedSize * 100 / mTotalSize);
                    if (this.mDownloadProgressListener != null) {
                        mDownloadProgressListener.onDownloadSizeChange(mDownloadPercent);
                    }
                    count = 0;
                }
                mDownloadedSize += offset;
            }

            if (mDownloadedSize == mTotalSize && this.mDownloadProgressListener != null) {
                mDownloadProgressListener.onDownloadSizeChange(100);
            }
            Log.d(TAG, "download finished.");

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (inStream != null) {
                    inStream.close();
                }
                if (httpConnection != null) {
                    httpConnection.disconnect();
                }
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        prepare();

        if (!download()) {
            Log.d(TAG, "download failed");
            mHandler.sendEmptyMessage(DOWNLOAD_ERROR);
        }
    };

    private String getStringData(String key) {
        SharedPreferences preference = mContext.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        return preference.getString(key, "");
    }
}
