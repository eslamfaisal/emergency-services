package com.bluethunder.tar2.utils

import android.content.Context
import android.content.SharedPreferences

object SharedHelperKeys {
    const val ON_BOARDING_SHOW = "on_boarding_show"
    const val IS_LOGGED_IN = "isLoggedIn"
    const val USER_DATA = "userData"
    const val LANGUAGE_KEY = "languageKey"
    const val PERMISSIONS_REQUEST = "permissions_request"
    const val NOTIFICATION_ENABLED = "notificationEnabled"
}

object SharedHelper {

    private const val KEY = "shared_prefs_key"
    lateinit var sharedPreferences: SharedPreferences
    lateinit var editor: SharedPreferences.Editor

    fun putString(context: Context, Key: String?, Value: String?) {
        sharedPreferences = context.getSharedPreferences(KEY, Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()
        editor.putString(Key, Value)
        editor.commit()
    }

    fun getString(contextGetKey: Context, Key: String?, defaultValue: String? = ""): String? {
        sharedPreferences = contextGetKey.getSharedPreferences(KEY, Context.MODE_PRIVATE)
        return sharedPreferences.getString(Key, defaultValue)
    }

    fun getBoolean(contextGetKey: Context, Key: String?, defaultValue: Boolean = false): Boolean {
        sharedPreferences = contextGetKey.getSharedPreferences(KEY, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(Key, defaultValue)
    }

    fun getInt(contextGetKey: Context, Key: String?, default: Int = -1): Int {
        sharedPreferences = contextGetKey.getSharedPreferences(KEY, Context.MODE_PRIVATE)
        return sharedPreferences.getInt(Key, default)
    }

    fun putBoolean(context: Context, Key: String?, Value: Boolean) {
        sharedPreferences = context.getSharedPreferences(KEY, Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()
        editor.putBoolean(Key, Value)
        editor.commit()
    }

    fun putInt(context: Context, Key: String?, Value: Int) {
        sharedPreferences = context.getSharedPreferences(KEY, Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()
        editor.putInt(Key, Value)
        editor.commit()
    }

    fun putIntApply(context: Context, Key: String?, Value: Int) {
        sharedPreferences = context.getSharedPreferences(KEY, Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()
        editor.putInt(Key, Value)
        editor.apply()
    }

    fun clearSharedPreferences(context: Context) {
        sharedPreferences = context.getSharedPreferences(KEY, Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().commit()
    }
}