package com.bluethunder.tar2.ui.auth.viewmodel

import android.app.Activity
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
import java.util.*

class AuthViewModel : ViewModel() {

    companion object {
        private val TAG = AuthViewModel::class.java.simpleName
    }

    private var profileImageLocalPath: String = ""
    private var profileImageUrl: String = ""
    private var imageSelected = false
    var imageUploaded = false

    private val _uploadingImage = MutableLiveData<Resource<Boolean>>()
    val uploadingImage: LiveData<Resource<Boolean>> = _uploadingImage

    private val _userData = MutableLiveData<Resource<UserModel>>()
    val userData: LiveData<Resource<UserModel>> = _userData

    private val _signInWithHuaweiId = MutableLiveData<Resource<SignInResult>>()
    val signInWithHuaweiId: LiveData<Resource<SignInResult>> = _signInWithHuaweiId

    private val _newAccountWithPhoneResult = MutableLiveData<Resource<String>>()
    val newAccountWithPhoneResult: LiveData<Resource<String>> = _newAccountWithPhoneResult

    private val _phoneCodeResult = MutableLiveData<Resource<VerifyCodeResult>>()
    val phoneCodeResult: LiveData<Resource<VerifyCodeResult>> = _phoneCodeResult

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

    fun verifyEmailCode(email: String, password: String, code: String) {
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

    fun verifyPhoneNumber(countryCodeStr: String, phoneNumberStr: String) {
        setPhoneCodeResult(Resource.loading())
        val settings = VerifyCodeSettings.newBuilder()
            .action(VerifyCodeSettings.ACTION_REGISTER_LOGIN)
            .sendInterval(30)
            .locale(Locale.ENGLISH)
            .build()
        val task =
            AGConnectAuth.getInstance().requestVerifyCode(countryCodeStr, phoneNumberStr, settings)
        task.addOnSuccessListener {
            // onSuccess
            setPhoneCodeResult(Resource.success(it))
        }.addOnFailureListener {
            // onFail
            setPhoneCodeResult(Resource.error(it.message))
        }
    }


    fun createAccountWithPHoneNumber(
        countryCode: String,
        phoneNumber: String,
        password: String?,
        otp: String
    ) {

        setNewAccountWithPhoneResult(Resource.loading())
        val credential =
            PhoneAuthProvider.credentialWithVerifyCode(countryCode, phoneNumber, password, otp)
        AGConnectAuth.getInstance().signIn(credential).addOnSuccessListener {
            // Obtain sign-in information.
            setNewAccountWithPhoneResult(Resource.success("created"))
        }.addOnFailureListener {
            // onFail
            setNewAccountWithPhoneResult(Resource.error(it.message))
        }
    }

    fun linkPhoneToHuaweiIDAccount(
        countryCode: String,
        phoneNumber: String,
        otp: String
    ) {
        setNewAccountWithPhoneResult(Resource.loading())
        AGConnectAuth.getInstance().currentUser.updatePhone(countryCode, phoneNumber, otp)
            .addOnSuccessListener {
                // onSuccess
                setNewAccountWithPhoneResult(Resource.success("updated"))
            }.addOnFailureListener {
                // onFail
                setNewAccountWithPhoneResult(Resource.error(it.message))
            }
    }

    fun setNewAccountWithPhoneResult(result: Resource<String>) {
        _newAccountWithPhoneResult.value = result
    }

    fun setPhoneCodeResult(result: Resource<VerifyCodeResult>) {
        _phoneCodeResult.value = result
    }


    fun signInWithHuaweiId(activity: Activity) {
        setSignInWithHuaweiIdResponse(Resource.loading())
        AGConnectAuth.getInstance().signIn(activity, AGConnectAuthCredential.HMS_Provider)
            .addOnSuccessListener {
                setSignInWithHuaweiIdResponse(Resource.success(it))
            }.addOnFailureListener {
                setSignInWithHuaweiIdResponse(Resource.error(it.message))
            }
    }

    private fun setSignInWithHuaweiIdResponse(success: Resource<SignInResult>) {
        _signInWithHuaweiId.value = success
    }

    fun signOut() {
        AGConnectAuth.getInstance().signOut()
    }

    private fun setUserData(result: Resource<UserModel>) {
        _userData.value = result
    }

    fun setProfileImageLocalPath(path: String) {
        profileImageLocalPath = path
    }

    fun uploadProfileImage() {
        setDataLoading(Resource.loading())

        val reference =
            storageManagement.getStorageReference("profile_image/${System.currentTimeMillis()}.jpg")
        val uploadTask = reference.putFile(File(profileImageLocalPath))
        uploadTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                task.result.storage.downloadUrl.addOnSuccessListener {
                    profileImageUrl = it.toString()
                    imageUploaded = true
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
        _uploadingImage.value = loading
    }

    fun setImageSelected(selected: Boolean) {
        imageSelected = selected
        if (!imageSelected) {
            imageUploaded = false
        }
    }

    fun removeImage() {
        setImageSelected(false)
    }

    fun isImageSelected(): Boolean {
        return imageSelected
    }

    fun resetRegisterFields() {
        signOut()
        setUserData(Resource.empty())
        setDataLoading(Resource.empty())
        setPhoneCodeResult(Resource.empty())
        setSignInWithHuaweiIdResponse(Resource.empty())
    }


}