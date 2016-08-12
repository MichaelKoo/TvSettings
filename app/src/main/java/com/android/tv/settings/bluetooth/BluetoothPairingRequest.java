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

import com.android.tv.settings.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.text.TextUtils;
import android.os.PowerManager;

/**
 * BluetoothPairingRequest is a receiver for any Bluetooth pairing request. It
 * checks if the Bluetooth Settings is currently visible and brings up the PIN, the passkey or a
 * confirmation entry dialog. Otherwise it puts a Notification in the status bar, which can
 * be clicked to bring up the Pairing entry dialog.
 */
public final class BluetoothPairingRequest extends BroadcastReceiver {

    private static final int NOTIFICATION_ID = android.R.drawable.stat_sys_data_bluetooth;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(BluetoothDevice.ACTION_PAIRING_REQUEST)) {
            // convert broadcast intent into activity intent (same action string)
            BluetoothDevice device =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            int type = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT,
                    BluetoothDevice.ERROR);
            Intent pairingIntent = new Intent();
            pairingIntent.setClass(context, BluetoothPairingDialog.class);
            pairingIntent.putExtra(BluetoothDevice.EXTRA_DEVICE, device);
            pairingIntent.putExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT, type);
            if (type == BluetoothDevice.PAIRING_VARIANT_PASSKEY_CONFIRMATION ||
                    type == BluetoothDevice.PAIRING_VARIANT_DISPLAY_PASSKEY ||
                    type == BluetoothDevice.PAIRING_VARIANT_DISPLAY_PIN) {
                int pairingKey = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_KEY,
                        BluetoothDevice.ERROR);
                pairingIntent.putExtra(BluetoothDevice.EXTRA_PAIRING_KEY, pairingKey);
            }
            pairingIntent.setAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
            pairingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            PowerManager powerManager =
                    (PowerManager)context.getSystemService(Context.POWER_SERVICE);
            String deviceAddress = device != null ? device.getAddress() : null;
            if (powerManager.isScreenOn() &&
                    LocalBluetoothPreferences.shouldShowDialogInForeground(context, deviceAddress)) {
                // Since the screen is on and the BT-related activity is in the foreground,
                // just open the dialog
                context.startActivity(pairingIntent);
            } else {
                // Put up a notification that leads to the dialog
                Resources res = context.getResources();
                Notification.Builder builder = new Notification.Builder(context)
                        .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
                        .setTicker(res.getString(R.string.bluetooth_notif_ticker));

                PendingIntent pending = PendingIntent.getActivity(context, 0,
                        pairingIntent, PendingIntent.FLAG_ONE_SHOT);

                String name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
                if (TextUtils.isEmpty(name)) {
                    name = device != null ? device.getAliasName() :
                            context.getString(android.R.string.unknownName);
                }

                builder.setContentTitle(res.getString(R.string.bluetooth_notif_title))
                        .setContentText(res.getString(R.string.bluetooth_notif_message, name))
                        .setContentIntent(pending)
                        .setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_SOUND)
                        .setColor(res.getColor(
                                com.android.internal.R.color.system_notification_accent_color));

                NotificationManager manager = (NotificationManager)
                        context.getSystemService(Context.NOTIFICATION_SERVICE);
                manager.notify(NOTIFICATION_ID, builder.getNotification());
            }

        } else if (action.equals(BluetoothDevice.ACTION_PAIRING_CANCEL)) {

            // Remove the notification
            NotificationManager manager = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancel(NOTIFICATION_ID);

        } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
            int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE,
                    BluetoothDevice.ERROR);
            int oldState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE,
                    BluetoothDevice.ERROR);
            if((oldState == BluetoothDevice.BOND_BONDING) &&
                    (bondState == BluetoothDevice.BOND_NONE)) {
                // Remove the notification
                NotificationManager manager = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
                manager.cancel(NOTIFICATION_ID);
            }
        }
    }
}
