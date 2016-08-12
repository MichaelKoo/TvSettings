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

package com.android.tv.settings;

import java.util.HashSet;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.RestrictionsManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.UserManager;
import android.preference.CheckBoxPreference;
import android.preference.Preference;

/**
 * Base class for settings screens that should be pin protected when in restricted mode.
 * The constructor for this class will take the restriction key that this screen should be
 * locked by.  If {@link RestrictionsManager.hasRestrictionsProvider()} and
 * {@link UserManager.hasUserRestriction()}, then the user will have to enter the restrictions
 * pin before seeing the Settings screen.
 *
 * If this settings screen should be pin protected whenever
 * {@link RestrictionsManager.hasRestrictionsProvider()} returns true, pass in
 * {@link RESTRICT_IF_OVERRIDABLE} to the constructor instead of a restrictions key.
 */
public class RestrictedSettingsFragment extends SettingsPreferenceFragment {

    protected static final String RESTRICT_IF_OVERRIDABLE = "restrict_if_overridable";

    // No RestrictedSettingsFragment screens should use this number in startActivityForResult.
    private static final int REQUEST_PIN_CHALLENGE = 12309;

    private static final String KEY_CHALLENGE_SUCCEEDED = "chsc";
    private static final String KEY_CHALLENGE_REQUESTED = "chrq";

    // If the restriction PIN is entered correctly.
    private boolean mChallengeSucceeded;
    private boolean mChallengeRequested;

    private UserManager mUserManager;
    private RestrictionsManager mRestrictionsManager;

    private final String mRestrictionKey;

    // Receiver to clear pin status when the screen is turned off.
    private BroadcastReceiver mScreenOffReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!mChallengeRequested) {
                mChallengeSucceeded = false;
                mChallengeRequested = false;
            }
        }
    };

    /**
     * @param restrictionKey The restriction key to check before pin protecting
     *            this settings page. Pass in {@link RESTRICT_IF_OVERRIDABLE} if it should
     *            be protected whenever a restrictions provider is set. Pass in
     *            null if it should never be protected.
     */
    public RestrictedSettingsFragment(String restrictionKey) {
        mRestrictionKey = restrictionKey;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        mRestrictionsManager = (RestrictionsManager) getSystemService(Context.RESTRICTIONS_SERVICE);
        mUserManager = (UserManager) getSystemService(Context.USER_SERVICE);

        if (icicle != null) {
            mChallengeSucceeded = icicle.getBoolean(KEY_CHALLENGE_SUCCEEDED, false);
            mChallengeRequested = icicle.getBoolean(KEY_CHALLENGE_REQUESTED, false);
        }

        IntentFilter offFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        offFilter.addAction(Intent.ACTION_USER_PRESENT);
        getActivity().registerReceiver(mScreenOffReceiver, offFilter);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (getActivity().isChangingConfigurations()) {
            outState.putBoolean(KEY_CHALLENGE_REQUESTED, mChallengeRequested);
            outState.putBoolean(KEY_CHALLENGE_SUCCEEDED, mChallengeSucceeded);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (shouldBeProviderProtected(mRestrictionKey)) {
            ensurePin();
        }
    }

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(mScreenOffReceiver);
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PIN_CHALLENGE) {
            if (resultCode == Activity.RESULT_OK) {
                mChallengeSucceeded = true;
                mChallengeRequested = false;
            } else {
                mChallengeSucceeded = false;
            }
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void ensurePin() {
        if (!mChallengeSucceeded && !mChallengeRequested
                && mRestrictionsManager.hasRestrictionsProvider()) {
            Intent intent = mRestrictionsManager.createLocalApprovalIntent();
            if (intent != null) {
                mChallengeRequested = true;
                mChallengeSucceeded = false;
                PersistableBundle request = new PersistableBundle();
                request.putString(RestrictionsManager.REQUEST_KEY_MESSAGE,
                        getResources().getString(R.string.restr_pin_enter_admin_pin));
                intent.putExtra(RestrictionsManager.EXTRA_REQUEST_BUNDLE, request);
                startActivityForResult(intent, REQUEST_PIN_CHALLENGE);
            }
        }
    }

    /**
     * Returns true if this activity is restricted, but no restrictions provider has been set.
     * Used to determine if the settings UI should disable UI.
     */
    protected boolean isRestrictedAndNotProviderProtected() {
        if (mRestrictionKey == null || RESTRICT_IF_OVERRIDABLE.equals(mRestrictionKey)) {
            return false;
        }
        return mUserManager.hasUserRestriction(mRestrictionKey)
                && !mRestrictionsManager.hasRestrictionsProvider();
    }

    protected boolean hasChallengeSucceeded() {
        return (mChallengeRequested && mChallengeSucceeded) || !mChallengeRequested;
    }

    /**
     * Returns true if this restrictions key is locked down.
     */
    protected boolean shouldBeProviderProtected(String restrictionKey) {
        if (restrictionKey == null) {
            return false;
        }
        boolean restricted = RESTRICT_IF_OVERRIDABLE.equals(restrictionKey)
                || mUserManager.hasUserRestriction(mRestrictionKey);
        return restricted && mRestrictionsManager.hasRestrictionsProvider();
    }

    /**
     * Returns whether restricted or actionable UI elements should be removed or disabled.
     */
    protected boolean isUiRestricted() {
        return isRestrictedAndNotProviderProtected() || !hasChallengeSucceeded();
    }
}
