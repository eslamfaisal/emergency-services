package com.bluethunder.tar2.ui.edit_case.model

import java.io.Serializable

class CaseModel : Serializable {

    var id: String? = null
    var userId: String? = null
    var categoryId: String? = null
    var title: String? = null
    var description: String? = null
    var images: String? = null
    var mainImage: String? = null
    var latitude: String? = null
    var longitude: String? = null
    var showUserData = true
    var hasChatMessages = true
    var hasPhoneCall = true
    var hasOnlineCall = true
    var hasVideoCall = true
    var locationName: String? = null
    var address: String? = null

    override fun toString(): String {
        return "CaseModel{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", categoryId='" + categoryId + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", images='" + images + '\'' +
                ", mainImage='" + mainImage + '\'' +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", showUserData=" + showUserData +
                ", hasPhoneCall=" + hasPhoneCall +
                ", hasChatMessages=" + hasChatMessages +
                ", hasOnlineCall='" + hasOnlineCall + '\'' +
                ", hasVideoCall='" + hasVideoCall + '\'' +
                ", locationName='" + locationName + '\'' +
                ", address='" + address + '\'' +
                '}'
    }
}