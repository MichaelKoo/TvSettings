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

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothUuid;
import android.os.ParcelUuid;
import android.util.Log;

/**
 * BluetoothDeviceFilter contains a static method that returns a
 * Filter object that returns whether or not the BluetoothDevice
 * passed to it matches the specified filter type constant from
 * {@link android.bluetooth.BluetoothDevicePicker}.
 */
final class BluetoothDeviceFilter {
    private static final String TAG = "BluetoothDeviceFilter";

    /** The filter interface to external classes. */
    interface Filter {
        boolean matches(BluetoothDevice device);
    }

    /** All filter singleton (referenced directly). */
    static final Filter ALL_FILTER = new AllFilter();

    /** Bonded devices only filter (referenced directly). */
    static final Filter BONDED_DEVICE_FILTER = new BondedDeviceFilter();

    /** Unbonded devices only filter (referenced directly). */
    static final Filter UNBONDED_DEVICE_FILTER = new UnbondedDeviceFilter();

    // broadcom setting patch
    /** Table of singleton filter objects. */
    private static final Filter[] FILTERS = {
            ALL_FILTER,             // FILTER_TYPE_ALL
            new AudioFilter(),      // FILTER_TYPE_AUDIO
            new TransferFilter(),   // FILTER_TYPE_TRANSFER
            new PanuFilter(),       // FILTER_TYPE_PANU
            new NapFilter(),        // FILTER_TYPE_NAP
            new HidFilter(),        // FILTER_TYPE_HID
            new HidRCFilter()       // FILTER_TYPE_HID_RC
    };

    /** Private constructor. */
    private BluetoothDeviceFilter() {
    }

    /**
     * Returns the singleton {@link Filter} object for the specified type,
     * or {@link #ALL_FILTER} if the type value is out of range.
     *
     * @param filterType a constant from BluetoothDevicePicker
     * @return a singleton object implementing the {@link Filter} interface.
     */
    static Filter getFilter(int filterType) {
        if (filterType >= 0 && filterType < FILTERS.length) {
            return FILTERS[filterType];
        } else {
            Log.w(TAG, "Invalid filter type " + filterType + " for device picker");
            return ALL_FILTER;
        }
    }

    /** Filter that matches all devices. */
    private static final class AllFilter implements Filter {
        public boolean matches(BluetoothDevice device) {
            return true;
        }
    }

    /** Filter that matches only bonded devices. */
    private static final class BondedDeviceFilter implements Filter {
        public boolean matches(BluetoothDevice device) {
            return device.getBondState() == BluetoothDevice.BOND_BONDED;
        }
    }

    /** Filter that matches only unbonded devices. */
    private static final class UnbondedDeviceFilter implements Filter {
        public boolean matches(BluetoothDevice device) {
            return device.getBondState() != BluetoothDevice.BOND_BONDED;
        }
    }

    /** Parent class of filters based on UUID and/or Bluetooth class. */
    private abstract static class ClassUuidFilter implements Filter {
        abstract boolean matches(ParcelUuid[] uuids, BluetoothClass btClass);

        public boolean matches(BluetoothDevice device) {
            return matches(device.getUuids(), device.getBluetoothClass());
        }
    }

    /** Filter that matches devices that support AUDIO profiles. */
    private static final class AudioFilter extends ClassUuidFilter {
        @Override
        boolean matches(ParcelUuid[] uuids, BluetoothClass btClass) {
            if (uuids != null) {
                if (BluetoothUuid.containsAnyUuid(uuids, A2dpProfile.SINK_UUIDS)) {
                    return true;
                }
                // proting from brcm
                if (BluetoothUuid.containsAnyUuid(uuids, A2dpProfile.SOURCE_UUIDS)) {
                    return true;
                }
                if (BluetoothUuid.containsAnyUuid(uuids, HeadsetProfile.UUIDS)) {
                    return true;
                }
            } else if (btClass != null) {
                if (btClass.doesClassMatch(BluetoothClass.PROFILE_A2DP) ||
                        btClass.doesClassMatch(BluetoothClass.PROFILE_HEADSET)) {
                    return true;
                }
            }
            return false;
        }
    }

    /** Filter that matches devices that support Object Transfer. */
    private static final class TransferFilter extends ClassUuidFilter {
        @Override
        boolean matches(ParcelUuid[] uuids, BluetoothClass btClass) {
            if (uuids != null) {
                if (BluetoothUuid.isUuidPresent(uuids, BluetoothUuid.ObexObjectPush)) {
                    return true;
                }
            }
            return btClass != null
                    && btClass.doesClassMatch(BluetoothClass.PROFILE_OPP);
        }
    }

    /** Filter that matches devices that support PAN User (PANU) profile. */
    private static final class PanuFilter extends ClassUuidFilter {
        @Override
        boolean matches(ParcelUuid[] uuids, BluetoothClass btClass) {
            if (uuids != null) {
                if (BluetoothUuid.isUuidPresent(uuids, BluetoothUuid.PANU)) {
                    return true;
                }
            }
            return btClass != null
                    && btClass.doesClassMatch(BluetoothClass.PROFILE_PANU);
        }
    }

    /** Filter that matches devices that support NAP profile. */
    private static final class NapFilter extends ClassUuidFilter {
        @Override
        boolean matches(ParcelUuid[] uuids, BluetoothClass btClass) {
            if (uuids != null) {
                if (BluetoothUuid.isUuidPresent(uuids, BluetoothUuid.NAP)) {
                    return true;
                }
            }
            return btClass != null
                    && btClass.doesClassMatch(BluetoothClass.PROFILE_NAP);
        }
    }

    // broadcom setting patch
    /** Filter that matches devices that support Human Interface Device (HID) profile. */
    private static final class HidFilter extends ClassUuidFilter {
        @Override
        boolean matches(ParcelUuid[] uuids, BluetoothClass btClass) {
            if (uuids != null) {
                if (BluetoothUuid.isUuidPresent(uuids, BluetoothUuid.Hid)) {
                    return true;
                }
            }
            return btClass != null
                    && btClass.doesClassMatch(BluetoothClass.PROFILE_HID);
        }
    }

    /** Filter that matches devices that support Human Interface Device (HID) profile
    * with minor class aas RC*/
    private static final class HidRCFilter extends ClassUuidFilter {
        @Override
        boolean matches(ParcelUuid[] uuids, BluetoothClass btClass) {
            if (uuids != null) {
                if (BluetoothUuid.isUuidPresent(uuids, BluetoothUuid.Hid)) {
                    return true;
                }
            }
            return btClass != null
                    && btClass.doesClassMatch(BluetoothClass.PROFILE_HID)
                    && ((btClass.getDeviceClass() &
                    BluetoothClass.Device.PERIPHERAL_REMOTE_CONTROL) ==
                    BluetoothClass.Device.PERIPHERAL_REMOTE_CONTROL);
        }
    }
}
