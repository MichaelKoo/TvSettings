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

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.UserDictionary;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.android.tv.settings.R;
import com.android.tv.settings.UserDictionarySettings;
import com.android.tv.settings.util.Tools;

import java.util.ArrayList;
import java.util.Locale;
import java.util.TreeSet;

/**
 * A container class to factor common code to UserDictionaryAddWordFragment
 * and UserDictionaryAddWordActivity.
 */
public class UserDictionaryAddWordContents {
    public static final String EXTRA_MODE = "mode";
    public static final String EXTRA_WORD = "word";
    public static final String EXTRA_SHORTCUT = "shortcut";
    public static final String EXTRA_LOCALE = "locale";
    public static final String EXTRA_ORIGINAL_WORD = "originalWord";
    public static final String EXTRA_ORIGINAL_SHORTCUT = "originalShortcut";

    public static final int MODE_EDIT = 0;
    public static final int MODE_INSERT = 1;

    private static final int FREQUENCY_FOR_USER_DICTIONARY_ADDS = 250;

    private final int mMode; // Either MODE_EDIT or MODE_INSERT
    private final EditText mWordEditText;
    private final EditText mShortcutEditText;
    private String mLocale;
    private final String mOldWord;
    private final String mOldShortcut;
    private String mSavedWord;
    private String mSavedShortcut;

    /* package */ UserDictionaryAddWordContents(final View view, final Bundle args) {
        mWordEditText = (EditText)view.findViewById(R.id.user_dictionary_add_word_text);
        mShortcutEditText = (EditText)view.findViewById(R.id.user_dictionary_add_shortcut);
        final String word = args.getString(EXTRA_WORD);
        if (null != word) {
            mWordEditText.setText(word);
            // Use getText in case the edit text modified the text we set. This happens when
            // it's too long to be edited.
            mWordEditText.setSelection(mWordEditText.getText().length());
        }
        final String shortcut = args.getString(EXTRA_SHORTCUT);
        if (null != shortcut && null != mShortcutEditText) {
            mShortcutEditText.setText(shortcut);
        }
        mMode = args.getInt(EXTRA_MODE); // default return value for #getInt() is 0 = MODE_EDIT
        mOldWord = args.getString(EXTRA_WORD);
        mOldShortcut = args.getString(EXTRA_SHORTCUT);
        updateLocale(args.getString(EXTRA_LOCALE));
    }

    /* package */ UserDictionaryAddWordContents(final View view,
            final UserDictionaryAddWordContents oldInstanceToBeEdited) {
        mWordEditText = (EditText)view.findViewById(R.id.user_dictionary_add_word_text);
        mShortcutEditText = (EditText)view.findViewById(R.id.user_dictionary_add_shortcut);
        mMode = MODE_EDIT;
        mOldWord = oldInstanceToBeEdited.mSavedWord;
        mOldShortcut = oldInstanceToBeEdited.mSavedShortcut;
        updateLocale(mLocale);
    }

    // locale may be null, this means default locale
    // It may also be the empty string, which means "all locales"
    /* package */ void updateLocale(final String locale) {
        mLocale = null == locale ? Locale.getDefault().toString() : locale;
    }

    /* package */ void saveStateIntoBundle(final Bundle outState) {
        outState.putString(EXTRA_WORD, mWordEditText.getText().toString());
        outState.putString(EXTRA_ORIGINAL_WORD, mOldWord);
        if (null != mShortcutEditText) {
            outState.putString(EXTRA_SHORTCUT, mShortcutEditText.getText().toString());
        }
        if (null != mOldShortcut) {
            outState.putString(EXTRA_ORIGINAL_SHORTCUT, mOldShortcut);
        }
        outState.putString(EXTRA_LOCALE, mLocale);
    }

    /* package */ void delete(final Context context) {
        if (MODE_EDIT == mMode && !TextUtils.isEmpty(mOldWord)) {
            // Mode edit: remove the old entry.
            final ContentResolver resolver = context.getContentResolver();
            UserDictionarySettings.deleteWord(mOldWord, mOldShortcut, resolver);
        }
        // If we are in add mode, nothing was added, so we don't need to do anything.
    }

