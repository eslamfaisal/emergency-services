package com.bluethunder.tar2.ui.auth.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluethunder.tar2.cloud_db.CloudDBWrapper.mUsersCloudDBZone
import com.bluethunder.tar2.model.Resource
import com.bluethunder.tar2.ui.auth.model.UserModel
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.auth.EmailAuthProvider
import com.huawei.agconnect.auth.EmailUser
import com.huawei.agconnect.auth.VerifyCodeSettings
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    companion object {
        private val TAG = AuthViewModel::class.java.simpleName
    }

    private val _dataLoading = MutableLiveData(true)
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _userData = MutableLiveData<Resource<UserModel>>()
    val userData: LiveData<Resource<UserModel>> = _userData

    fun loginWithEmailAndPassword(email: String, password: String) {
        setUserData(Resource.loading())

        val credential = EmailAuthProvider.credentialWithPassword(email, password)

        AGConnectAuth.getInstance().signIn(credential).addOnSuccessListener {
            Log.d(TAG, "loginWithEmailAndPassword: email: ${it.user.email}")
            val query =
                CloudDBZoneQuery.where(UserModel::class.java).equalTo("id", it.user.uid).limit(1)

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

        }.addOnFailureListener {
            setUserData(Resource.error(it?.message))
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

}