package com.bluethunder.tar2.ui.profile.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bluethunder.tar2.SessionConstants
import com.bluethunder.tar2.cloud_db.CloudStorageWrapper
import com.bluethunder.tar2.cloud_db.FirestoreReferences
import com.bluethunder.tar2.model.Resource
import com.bluethunder.tar2.ui.auth.model.UserModel
import com.bluethunder.tar2.ui.edit_case.viewmodel.EditCaseViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.io.File


class ProfileViewModel : ViewModel() {

    companion object {
        private val TAG = ProfileViewModel::class.java.simpleName
    }


    val userID = SessionConstants.currentLoggedInUserModel!!.id!!
    var snapshotRegistration: ListenerRegistration? = null

    private val _currentCaseUserDetails = MutableLiveData<UserModel?>()
    val currentCaseUserDetails: LiveData<UserModel?> = _currentCaseUserDetails

    private val _uploadingImage = MutableLiveData<Boolean>()
    val uploadingImage: LiveData<Boolean> = _uploadingImage


    fun listenToUserDetails() {
        snapshotRegistration =
            FirebaseFirestore.getInstance().collection(FirestoreReferences.UsersCollection.value())
                .document(userID).addSnapshotListener { value, error ->
                    if (error != null) {
                        Log.e(TAG, "listenToComments: ", error)
                    } else {
                        val case = value!!.toObject(UserModel::class.java)
                        _currentCaseUserDetails.value = case
                    }
                }
    }

    fun uploadProfileImage(path: String) {
        setMainImageLoading(true)

        val reference =
            CloudStorageWrapper.storageManagement.getStorageReference("profile_image/${System.currentTimeMillis()}.jpg")
        val uploadTask = reference.putFile(File(path))
        uploadTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                task.result.storage.downloadUrl.addOnSuccessListener {
                    updateUserProfileImage(it.toString())
                }.addOnFailureListener {
                    setMainImageLoading(false)
                }
            } else {
                setMainImageLoading(false)
            }
        }
    }

    fun updateUserProfileImage(url: String){
        FirebaseFirestore.getInstance().collection(FirestoreReferences.UsersCollection.value())
            .document(userID).update(
                mapOf("imageUrl" to url)
            ).addOnCompleteListener {
                Log.d(TAG, "updateUserProfileImage: isSuccessful = ${it.isSuccessful}")
                setMainImageLoading(false)
            }
    }

    private fun setMainImageLoading(loading: Boolean) {
        _uploadingImage.value = loading
    }


    fun removeSnapshotListener() {
        snapshotRegistration?.remove()
    }
}
