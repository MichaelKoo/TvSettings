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

import static android.os.UserManager.DISALLOW_CONFIG_BLUETOOTH;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothMonitor;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.android.tv.settings.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * BluetoothSettings is the Settings screen for Bluetooth configuration and
 * connection management.
 */
public final class BluetoothSettings extends DeviceListPreferenceFragment {
    private static final String TAG = "BluetoothSettings";

    private static final int MENU_ID_SCAN = Menu.FIRST;
    private static final int MENU_ID_RENAME_DEVICE = Menu.FIRST + 1;
    private static final int MENU_ID_SHOW_RECEIVED = Menu.FIRST + 2;

    /* Private intent to show the list of received files */
    private static final String BTOPP_ACTION_OPEN_RECEIVED_FILES =
            "android.btopp.intent.action.OPEN_RECEIVED_FILES";

    private static View mSettingsDialogView = null;

    private PreferenceGroup mPairedDevicesCategory;
    private PreferenceGroup mAvailableDevicesCategory;
    private boolean mAvailableDevicesCategoryIsPresent;

    private boolean mInitialScanStarted;
    private boolean mInitiateDiscoverable;

    private TextView mEmptyView;

    private final IntentFilter mIntentFilter;


    // accessed from inner class (not private to avoid thunks)
    Preference mMyDevicePreference;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            final int state =
                intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

            if (action.equals(BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED)) {
                updateDeviceName(context);
            }

