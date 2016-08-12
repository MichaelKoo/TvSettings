///<MStar Software>
//******************************************************************************
// MStar Software
// Copyright (c) 2010 - 2015 MStar Semiconductor, Inc. All rights reserved.
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

package com.android.tv.settings.tvos.display.util;

import android.util.Log;

import com.mstar.android.tv.TvPictureManager;
import com.mstar.android.tvapi.common.exception.TvCommonException;
import com.mstar.android.tvapi.common.vo.TimingInfo;
import android.os.SystemProperties;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;

import com.android.tv.settings.tvos.display.PictureSkin;
import com.android.tv.settings.R;

public class TvResolutionUtil {

    private PictureSkin pictureSkin;
    private TvPictureManager mPicturemanger;
    private static final String resProp = "mstar.resolutionState";
    private static String[] resolutionArray;
    private TimingInfo[] timingList;
    public static String ENABLE_4K2K_UI_PRIORITY = "mstar.resolution.4k2kEnableUI";
    private int currentResolution;
    private static final String TAG = "TvResolutionUtil";
    public int reproduceRate = 0;
    private static TvResolutionUtil instance = null;

    public static TvResolutionUtil getInstance() {
        if (instance==null) {
            instance = new TvResolutionUtil();
        }
        return instance;
    }

    private TvResolutionUtil() {
        pictureSkin = PictureSkin.getInstance();
        mPicturemanger = TvPictureManager.getInstance();
        pictureSkin = PictureSkin.getInstance();
        timingList = mPicturemanger.getSupportedTimingList();
        resolutionArray = new String[timingList.length];
        for (int i=0; i<timingList.length; i++) {
            resolutionArray[i] = timingList[i].hResolution+"x"+timingList[i].vResolution+(timingList[i].progressiveMode?"P":"I")+"@"+timingList[i].frameRate+"Hz";
        }
    }

    public String[] getResolutionArray() {
        return resolutionArray;
    }

    public int getCurrentResolution() {
        int timingID = mPicturemanger.getCurrentTimingId();
        Log.d(TAG, "getCurrentResolution: timingID=" + timingID);
        for (int i=0; i<timingList.length; i++) {
            if (timingID == timingList[i].timingID) {
                return i;
            }
        }
        return 0;
    }

    public void setCurrentResolution(int index, Context mContext) {
        Log.d(TAG, "setCurrentResolution: " + resolutionArray[index]+", timingID="+timingList[index].timingID);
        if (timingList[index].hResolution==3840 && timingList[index].vResolution==2160) {
            String enable4K2KPriority = SystemProperties.get(ENABLE_4K2K_UI_PRIORITY);
            if ("0".equals(enable4K2KPriority)) {
                show4K2KNotSupportDialog(mContext);
                return;
            }
        }
        mPicturemanger.SetResolution((byte)timingList[index].timingID);
        reproduceRate = mPicturemanger.GetReproduce();
        Log.d(TAG, "positiveClickSelector reproduceRate : " + reproduceRate);
        pictureSkin.Connect();
        pictureSkin.setSurfaceResolutionMode(timingList[index].hResolution, timingList[index].vResolution, getHStart(index),
                timingList[index].progressiveMode?0:1, 0, reproduceRate);
    }

    private int getHStart(int index) {
        int hstart = 0;
        if (timingList[index].vResolution == 1080) {
            hstart = 192;
        } else if (timingList[index].vResolution == 720) {
            hstart = 260;
        } else if (timingList[index].vResolution == 576) {
            hstart = 132;
        } else if (timingList[index].vResolution == 480) {
            hstart = 122;
        } else if (timingList[index].hResolution == 3840) {
            if (timingList[index].frameRate == 30) {
                hstart = 343;
            } else {
                hstart = 128;
            }
        }
        return hstart;
    }

    protected void show4K2KNotSupportDialog(Context mContext) {
        AlertDialog.Builder builder = new Builder(mContext);
        builder.setMessage(R.string.solution_device_notsupport);
        builder.setTitle(R.string.solution_device_notsupport);
        builder.setPositiveButton(R.string.dlg_ok, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
            });
        builder.create().show();
   }

}
