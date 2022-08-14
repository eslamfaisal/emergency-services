package com.bluethunder.tar2

import com.bluethunder.tar2.ui.auth.model.UserModel
import com.huawei.location.lite.common.util.coordinateconverter.LatLon

object SessionConstants {
    var currentLanguage = "en"
    var currentLoggedInUserModel: UserModel? = null
    var myCurrentLocation: LatLon? = null
}