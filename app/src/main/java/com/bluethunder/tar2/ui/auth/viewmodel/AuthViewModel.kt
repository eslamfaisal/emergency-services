package com.bluethunder.tar2.ui.auth.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluethunder.tar2.cloud_db.CloudDBWrapper.mUsersCloudDBZone
import com.bluethunder.tar2.cloud_db.CloudStorageWrapper.storageManagement
import com.bluethunder.tar2.model.Resource
import com.bluethunder.tar2.ui.auth.model.UserModel
import com.huawei.agconnect.auth.*
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery
import kotlinx.coroutines.launch
import java.io.File

class AuthViewModel : ViewModel() {

    companion object {
        private val TAG = AuthViewModel::class.java.simpleName
    }

    var profileImageUrl: String = ""

    private val _dataLoading = MutableLiveData<Resource<Boolean>>()
    val dataLoading: LiveData<Resource<Boolean>> = _dataLoading

    private val _userData = MutableLiveData<Resource<UserModel>>()
    val userData: LiveData<Resource<UserModel>> = _userData

    fun loginWithEmailAndPassword(email: String, password: String) {
        setUserData(Resource.loading())
        val credential = EmailAuthProvider.credentialWithPassword(email, password)
        AGConnectAuth.getInstance().signIn(credential).addOnSuccessListener { user ->
            getUserDetails(user)
        }.addOnFailureListener {
            setUserData(Resource.error(it?.message))
        }
    }

    private fun getUserDetails(it: SignInResult) {
        val query = CloudDBZoneQuery.where(UserModel::class.java)
            .equalTo("id", it.user.uid).limit(1)
        mUsersCloudDBZone!!.executeQuery(
            query,
            CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_DEFAULT
        ).addOnCompleteListener {
            if (it.isSuccessful) {
                if (it.result.snapshotObjects.size() > 0) {
                    val user = it.result.snapshotObjects.get(0) as UserModel
                    setUserData(Resource.success(user))
                } else {
                    setUserData(Resource.error("User not found"))
                }
            } else {
                setUserData(Resource.error(it.exception?.message))
            }
        }
    }

    fun sendVerificationEmail(email: String) {
        viewModelScope.launch {
            val settings = VerifyCodeSettings.newBuilder()
                .action(VerifyCodeSettings.ACTION_REGISTER_LOGIN)
                .sendInterval(30)
                .build()
            val task = AGConnectAuth.getInstance().requestVerifyCode(email, settings)
            task.addOnSuccessListener {
                // onSuccess
            }.addOnFailureListener {
                // onFail
            }
        }
    }

    fun verifyCode(email: String, password: String, code: String) {
        viewModelScope.launch {
            val emailUser = EmailUser.Builder()
                .setEmail(email)
                .setVerifyCode(code)
                .setPassword(password)
                .build()
            AGConnectAuth.getInstance().createUser(emailUser).addOnSuccessListener {
                // A newly created user account is automatically signed in to your app.
            }.addOnFailureListener {
                // onFail
            }
        }
    }

    private fun setUserData(result: Resource<UserModel>) {
        _userData.value = result
    }

    fun setProfileImage(url: String) {
        if (url.isNotEmpty()) {
            profileImageUrl = url
            uploadProfileImage()
        } else {
            profileImageUrl = ""
        }

    }

    private fun uploadProfileImage() {
        setDataLoading(Resource.loading())

        val reference =
            storageManagement.getStorageReference("profile_image/${System.currentTimeMillis()}.jpg")
        val uploadTask = reference.putFile(File(profileImageUrl))
        uploadTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                task.result.storage.downloadUrl.addOnSuccessListener {
                    profileImageUrl = it.toString()
                    setDataLoading(Resource.success(true))
                }.addOnFailureListener {
                    setDataLoading(Resource.error(it.message))
                }
            } else {
                setDataLoading(Resource.error(task.exception?.message))
            }
        }
    }


    private fun setDataLoading(loading: Resource<Boolean>) {
        _dataLoading.value = loading
    }
}