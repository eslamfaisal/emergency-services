package com.bluethunder.tar2

import com.bluethunder.tar2.ui.auth.model.UserModel
import com.huawei.hms.maps.model.LatLng

object SessionConstants {
    var currentLanguage = "en"
    var currentLoggedInUserModel: UserModel? = null
    var myCurrentLocation: LatLng? = null
    var enabledCategories: List<String>? = null
}