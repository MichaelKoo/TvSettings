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

import com.android.tv.settings.R;
import com.android.tv.settings.SettingsPreferenceFragment;
import com.android.tv.settings.util.Tools;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.provider.UserDictionary;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;

import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

public class UserDictionaryList extends SettingsPreferenceFragment {
    public static final String USER_DICTIONARY_SETTINGS_INTENT_ACTION =
            "android.settings.USER_DICTIONARY_SETTINGS";
    private String mLocale;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setPreferenceScreen(getPreferenceManager().createPreferenceScreen(getActivity()));
        getActivity().getActionBar().setTitle(R.string.user_dict_settings_title);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final Intent intent = getActivity().getIntent();
        final String localeFromIntent =
                null == intent ? null : intent.getStringExtra("locale");

        final Bundle arguments = getArguments();
        final String localeFromArguments =
                null == arguments ? null : arguments.getString("locale");

        final String locale;
        if (null != localeFromArguments) {
            locale = localeFromArguments;
        } else if (null != localeFromIntent) {
            locale = localeFromIntent;
        } else {
            locale = null;
        }
        mLocale = locale;
    }

    public static TreeSet<String> getUserDictionaryLocalesSet(Context context) {
        final Cursor cursor = context.getContentResolver().query(
                UserDictionary.Words.CONTENT_URI, new String[] { UserDictionary.Words.LOCALE },
                null, null, null);
        final TreeSet<String> localeSet = new TreeSet<String>();
        if (null == cursor) {
            // The user dictionary service is not present or disabled. Return null.
            return null;
        }
        try {
            if (cursor.moveToFirst()) {
                final int columnIndex = cursor.getColumnIndex(UserDictionary.Words.LOCALE);
                do {
                    final String locale = cursor.getString(columnIndex);
                    localeSet.add(null != locale ? locale : "");
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }

        // CAVEAT: Keep this for consistency of the implementation between Keyboard and Settings
        // if (!UserDictionarySettings.IS_SHORTCUT_API_SUPPORTED) {
        //     // For ICS, we need to show "For all languages" in case that the keyboard locale
        //     // is different from the system locale
        //     localeSet.add("");
        // }

        final InputMethodManager imm =
                (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        final List<InputMethodInfo> imis = imm.getEnabledInputMethodList();
        for (final InputMethodInfo imi : imis) {
            final List<InputMethodSubtype> subtypes =
                    imm.getEnabledInputMethodSubtypeList(
                            imi, true /* allowsImplicitlySelectedSubtypes */);
            for (InputMethodSubtype subtype : subtypes) {
                final String locale = subtype.getLocale();
                if (!TextUtils.isEmpty(locale)) {
                    localeSet.add(locale);
                }
            }
        }

        // We come here after we have collected locales from existing user dictionary entries and
        // enabled subtypes. If we already have the locale-without-country version of the system
        // locale, we don't add the system locale to avoid confusion even though it's technically
        // correct to add it.
        if (!localeSet.contains(Locale.getDefault().getLanguage().toString())) {
            localeSet.add(Locale.getDefault().toString());
        }

        return localeSet;
    }

    /**
     * Creates the entries that allow the user to go into the user dictionary for each locale.
     * @param userDictGroup The group to put the settings in.
     */
    protected void createUserDictSettings(PreferenceGroup userDictGroup) {
        final Activity activity = getActivity();
        userDictGroup.removeAll();
        final TreeSet<String> localeSet =
                UserDictionaryList.getUserDictionaryLocalesSet(activity);
        if (mLocale != null) {
            // If the caller explicitly specify empty string as a locale, we'll show "all languages"
            // in the list.
            localeSet.add(mLocale);
        }
        if (localeSet.size() > 1) {
            // Have an "All languages" entry in the languages list if there are two or more active
            // languages
            localeSet.add("");
        }

        if (localeSet.isEmpty()) {
            userDictGroup.addPreference(createUserDictionaryPreference(null, activity));
        } else {
            for (String locale : localeSet) {
                userDictGroup.addPreference(createUserDictionaryPreference(locale, activity));
            }
        }
    }

    /**
     * Create a single User Dictionary Preference object, with its parameters set.
     * @param locale The locale for which this user dictionary is for.
     * @return The corresponding preference.
     */
    protected Preference createUserDictionaryPreference(String locale, Activity activity) {
        final Preference newPref = new Preference(getActivity());
        final Intent intent = new Intent(USER_DICTIONARY_SETTINGS_INTENT_ACTION);
        if (null == locale) {
            newPref.setTitle(Locale.getDefault().getDisplayName());
        } else {
            if ("".equals(locale))
                newPref.setTitle(getString(R.string.user_dict_settings_all_languages));
            else
                newPref.setTitle(Tools.createLocaleFromString(locale).getDisplayName());
            intent.putExtra("locale", locale);
            newPref.getExtras().putString("locale", locale);
        }
        newPref.setIntent(intent);
        newPref.setFragment(com.android.tv.settings.UserDictionarySettings.class.getName());
        return newPref;
    }

    @Override
    public void onResume() {
        super.onResume();
        createUserDictSettings(getPreferenceScreen());
    }
}
