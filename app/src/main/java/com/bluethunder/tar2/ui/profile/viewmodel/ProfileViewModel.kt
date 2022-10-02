package com.bluethunder.tar2.ui.profile.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bluethunder.tar2.cloud_db.FirestoreReferences
import com.bluethunder.tar2.ui.auth.model.UserModel
import com.google.firebase.firestore.FirebaseFirestore


class ProfileViewModel : ViewModel() {

    companion object {
        private val TAG = ProfileViewModel::class.java.simpleName
    }


    private val _currentCaseUserDetails = MutableLiveData<UserModel?>()
    val currentCaseUserDetails: LiveData<UserModel?> = _currentCaseUserDetails

    fun listenToUserDetails(userID: String) {

        FirebaseFirestore.getInstance()
            .collection(FirestoreReferences.UsersCollection.value())
            .document(userID)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e(TAG, "listenToComments: ", error)
                } else {
                    val case = value!!.toObject(UserModel::class.java)
                    _currentCaseUserDetails.value = case
                }
            }
    }


}
