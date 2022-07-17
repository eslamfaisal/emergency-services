package com.bluethunder.tar2.utils

import android.content.Context
import com.bluethunder.tar2.R


fun parseHMSErrorMsg(msg: String): String {
    return msg.replace("code", "").replace("\\d".toRegex(), "").trim()
}

fun Context.getErrorMsg(msg: String): String {
    return if (msg.contains("code: 0")) {
        this.getString(R.string.no_enternet_connection)
    } else if (msg.contains("code: 203818038")) {
        this.getString(R.string.no_enternet_connection)
    } else if (msg.contains("code: 203818032")) {
        this.getString(R.string.incorrect_phone_or_password)
    }else if (msg.contains("code: 203818129")) {
        this.getString(R.string.incorrect_verification_code)
    } else parseHMSErrorMsg(msg)
}