            if (state == BluetoothAdapter.STATE_ON) {
                mInitiateDiscoverable = true;
            }
        }

        private void updateDeviceName(Context context) {
            if (mLocalAdapter.isEnabled() && mMyDevicePreference != null) {
                mMyDevicePreference.setSummary(context.getResources().getString(
                            R.string.bluetooth_is_visible_message, mLocalAdapter.getName()));
            }
        }
    };

    public BluetoothSettings() {
        super(DISALLOW_CONFIG_BLUETOOTH);
        mIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter!=null && !btAdapter.isEnabled()) {
            btAdapter.enable();
        }
        mInitialScanStarted = (savedInstanceState != null);    // don't auto start scan after rotation
        mInitiateDiscoverable = true;

        mEmptyView = (TextView) getView().findViewById(android.R.id.empty);
        getListView().setEmptyView(mEmptyView);
        getActivity().getActionBar().setTitle(R.string.bluetooth_settings_title);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    void addPreferencesForActivity() {
        addPreferencesFromResource(R.xml.bluetooth_settings);

        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();

        mInitiateDiscoverable = true;

        if (isUiRestricted()) {
            setDeviceListGroup(getPreferenceScreen());
            removeAllDevices();
            mEmptyView.setText(R.string.bluetooth_empty_list_user_restricted);
            return;
        }

        getActivity().registerReceiver(mReceiver, mIntentFilter);
        if (mLocalAdapter != null) {
            updateContent(mLocalAdapter.getBluetoothState());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Make the device only visible to connected devices.
        mLocalAdapter.setScanMode(BluetoothAdapter.SCAN_MODE_CONNECTABLE);

        if (isUiRestricted()) {
            return;
        }

        getActivity().unregisterReceiver(mReceiver);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mLocalAdapter == null) return;
        // If the user is not allowed to configure bluetooth, do not show the menu.
        if (isUiRestricted()) return;

        boolean bluetoothIsEnabled = mLocalAdapter.getBluetoothState() == BluetoothAdapter.STATE_ON;
        boolean isDiscovering = mLocalAdapter.isDiscovering();
        int textId = isDiscovering ? R.string.bluetooth_searching_for_devices :
            R.string.bluetooth_search_for_devices;
        menu.add(Menu.NONE, MENU_ID_SCAN, 0, textId)
                .setEnabled(bluetoothIsEnabled && !isDiscovering)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menu.add(Menu.NONE, MENU_ID_RENAME_DEVICE, 0, R.string.bluetooth_rename_device)
                .setEnabled(bluetoothIsEnabled)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menu.add(Menu.NONE, MENU_ID_SHOW_RECEIVED, 0, R.string.bluetooth_show_received_files)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        // porting from brcm
        if (BluetoothMonitor.isBroadcomBt()) {
            com.broadcom.bt.app.settings.AdvancedSettings.addAdvancedSettingsMenu(getActivity(),menu,bluetoothIsEnabled);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ID_SCAN:
                if (mLocalAdapter.getBluetoothState() == BluetoothAdapter.STATE_ON) {
                    startScanning();
                }
                return true;

            case MENU_ID_RENAME_DEVICE:
                new BluetoothNameDialogFragment().show(
                        getFragmentManager(), "rename device");
                return true;

            case MENU_ID_SHOW_RECEIVED:
                Intent intent = new Intent(BTOPP_ACTION_OPEN_RECEIVED_FILES);
                getActivity().sendBroadcast(intent);
                return true;
        }
        // porting from brcm
        if (com.broadcom.bt.app.settings.AdvancedSettings.handleOptionsItemSelected(getActivity(), item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startScanning() {
        if (isUiRestricted()) {
            return;
        }

        if (!mAvailableDevicesCategoryIsPresent) {
            getPreferenceScreen().addPreference(mAvailableDevicesCategory);
            mAvailableDevicesCategoryIsPresent = true;
        }

        if (mAvailableDevicesCategory != null) {
            setDeviceListGroup(mAvailableDevicesCategory);
            removeAllDevices();
        }

        mLocalManager.getCachedDeviceManager().clearNonBondedDevices();
        mAvailableDevicesCategory.removeAll();
        mInitialScanStarted = true;
        mLocalAdapter.startScanning(true);
    }

    @Override
    void onDevicePreferenceClick(BluetoothDevicePreference btPreference) {
        mLocalAdapter.stopScanning();
        super.onDevicePreferenceClick(btPreference);
    }

    private void addDeviceCategory(PreferenceGroup preferenceGroup, int titleId,
            BluetoothDeviceFilter.Filter filter, boolean addCachedDevices) {
        preferenceGroup.setTitle(titleId);
        getPreferenceScreen().addPreference(preferenceGroup);
        setFilter(filter);
        setDeviceListGroup(preferenceGroup);
        if (addCachedDevices) {
            addCachedDevices();
        }
        preferenceGroup.setEnabled(true);
    }

    private void updateContent(int bluetoothState) {
        final PreferenceScreen preferenceScreen = getPreferenceScreen();
        int messageId = 0;

        switch (bluetoothState) {
            case BluetoothAdapter.STATE_ON:
                preferenceScreen.removeAll();
                preferenceScreen.setOrderingAsAdded(true);
                mDevicePreferenceMap.clear();

                if (isUiRestricted()) {
                    messageId = R.string.bluetooth_empty_list_user_restricted;
                    break;
                }

                // Paired devices category
                if (mPairedDevicesCategory == null) {
                    mPairedDevicesCategory = new PreferenceCategory(getActivity());
                } else {
                    mPairedDevicesCategory.removeAll();
                }
                addDeviceCategory(mPairedDevicesCategory,
                        R.string.bluetooth_preference_paired_devices,
                        BluetoothDeviceFilter.BONDED_DEVICE_FILTER, true);
                int numberOfPairedDevices = mPairedDevicesCategory.getPreferenceCount();

                if (isUiRestricted() || numberOfPairedDevices <= 0) {
                    preferenceScreen.removePreference(mPairedDevicesCategory);
                }

                // Available devices category
                if (mAvailableDevicesCategory == null) {
                    mAvailableDevicesCategory = new BluetoothProgressCategory(getActivity());
                    mAvailableDevicesCategory.setSelectable(false);
                } else {
                    mAvailableDevicesCategory.removeAll();
                }
                addDeviceCategory(mAvailableDevicesCategory,
                        R.string.bluetooth_preference_found_devices,
                        BluetoothDeviceFilter.UNBONDED_DEVICE_FILTER, mInitialScanStarted);
                int numberOfAvailableDevices = mAvailableDevicesCategory.getPreferenceCount();

                if (!mInitialScanStarted) {
                    startScanning();
                }

                if (mMyDevicePreference == null) {
                    mMyDevicePreference = new Preference(getActivity());
                }

                mMyDevicePreference.setSummary(getResources().getString(
                            R.string.bluetooth_is_visible_message, mLocalAdapter.getName()));
                mMyDevicePreference.setSelectable(false);
                preferenceScreen.addPreference(mMyDevicePreference);

                getActivity().invalidateOptionsMenu();

                // mLocalAdapter.setScanMode is internally synchronized so it is okay for multiple
                // threads to execute.
                if (mInitiateDiscoverable) {
                    // Make the device visible to other devices.
                    mLocalAdapter.setScanMode(BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE);
                    mInitiateDiscoverable = false;
                }
                return; // not break

            case BluetoothAdapter.STATE_TURNING_OFF:
                messageId = R.string.bluetooth_turning_off;
                break;

            case BluetoothAdapter.STATE_OFF:
                messageId = R.string.bluetooth_empty_list_bluetooth_off;
                if (isUiRestricted()) {
                    messageId = R.string.bluetooth_empty_list_user_restricted;
                }
                break;

            case BluetoothAdapter.STATE_TURNING_ON:
                messageId = R.string.bluetooth_turning_on;
                mInitialScanStarted = false;
                break;
        }

        setDeviceListGroup(preferenceScreen);
        removeAllDevices();
        mEmptyView.setText(messageId);
        if (!isUiRestricted()) {
            getActivity().invalidateOptionsMenu();
        }
    }

    @Override
    public void onBluetoothStateChanged(int bluetoothState) {
        super.onBluetoothStateChanged(bluetoothState);
        updateContent(bluetoothState);
    }

    @Override
    public void onScanningStateChanged(boolean started) {
        super.onScanningStateChanged(started);
        // Update options' enabled state
        if (getActivity() != null) {
            getActivity().invalidateOptionsMenu();
        }
    }

    public void onDeviceBondStateChanged(CachedBluetoothDevice cachedDevice, int bondState) {
        setDeviceListGroup(getPreferenceScreen());
        removeAllDevices();
        updateContent(mLocalAdapter.getBluetoothState());
    }

    private final View.OnClickListener mDeviceProfilesListener = new View.OnClickListener() {
        public void onClick(View v) {
            // User clicked on advanced options icon for a device in the list
            if (!(v.getTag() instanceof CachedBluetoothDevice)) {
                Log.w(TAG, "onClick() called for other View: " + v);
                return;
            }

            final CachedBluetoothDevice device = (CachedBluetoothDevice) v.getTag();
            final Activity activity = getActivity();
            DeviceProfilesSettings profileFragment = (DeviceProfilesSettings)activity.
                getFragmentManager().findFragmentById(R.id.bluetooth_fragment_settings);

            if (mSettingsDialogView != null){
                ViewGroup parent = (ViewGroup) mSettingsDialogView.getParent();
                if (parent != null) {
                    parent.removeView(mSettingsDialogView);
                }
            }

            if (profileFragment == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                mSettingsDialogView = inflater.inflate(R.layout.bluetooth_device_settings, null);
                profileFragment = (DeviceProfilesSettings)activity.getFragmentManager()
                    .findFragmentById(R.id.bluetooth_fragment_settings);

                // To enable scrolling we store the name field in a seperate header and add to
                // the ListView of the profileFragment.
                View header = inflater.inflate(R.layout.bluetooth_device_settings_header, null);
                profileFragment.getListView().addHeaderView(header);
            }

            final View dialogLayout = mSettingsDialogView;
            AlertDialog.Builder settingsDialog = new AlertDialog.Builder(activity);
            profileFragment.setDevice(device);
            final EditText deviceName = (EditText)dialogLayout.findViewById(R.id.name);
            deviceName.setText(device.getName(), TextView.BufferType.EDITABLE);

            final DeviceProfilesSettings dpsFragment = profileFragment;
            final Context context = v.getContext();
            settingsDialog.setView(dialogLayout);
            settingsDialog.setTitle(R.string.bluetooth_preference_paired_devices);
            settingsDialog.setPositiveButton(R.string.okay,
                    new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    EditText deviceName = (EditText)dialogLayout.findViewById(R.id.name);
                    device.setName(deviceName.getText().toString());
                }
            });

            settingsDialog.setNegativeButton(R.string.forget,
                    new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    device.unpair();
                }
            });

            // We must ensure that the fragment gets destroyed to avoid duplicate fragments.
            settingsDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                public void onDismiss(final DialogInterface dialog) {
                    if (!activity.isDestroyed()) {
                        activity.getFragmentManager().beginTransaction().remove(dpsFragment)
                            .commitAllowingStateLoss();
                    }
                }
            });

            AlertDialog dialog = settingsDialog.create();
            dialog.create();
            dialog.show();

            // We must ensure that clicking on the EditText will bring up the keyboard.
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        }
    };

    /**
     * Add a listener, which enables the advanced settings icon.
     * @param preference the newly added preference
     */
    @Override
    void initDevicePreference(BluetoothDevicePreference preference) {
        CachedBluetoothDevice cachedDevice = preference.getCachedDevice();
        if (cachedDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
            // Only paired device have an associated advanced settings screen
            preference.setOnSettingsClickListener(mDeviceProfilesListener);
        }
    }
}
