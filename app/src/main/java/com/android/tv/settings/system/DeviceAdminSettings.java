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

package com.android.tv.settings.system;

import com.android.tv.settings.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.app.admin.DeviceAdminInfo;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Collection;

public class DeviceAdminSettings extends ListFragment {
    static final String TAG = "DeviceAdminSettings";

    static final int DIALOG_WARNING = 1;
    private DevicePolicyManager mDPM;
    private UserManager mUm;

    /**
     * Internal collection of device admin info objects for all profiles associated with the current
     * user.
     */
    private final SparseArray<ArrayList<DeviceAdminInfo>>
            mAdminsByProfile = new SparseArray<ArrayList<DeviceAdminInfo>>();

    private String mDeviceOwnerPkg;
    private SparseArray<ComponentName> mProfileOwnerComponents = new SparseArray<ComponentName>();

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mDPM = (DevicePolicyManager) getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);
        mUm = (UserManager) getActivity().getSystemService(Context.USER_SERVICE);
        return inflater.inflate(R.layout.device_admin_settings, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        forceCustomPadding(getListView(), true /* additive padding */);
    }

    @Override
    public void onResume() {
        super.onResume();
        mDeviceOwnerPkg = mDPM.getDeviceOwner();
        if (mDeviceOwnerPkg != null && !mDPM.isDeviceOwner(mDeviceOwnerPkg)) {
            mDeviceOwnerPkg = null;
        }
        mProfileOwnerComponents.clear();
        final List<UserHandle> profiles = mUm.getUserProfiles();
        final int profilesSize = profiles.size();
        for (int i = 0; i < profilesSize; ++i) {
            final int profileId = profiles.get(i).getIdentifier();
            mProfileOwnerComponents.put(profileId, mDPM.getProfileOwnerAsUser(profileId));
        }
        updateList();
    }

    public void forceCustomPadding(View view, boolean additive) {
        final Resources res = view.getResources();
        final int paddingSide = res.getDimensionPixelSize(R.dimen.settings_side_margin);

        final int paddingStart = paddingSide + (additive ? view.getPaddingStart() : 0);
        final int paddingEnd = paddingSide + (additive ? view.getPaddingEnd() : 0);
        final int paddingBottom = res.getDimensionPixelSize(
                com.android.internal.R.dimen.preference_fragment_padding_bottom);

        view.setPaddingRelative(paddingStart, 0, paddingEnd, paddingBottom);
    }

    /**
     * Update the internal collection of available admins for all profiles associated with the
     * current user.
     */
    void updateList() {
        mAdminsByProfile.clear();

        final List<UserHandle> profiles = mUm.getUserProfiles();
        final int profilesSize = profiles.size();
        for (int i = 0; i < profilesSize; ++i) {
            final int profileId = profiles.get(i).getIdentifier();
            updateAvailableAdminsForProfile(profileId);
        }

        getListView().setAdapter(new PolicyListAdapter());
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Object o = l.getAdapter().getItem(position);
        if (!(o instanceof DeviceAdminInfo)) {
            // race conditions may cause this
            return;
        }
        DeviceAdminInfo dpi = (DeviceAdminInfo) o;
        final Activity activity = getActivity();
        final int userId = getUserId(dpi);
        if (userId == UserHandle.myUserId() || !isProfileOwner(dpi)) {
            Intent intent = new Intent();
            intent.setClass(activity, DeviceAdminAdd.class);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, dpi.getComponent());
            activity.startActivityAsUser(intent, new UserHandle(userId));
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setMessage(getString(R.string.managed_profile_device_admin_info,
                    dpi.loadLabel(activity.getPackageManager())));
            builder.setPositiveButton(android.R.string.ok, null);
            builder.create().show();
        }
    }

    static class ViewHolder {
        ImageView icon;
        TextView name;
        CheckBox checkbox;
        TextView description;
    }

    class PolicyListAdapter extends BaseAdapter {
        final LayoutInflater mInflater;

        PolicyListAdapter() {
            mInflater = (LayoutInflater)
                    getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public int getCount() {
            int adminCount = 0;
            final int profileCount = mAdminsByProfile.size();
            for (int i = 0; i < profileCount; ++i) {
                adminCount += mAdminsByProfile.valueAt(i).size();
            }
            // Add 'profileCount' for title items.
            return adminCount + profileCount;
        }

        /**
         * The item for the given position in the list.
         *
         * @return a String object for title items and a DeviceAdminInfo object for actual device
         *         admins.
         */
        @Override
        public Object getItem(int position) {
            if (position < 0) {
                throw new ArrayIndexOutOfBoundsException();
            }
            // The position of the item in the list of admins.
            // We start from the given position and discount the length of the upper lists until we
            // get the one for the right profile
            int adminPosition = position;
            final int n = mAdminsByProfile.size();
            int i = 0;
            for (; i < n; ++i) {
                // The elements in that section including the title item (that's why adding one).
                final int listSize = mAdminsByProfile.valueAt(i).size() + 1;
                if (adminPosition < listSize) {
                    break;
                }
                adminPosition -= listSize;
            }
            if (i == n) {
                throw new ArrayIndexOutOfBoundsException();
            }
            // If countdown == 0 that means the title item
            if (adminPosition == 0) {
                Resources res = getActivity().getResources();
                if (mAdminsByProfile.keyAt(i) == UserHandle.myUserId()) {
                    return res.getString(R.string.personal_device_admin_title);
                } else {
                    return res.getString(R.string.managed_device_admin_title);
                }
            } else {
                // Subtracting one for the title.
                return mAdminsByProfile.valueAt(i).get(adminPosition - 1);
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        /**
         * See {@link #getItemViewType} for the view types.
         */
        @Override
        public int getViewTypeCount() {
            return 2;
        }

        /**
         * Returns 1 for title items and 0 for anything else.
         */
        @Override
        public int getItemViewType(int position) {
            Object o = getItem(position);
            return (o instanceof String) ? 1 : 0;
        }

        @Override
        public boolean isEnabled(int position) {
            Object o = getItem(position);
            return isEnabled(o);
        }

        private boolean isEnabled(Object o) {
            if (!(o instanceof DeviceAdminInfo)) {
                // Title item
                return false;
            }
            DeviceAdminInfo info = (DeviceAdminInfo) o;
            if (isActiveAdmin(info) && getUserId(info) == UserHandle.myUserId()
                    && (isDeviceOwner(info) || isProfileOwner(info))) {
                return false;
            }
            return true;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Object o = getItem(position);
            if (o instanceof DeviceAdminInfo) {
                if (convertView == null) {
                    convertView = newDeviceAdminView(parent);
                }
                bindView(convertView, (DeviceAdminInfo) o);
            } else {
                if (convertView == null) {
                    convertView = newTitleView(parent);
                }
                final TextView title = (TextView) convertView.findViewById(android.R.id.title);
                title.setText((String)o);
            }
            return convertView;
        }

        private View newDeviceAdminView(ViewGroup parent) {
            View v = mInflater.inflate(R.layout.device_admin_item, parent, false);
            ViewHolder h = new ViewHolder();
            h.icon = (ImageView)v.findViewById(R.id.icon);
            h.name = (TextView)v.findViewById(R.id.name);
            h.checkbox = (CheckBox)v.findViewById(R.id.checkbox);
            h.description = (TextView)v.findViewById(R.id.description);
            v.setTag(h);
            return v;
        }

        private View newTitleView(ViewGroup parent) {
            final TypedArray a = mInflater.getContext().obtainStyledAttributes(null,
                    com.android.internal.R.styleable.Preference,
                    com.android.internal.R.attr.preferenceCategoryStyle, 0);
            final int resId = a.getResourceId(com.android.internal.R.styleable.Preference_layout,
                    0);
            return mInflater.inflate(resId, parent, false);
        }

        private void bindView(View view, DeviceAdminInfo item) {
            final Activity activity = getActivity();
            ViewHolder vh = (ViewHolder) view.getTag();
            Drawable activityIcon = item.loadIcon(activity.getPackageManager());
            Drawable badgedIcon = activity.getPackageManager().getUserBadgedIcon(
                    activityIcon, new UserHandle(getUserId(item)));
            vh.icon.setImageDrawable(badgedIcon);
            vh.name.setText(item.loadLabel(activity.getPackageManager()));
            vh.checkbox.setChecked(isActiveAdmin(item));
            final boolean enabled = isEnabled(item);
            try {
                vh.description.setText(item.loadDescription(activity.getPackageManager()));
            } catch (Resources.NotFoundException e) {
            }
            vh.checkbox.setEnabled(enabled);
            vh.name.setEnabled(enabled);
            vh.description.setEnabled(enabled);
            vh.icon.setEnabled(enabled);
        }
    }

    private boolean isDeviceOwner(DeviceAdminInfo item) {
        return getUserId(item) == UserHandle.myUserId()
                && item.getPackageName().equals(mDeviceOwnerPkg);
    }

    private boolean isProfileOwner(DeviceAdminInfo item) {
        ComponentName profileOwner = mProfileOwnerComponents.get(getUserId(item));
        return item.getComponent().equals(profileOwner);
    }

    private boolean isActiveAdmin(DeviceAdminInfo item) {
        return mDPM.isAdminActiveAsUser(item.getComponent(), getUserId(item));
    }

    /**
     * Add device admins to the internal collection that belong to a profile.
     *
     * @param profileId the profile identifier.
     */
    private void updateAvailableAdminsForProfile(final int profileId) {
        // We are adding the union of two sets 'A' and 'B' of device admins to mAvailableAdmins.
        // Set 'A' is the set of active admins for the profile whereas set 'B' is the set of
        // listeners to DeviceAdminReceiver.ACTION_DEVICE_ADMIN_ENABLED for the profile.

        // Add all of set 'A' to mAvailableAdmins.
        List<ComponentName> activeAdminsListForProfile = mDPM.getActiveAdminsAsUser(profileId);
        addActiveAdminsForProfile(activeAdminsListForProfile, profileId);

        // Collect set 'B' and add B-A to mAvailableAdmins.
        addDeviceAdminBroadcastReceiversForProfile(activeAdminsListForProfile, profileId);
    }

    /**
     * Add a profile's device admins that are receivers of
     * {@code DeviceAdminReceiver.ACTION_DEVICE_ADMIN_ENABLED} to the internal collection if they
     * haven't been added yet.
     *
     * @param alreadyAddedComponents the set of active admin component names. Receivers of
     *            {@code DeviceAdminReceiver.ACTION_DEVICE_ADMIN_ENABLED} whose component is in this
     *            set are not added to the internal collection again.
     * @param profileId the identifier of the profile
     */
    private void addDeviceAdminBroadcastReceiversForProfile(
            Collection<ComponentName> alreadyAddedComponents, final int profileId) {
        final PackageManager pm = getActivity().getPackageManager();
        List<ResolveInfo> enabledForProfile = pm.queryBroadcastReceivers(
                new Intent(DeviceAdminReceiver.ACTION_DEVICE_ADMIN_ENABLED),
                PackageManager.GET_META_DATA | PackageManager.GET_DISABLED_UNTIL_USED_COMPONENTS,
                profileId);
        if (enabledForProfile == null) {
            enabledForProfile = Collections.emptyList();
        }
        final int n = enabledForProfile.size();
        ArrayList<DeviceAdminInfo> deviceAdmins = mAdminsByProfile.get(profileId);
        if (deviceAdmins == null) {
            deviceAdmins = new ArrayList<DeviceAdminInfo>(n);
        }
        for (int i = 0; i < n; ++i) {
            ResolveInfo resolveInfo = enabledForProfile.get(i);
            ComponentName riComponentName =
                    new ComponentName(resolveInfo.activityInfo.packageName,
                            resolveInfo.activityInfo.name);
            if (alreadyAddedComponents == null
                    || !alreadyAddedComponents.contains(riComponentName)) {
                DeviceAdminInfo deviceAdminInfo = createDeviceAdminInfo(resolveInfo);
                // add only visible ones (note: active admins are added regardless of visibility)
                if (deviceAdminInfo != null && deviceAdminInfo.isVisible()) {
                    deviceAdmins.add(deviceAdminInfo);
                }
            }
        }
        if (!deviceAdmins.isEmpty()) {
            mAdminsByProfile.put(profileId, deviceAdmins);
        }
    }

    /**
     * Add a {@link DeviceAdminInfo} object to the internal collection of available admins for all
     * active admin components associated with a profile.
     *
     * @param profileId a profile identifier.
     */
    private void addActiveAdminsForProfile(final List<ComponentName> activeAdmins,
            final int profileId) {
        if (activeAdmins != null) {
            final PackageManager packageManager = getActivity().getPackageManager();
            final int n = activeAdmins.size();
            ArrayList<DeviceAdminInfo> deviceAdmins = new ArrayList<DeviceAdminInfo>(n);
            for (int i = 0; i < n; ++i) {
                ComponentName activeAdmin = activeAdmins.get(i);
                List<ResolveInfo> resolved = packageManager.queryBroadcastReceivers(
                        new Intent().setComponent(activeAdmin), PackageManager.GET_META_DATA
                                | PackageManager.GET_DISABLED_UNTIL_USED_COMPONENTS, profileId);
                if (resolved != null) {
                    final int resolvedMax = resolved.size();
                    for (int j = 0; j < resolvedMax; ++j) {
                        DeviceAdminInfo deviceAdminInfo = createDeviceAdminInfo(resolved.get(j));
                        if (deviceAdminInfo != null) {
                            deviceAdmins.add(deviceAdminInfo);
                        }
                    }
                }
            }
            if (!deviceAdmins.isEmpty()) {
                mAdminsByProfile.put(profileId, deviceAdmins);
            }
        }
    }

    /**
     * Creates a device admin info object for the resolved intent that points to the component of
     * the device admin.
     *
     * @param resolved resolved intent.
     * @return new {@link DeviceAdminInfo} object or null if there was an error.
     */
    private DeviceAdminInfo createDeviceAdminInfo(ResolveInfo resolved) {
        try {
            return new DeviceAdminInfo(getActivity(), resolved);
        } catch (XmlPullParserException e) {
            Log.w(TAG, "Skipping " + resolved.activityInfo, e);
        } catch (IOException e) {
            Log.w(TAG, "Skipping " + resolved.activityInfo, e);
        }
        return null;
    }

    /**
     * Extracts the user id from a device admin info object.
     * @param adminInfo the device administrator info.
     * @return identifier of the user associated with the device admin.
     */
    private int getUserId(DeviceAdminInfo adminInfo) {
        return UserHandle.getUserId(adminInfo.getActivityInfo().applicationInfo.uid);
    }
}