    /* package */ int apply(final Context context, final Bundle outParameters) {
        if (null != outParameters) saveStateIntoBundle(outParameters);
        final ContentResolver resolver = context.getContentResolver();
        if (MODE_EDIT == mMode && !TextUtils.isEmpty(mOldWord)) {
            // Mode edit: remove the old entry.
            UserDictionarySettings.deleteWord(mOldWord, mOldShortcut, resolver);
        }
        final String newWord = mWordEditText.getText().toString();
        final String newShortcut;
        if (null == mShortcutEditText) {
            newShortcut = null;
        } else {
            final String tmpShortcut = mShortcutEditText.getText().toString();
            if (TextUtils.isEmpty(tmpShortcut)) {
                newShortcut = null;
            } else {
                newShortcut = tmpShortcut;
            }
        }
        if (TextUtils.isEmpty(newWord)) {
            // If the word is somehow empty, don't insert it.
            return UserDictionaryAddWordActivity.CODE_CANCEL;
        }
        mSavedWord = newWord;
        mSavedShortcut = newShortcut;
        // If there is no shortcut, and the word already exists in the database, then we
        // should not insert, because either A. the word exists with no shortcut, in which
        // case the exact same thing we want to insert is already there, or B. the word
        // exists with at least one shortcut, in which case it has priority on our word.
        if (TextUtils.isEmpty(newShortcut) && hasWord(newWord, context)) {
            return UserDictionaryAddWordActivity.CODE_ALREADY_PRESENT;
        }

        // Disallow duplicates. If the same word with no shortcut is defined, remove it; if
        // the same word with the same shortcut is defined, remove it; but we don't mind if
        // there is the same word with a different, non-empty shortcut.
        UserDictionarySettings.deleteWord(newWord, null, resolver);
        if (!TextUtils.isEmpty(newShortcut)) {
            // If newShortcut is empty we just deleted this, no need to do it again
            UserDictionarySettings.deleteWord(newWord, newShortcut, resolver);
        }

        // In this class we use the empty string to represent 'all locales' and mLocale cannot
        // be null. However the addWord method takes null to mean 'all locales'.
        UserDictionary.Words.addWord(context, newWord.toString(),
                FREQUENCY_FOR_USER_DICTIONARY_ADDS, newShortcut,
                TextUtils.isEmpty(mLocale) ? null : Tools.createLocaleFromString(mLocale));

        return UserDictionaryAddWordActivity.CODE_WORD_ADDED;
    }

    private static final String[] HAS_WORD_PROJECTION = { UserDictionary.Words.WORD };
    private static final String HAS_WORD_SELECTION_ONE_LOCALE = UserDictionary.Words.WORD
            + "=? AND " + UserDictionary.Words.LOCALE + "=?";
    private static final String HAS_WORD_SELECTION_ALL_LOCALES = UserDictionary.Words.WORD
            + "=? AND " + UserDictionary.Words.LOCALE + " is null";
    private boolean hasWord(final String word, final Context context) {
        final Cursor cursor;
        // mLocale == "" indicates this is an entry for all languages. Here, mLocale can't
        // be null at all (it's ensured by the updateLocale method).
        if ("".equals(mLocale)) {
            cursor = context.getContentResolver().query(UserDictionary.Words.CONTENT_URI,
                      HAS_WORD_PROJECTION, HAS_WORD_SELECTION_ALL_LOCALES,
                      new String[] { word }, null /* sort order */);
        } else {
            cursor = context.getContentResolver().query(UserDictionary.Words.CONTENT_URI,
                      HAS_WORD_PROJECTION, HAS_WORD_SELECTION_ONE_LOCALE,
                      new String[] { word, mLocale }, null /* sort order */);
        }
        try {
            if (null == cursor) return false;
            return cursor.getCount() > 0;
        } finally {
            if (null != cursor) cursor.close();
        }
    }

    public static class LocaleRenderer {
        private final String mLocaleString;
        private final String mDescription;
        // LocaleString may NOT be null.
        public LocaleRenderer(final Context context, final String localeString) {
            mLocaleString = localeString;
            if (null == localeString) {
                mDescription = context.getString(R.string.user_dict_settings_more_languages);
            } else if ("".equals(localeString)) {
                mDescription = context.getString(R.string.user_dict_settings_all_languages);
            } else {
                mDescription = Tools.createLocaleFromString(localeString).getDisplayName();
            }
        }
        @Override
        public String toString() {
            return mDescription;
        }
        public String getLocaleString() {
            return mLocaleString;
        }
        // "More languages..." is null ; "All languages" is the empty string.
        public boolean isMoreLanguages() {
            return null == mLocaleString;
        }
    }

    private static void addLocaleDisplayNameToList(final Context context,
            final ArrayList<LocaleRenderer> list, final String locale) {
        if (null != locale) {
            list.add(new LocaleRenderer(context, locale));
        }
    }

    // Helper method to get the list of locales to display for this word
    public ArrayList<LocaleRenderer> getLocalesList(final Activity activity) {
        final TreeSet<String> locales = UserDictionaryList.getUserDictionaryLocalesSet(activity);
        // Remove our locale if it's in, because we're always gonna put it at the top
        locales.remove(mLocale); // mLocale may not be null
        final String systemLocale = Locale.getDefault().toString();
        // The system locale should be inside. We want it at the 2nd spot.
        locales.remove(systemLocale); // system locale may not be null
        locales.remove(""); // Remove the empty string if it's there
        final ArrayList<LocaleRenderer> localesList = new ArrayList<LocaleRenderer>();
        // Add the passed locale, then the system locale at the top of the list. Add an
        // "all languages" entry at the bottom of the list.
        addLocaleDisplayNameToList(activity, localesList, mLocale);
        if (!systemLocale.equals(mLocale)) {
            addLocaleDisplayNameToList(activity, localesList, systemLocale);
        }
        for (final String l : locales) {
            // TODO: sort in unicode order
            addLocaleDisplayNameToList(activity, localesList, l);
        }
        if (!"".equals(mLocale)) {
            // If mLocale is "", then we already inserted the "all languages" item, so don't do it
            addLocaleDisplayNameToList(activity, localesList, ""); // meaning: all languages
        }
        localesList.add(new LocaleRenderer(activity, null)); // meaning: select another locale
        return localesList;
    }

    public String getCurrentUserDictionaryLocale() {
        return mLocale;
    }
}
