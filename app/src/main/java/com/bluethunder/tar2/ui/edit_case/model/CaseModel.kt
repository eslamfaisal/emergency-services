package com.bluethunder.tar2.ui.edit_case.model

import com.bluethunder.tar2.ui.home.model.CaseStatus
import com.google.firebase.firestore.ServerTimestamp
import java.io.Serializable
import java.util.*

class CaseModel : Serializable {

    var id: String? = null
    var userId: String? = null
    var userName: String? = null
    var userImage: String? = null
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
    var countryCode: String? = null
    var status: String = CaseStatus.Published.name
    var upVotesCount: Int = 0
    var viewsCount: Int = 0
    var commentsCount: Int = 0

    @ServerTimestamp
    var createdAt: Date = Date()

    override fun toString(): String {
        return "CaseModel{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", userId='" + userName + '\'' +
                ", userId='" + userImage + '\'' +
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
                ", countryCode='" + countryCode + '\'' +
                ", address='" + address + '\'' +
                ", upVotesCount='" + upVotesCount + '\'' +
                ", viewsCount='" + viewsCount + '\'' +
                ", commentsCount='" + commentsCount + '\'' +
                ", status='" + status + '\'' +
                ", createdAt='" + createdAt + '\'' +
                '}'
    }

    override fun equals(other: Any?): Boolean {
        other?.let {
            if (it is CaseModel) {
                return it.id == id
            } else return false
        } ?: return false
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (userId?.hashCode() ?: 0)
        result = 31 * result + (userName?.hashCode() ?: 0)
        result = 31 * result + (userImage?.hashCode() ?: 0)
        result = 31 * result + (categoryId?.hashCode() ?: 0)
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (images?.hashCode() ?: 0)
        result = 31 * result + (mainImage?.hashCode() ?: 0)
        result = 31 * result + (latitude?.hashCode() ?: 0)
        result = 31 * result + (longitude?.hashCode() ?: 0)
        result = 31 * result + showUserData.hashCode()
        result = 31 * result + hasChatMessages.hashCode()
        result = 31 * result + hasPhoneCall.hashCode()
        result = 31 * result + hasOnlineCall.hashCode()
        result = 31 * result + hasVideoCall.hashCode()
        result = 31 * result + (locationName?.hashCode() ?: 0)
        result = 31 * result + (address?.hashCode() ?: 0)
        result = 31 * result + (countryCode?.hashCode() ?: 0)
        result = 31 * result + status.hashCode()
        result = 31 * result + upVotesCount
        result = 31 * result + viewsCount
        result = 31 * result + commentsCount
        result = 31 * result + createdAt.hashCode()
        return result
    }
}