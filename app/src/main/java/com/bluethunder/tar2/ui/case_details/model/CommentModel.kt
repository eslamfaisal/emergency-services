package com.bluethunder.tar2.ui.case_details.model

import android.os.Parcelable
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
class CommentModel : Parcelable {
    var id: String? = null
    var userId: String? = null
    var userName: String? = null
    var userImage: String? = null
    var caseId: String? = null
    var comment: String? = null
    var imageUrl: String? = null
    var type: String? = null

    @ServerTimestamp
    var createdAt: Date? = null

    override fun equals(other: Any?): Boolean {
        other?.let {
            if (it is CommentModel) {
                return it.id == id
            } else return false
        } ?: return false
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (userId?.hashCode() ?: 0)
        result = 31 * result + (userName?.hashCode() ?: 0)
        result = 31 * result + (userImage?.hashCode() ?: 0)
        result = 31 * result + (caseId?.hashCode() ?: 0)
        result = 31 * result + (comment?.hashCode() ?: 0)
        result = 31 * result + (imageUrl?.hashCode() ?: 0)
        result = 31 * result + (type?.hashCode() ?: 0)
        result = 31 * result + (createdAt?.hashCode() ?: 0)
        return result
    }
}