package com.bluethunder.tar2.ui.auth.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluethunder.tar2.cloud_db.CloudDBWrapper.mUsersCloudDBZone
import com.bluethunder.tar2.ui.auth.model.UserModel
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    companion object {
        private val TAG = AuthViewModel::class.java.simpleName
    }

    private val _userData = MutableLiveData<UserModel>()
    val userData: LiveData<UserModel> = _userData


    fun getUserDetails(userID: String = "123456") {
        viewModelScope.launch {
            val query =
                CloudDBZoneQuery.where(UserModel::class.java).equalTo(UserModel.ID, userID)

            if (mUsersCloudDBZone != null) {
                mUsersCloudDBZone!!.executeQuery(
                    query,
                    CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_DEFAULT
                ).addOnCompleteListener {
                    if (it.isSuccessful) {
                        _userData.value = it.result.snapshotObjects.next()
                    }
                }
            }else{
                Log.d(TAG, "getUserDetails: mUsersCloudDBZone is null")
            }
        }

    }
}