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

package com.android.tv.settings.inputmethod;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.android.tv.settings.R;
import com.android.tv.settings.inputmethod.UserDictionaryAddWordContents.LocaleRenderer;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Fragment to add a word/shortcut to the user dictionary.
 *
 * As opposed to the UserDictionaryActivity, this is only invoked within Settings
 * from the UserDictionarySettings.
 */
public class UserDictionaryAddWordFragment extends Fragment
        implements AdapterView.OnItemSelectedListener,
        com.android.internal.app.LocalePicker.LocaleSelectionListener {

    private static final int OPTIONS_MENU_DELETE = Menu.FIRST;

    private UserDictionaryAddWordContents mContents;
    private View mRootView;
    private boolean mIsDeleting = false;

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        getActivity().getActionBar().setTitle(R.string.user_dict_settings_title);
        // Keep the instance so that we remember mContents when configuration changes (eg rotation)
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedState) {
        mRootView = inflater.inflate(R.layout.user_dictionary_add_word_fullscreen, null);
        mIsDeleting = false;
        // If we have a non-null mContents object, it's the old value before a configuration
        // change (eg rotation) so we need to use its values. Otherwise, read from the arguments.
        if (null == mContents) {
            mContents = new UserDictionaryAddWordContents(mRootView, getArguments());
        } else {
            // We create a new mContents object to account for the new situation : a word has
            // been added to the user dictionary when we started rotating, and we are now editing
            // it. That means in particular if the word undergoes any change, the old version should
            // be updated, so the mContents object needs to switch to EDIT mode if it was in
            // INSERT mode.
            mContents = new UserDictionaryAddWordContents(mRootView,
                    mContents /* oldInstanceToBeEdited */);
        }
        getActivity().getActionBar().setSubtitle(UserDictionarySettingsUtils.getLocaleDisplayName(
                getActivity(), mContents.getCurrentUserDictionaryLocale()));
        return mRootView;
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        MenuItem actionItem = menu.add(0, OPTIONS_MENU_DELETE, 0, R.string.delete)
                .setIcon(android.R.drawable.ic_menu_delete);
        actionItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM |
                MenuItem.SHOW_AS_ACTION_WITH_TEXT);
    }

    /**
     * Callback for the framework when a menu option is pressed.
     *
     * This class only supports the delete menu item.
     * @param MenuItem the item that was pressed
     * @return false to allow normal menu processing to proceed, true to consume it here
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == OPTIONS_MENU_DELETE) {
            mContents.delete(getActivity());
            mIsDeleting = true;
            getActivity().onBackPressed();
            return true;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        // We are being shown: display the word
        updateSpinner();
    }

    private void updateSpinner() {
        final ArrayList<LocaleRenderer> localesList = mContents.getLocalesList(getActivity());

        final ArrayAdapter<LocaleRenderer> adapter = new ArrayAdapter<LocaleRenderer>(getActivity(),
                android.R.layout.simple_spinner_item, localesList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    @Override
    public void onPause() {
        super.onPause();
        // We are being hidden: commit changes to the user dictionary, unless we were deleting it
        if (!mIsDeleting) {
            mContents.apply(getActivity(), null);
        }
    }

    @Override
    public void onItemSelected(final AdapterView<?> parent, final View view, final int pos,
            final long id) {
        final LocaleRenderer locale = (LocaleRenderer)parent.getItemAtPosition(pos);
        if (locale.isMoreLanguages()) {
            startPreferenceFragment(new UserDictionaryLocalePicker(this), true);
        } else {
            mContents.updateLocale(locale.getLocaleString());
        }
    }

    /**
     * Start a new fragment.
     *
     * @param fragment The fragment to start
     * @param push If true, the current fragment will be pushed onto the back stack.  If false,
     * the current fragment will be replaced.
     */
    public void startPreferenceFragment(Fragment fragment, boolean push) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(android.R.id.content, fragment);
        if (push) {
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.addToBackStack(null);
        } else {
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        }
        transaction.commitAllowingStateLoss();
    }

    @Override
    public void onNothingSelected(final AdapterView<?> parent) {
        // I'm not sure we can come here, but if we do, that's the right thing to do.
        final Bundle args = getArguments();
        mContents.updateLocale(args.getString(UserDictionaryAddWordContents.EXTRA_LOCALE));
    }

    // Called by the locale picker
    @Override
    public void onLocaleSelected(final Locale locale) {
        mContents.updateLocale(locale.toString());
        getActivity().onBackPressed();
    }
}
