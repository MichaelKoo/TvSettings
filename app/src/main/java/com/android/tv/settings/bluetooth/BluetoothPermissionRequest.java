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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import com.android.tv.settings.R;

/**
 * BluetoothPermissionRequest is a receiver to receive Bluetooth connection
 * access request.
 */
public final class BluetoothPermissionRequest extends BroadcastReceiver {

    private static final String TAG = "BluetoothPermissionRequest";
    private static final boolean DEBUG = Utils.V;
    private static final int NOTIFICATION_ID = android.R.drawable.stat_sys_data_bluetooth;

    private static final String NOTIFICATION_TAG_PBAP = "Phonebook Access" ;
    private static final String NOTIFICATION_TAG_MAP = "Message Access";


    Context mContext;
    int mRequestType;
    BluetoothDevice mDevice;
    String mReturnPackage = null;
    String mReturnClass = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        String action = intent.getAction();

        if (DEBUG) Log.d(TAG, "onReceive" + action);

        if (action.equals(BluetoothDevice.ACTION_CONNECTION_ACCESS_REQUEST)) {
            // convert broadcast intent into activity intent (same action string)
            mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            mRequestType = intent.getIntExtra(BluetoothDevice.EXTRA_ACCESS_REQUEST_TYPE,
                                                 BluetoothDevice.REQUEST_TYPE_PROFILE_CONNECTION);
            mReturnPackage = intent.getStringExtra(BluetoothDevice.EXTRA_PACKAGE_NAME);
            mReturnClass = intent.getStringExtra(BluetoothDevice.EXTRA_CLASS_NAME);

            if (DEBUG) Log.d(TAG, "onReceive request type: " + mRequestType + " return "
                    + mReturnPackage + "," + mReturnClass);

            // Even if the user has already made the choice, Bluetooth still may not know that if
            // the user preference data have not been migrated from Settings app's shared
            // preferences to Bluetooth app's. In that case, Bluetooth app broadcasts an
            // ACTION_CONNECTION_ACCESS_REQUEST intent to ask to Settings app.
            //
            // If that happens, 'checkUserChoice()' here will do migration because it finds or
            // creates a 'CachedBluetoothDevice' object for the device.
            //
            // After migration is done, 'checkUserChoice()' replies to the request by sending an
            // ACTION_CONNECTION_ACCESS_REPLY intent. And we don't need to start permission activity
            // dialog or notification.
            if (checkUserChoice()) {
                return;
            }

            Intent connectionAccessIntent = new Intent(action);
            connectionAccessIntent.setClass(context, BluetoothPermissionActivity.class);
            // We use the FLAG_ACTIVITY_MULTIPLE_TASK since we can have multiple concurrent access
            // requests.
            connectionAccessIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            // This is needed to create two pending intents to the same activity. The value is not
            // used in the activity.
            connectionAccessIntent.setType(Integer.toString(mRequestType));
            connectionAccessIntent.putExtra(BluetoothDevice.EXTRA_ACCESS_REQUEST_TYPE,
                                            mRequestType);
            connectionAccessIntent.putExtra(BluetoothDevice.EXTRA_DEVICE, mDevice);
            connectionAccessIntent.putExtra(BluetoothDevice.EXTRA_PACKAGE_NAME, mReturnPackage);
            connectionAccessIntent.putExtra(BluetoothDevice.EXTRA_CLASS_NAME, mReturnClass);

            String deviceAddress = mDevice != null ? mDevice.getAddress() : null;
            String title = null;
            String message = null;
            PowerManager powerManager =
                (PowerManager) context.getSystemService(Context.POWER_SERVICE);

            if (powerManager.isScreenOn()
                    && LocalBluetoothPreferences.shouldShowDialogInForeground(
                            context, deviceAddress)) {
                context.startActivity(connectionAccessIntent);
            } else {
                // Put up a notification that leads to the dialog

                // Create an intent triggered by clicking on the
                // "Clear All Notifications" button

                Intent deleteIntent = new Intent(BluetoothDevice.ACTION_CONNECTION_ACCESS_REPLY);
                deleteIntent.putExtra(BluetoothDevice.EXTRA_DEVICE, mDevice);
                deleteIntent.putExtra(BluetoothDevice.EXTRA_CONNECTION_ACCESS_RESULT,
                        BluetoothDevice.CONNECTION_ACCESS_NO);
                deleteIntent.putExtra(BluetoothDevice.EXTRA_ACCESS_REQUEST_TYPE, mRequestType);
                String deviceName = mDevice != null ? mDevice.getAliasName() : null;
                switch (mRequestType) {
                    case BluetoothDevice.REQUEST_TYPE_PHONEBOOK_ACCESS:
                        title = context.getString(R.string.bluetooth_phonebook_request);
                        message = context.getString(R.string.bluetooth_pb_acceptance_dialog_text,
                                deviceName, deviceName);
                        break;
                    case BluetoothDevice.REQUEST_TYPE_MESSAGE_ACCESS:
                        title = context.getString(R.string.bluetooth_map_request);
                        message = context.getString(R.string.bluetooth_map_acceptance_dialog_text,
                                deviceName, deviceName);
                        break;
                    default:
                        title = context.getString(R.string.bluetooth_connection_permission_request);
                        message = context.getString(R.string.bluetooth_connection_dialog_text,
                                deviceName, deviceName);
                        break;
                }
                Notification notification = new Notification.Builder(context)
                        .setContentTitle(title)
                        .setTicker(message)
                        .setContentText(message)
                        .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
                        .setAutoCancel(true)
                        .setPriority(Notification.PRIORITY_MAX)
                        .setOnlyAlertOnce(false)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setContentIntent(PendingIntent.getActivity(context, 0,
                                connectionAccessIntent, 0))
                        .setDeleteIntent(PendingIntent.getBroadcast(context, 0, deleteIntent, 0))
                        .setColor(context.getResources().getColor(
                                com.android.internal.R.color.system_notification_accent_color))
                        .build();

                notification.flags |= Notification.FLAG_NO_CLEAR; // Cannot be set with the builder.

                NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                notificationManager.notify(getNotificationTag(mRequestType), NOTIFICATION_ID,
                        notification);
            }
        } else if (action.equals(BluetoothDevice.ACTION_CONNECTION_ACCESS_CANCEL)) {
            // Remove the notification
            NotificationManager manager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
            mRequestType = intent.getIntExtra(BluetoothDevice.EXTRA_ACCESS_REQUEST_TYPE,
                                        BluetoothDevice.REQUEST_TYPE_PHONEBOOK_ACCESS);
            manager.cancel(getNotificationTag(mRequestType), NOTIFICATION_ID);
        }
    }

    private String getNotificationTag(int requestType) {
        if(requestType == BluetoothDevice.REQUEST_TYPE_PHONEBOOK_ACCESS) {
            return NOTIFICATION_TAG_PBAP;
        } else if(mRequestType == BluetoothDevice.REQUEST_TYPE_MESSAGE_ACCESS) {
            return NOTIFICATION_TAG_MAP;
        }
        return null;
    }

    /**
     * @return true user had made a choice, this method replies to the request according
     *              to user's previous decision
     *         false user hadnot made any choice on this device
     */
    private boolean checkUserChoice() {
        boolean processed = false;

        // ignore if it is something else than phonebook/message settings it wants us to remember
        if (mRequestType != BluetoothDevice.REQUEST_TYPE_PHONEBOOK_ACCESS
                && mRequestType != BluetoothDevice.REQUEST_TYPE_MESSAGE_ACCESS) {
            if (DEBUG) Log.d(TAG, "checkUserChoice(): Unknown RequestType " + mRequestType);
            return processed;
        }

        LocalBluetoothManager bluetoothManager = LocalBluetoothManager.getInstance(mContext);
        CachedBluetoothDeviceManager cachedDeviceManager =
            bluetoothManager.getCachedDeviceManager();
        CachedBluetoothDevice cachedDevice = cachedDeviceManager.findDevice(mDevice);
        if (cachedDevice == null) {
            cachedDevice = cachedDeviceManager.addDevice(bluetoothManager.getBluetoothAdapter(),
                bluetoothManager.getProfileManager(), mDevice);
        }

        String intentName = BluetoothDevice.ACTION_CONNECTION_ACCESS_REPLY;

        if (mRequestType == BluetoothDevice.REQUEST_TYPE_PHONEBOOK_ACCESS) {
            int phonebookPermission = cachedDevice.getPhonebookPermissionChoice();

            if (phonebookPermission == CachedBluetoothDevice.ACCESS_UNKNOWN) {
                // Leave 'processed' as false.
            } else if (phonebookPermission == CachedBluetoothDevice.ACCESS_ALLOWED) {
                sendReplyIntentToReceiver(true);
                processed = true;
            } else if (phonebookPermission == CachedBluetoothDevice.ACCESS_REJECTED) {
                sendReplyIntentToReceiver(false);
                processed = true;
            } else {
                Log.e(TAG, "Bad phonebookPermission: " + phonebookPermission);
            }
        } else if (mRequestType == BluetoothDevice.REQUEST_TYPE_MESSAGE_ACCESS) {
            int messagePermission = cachedDevice.getMessagePermissionChoice();

            if (messagePermission == CachedBluetoothDevice.ACCESS_UNKNOWN) {
                // Leave 'processed' as false.
            } else if (messagePermission == CachedBluetoothDevice.ACCESS_ALLOWED) {
                sendReplyIntentToReceiver(true);
                processed = true;
            } else if (messagePermission == CachedBluetoothDevice.ACCESS_REJECTED) {
                sendReplyIntentToReceiver(false);
                processed = true;
            } else {
                Log.e(TAG, "Bad messagePermission: " + messagePermission);
            }
        }
        if (DEBUG) Log.d(TAG,"checkUserChoice(): returning " + processed);
        return processed;
    }

    private void sendReplyIntentToReceiver(final boolean allowed) {
        Intent intent = new Intent(BluetoothDevice.ACTION_CONNECTION_ACCESS_REPLY);

        if (mReturnPackage != null && mReturnClass != null) {
            intent.setClassName(mReturnPackage, mReturnClass);
        }

        intent.putExtra(BluetoothDevice.EXTRA_CONNECTION_ACCESS_RESULT,
                        allowed ? BluetoothDevice.CONNECTION_ACCESS_YES
                                : BluetoothDevice.CONNECTION_ACCESS_NO);
        intent.putExtra(BluetoothDevice.EXTRA_DEVICE, mDevice);
        intent.putExtra(BluetoothDevice.EXTRA_ACCESS_REQUEST_TYPE, mRequestType);
        mContext.sendBroadcast(intent, android.Manifest.permission.BLUETOOTH_ADMIN);
    }
}
