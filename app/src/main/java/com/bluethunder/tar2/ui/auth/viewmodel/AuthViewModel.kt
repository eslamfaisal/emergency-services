package com.bluethunder.tar2.ui.auth.viewmodel

import android.app.Activity
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluethunder.tar2.cloud_db.CloudStorageWrapper.storageManagement
import com.bluethunder.tar2.cloud_db.FirestoreReferences
import com.bluethunder.tar2.model.Resource
import com.bluethunder.tar2.ui.auth.model.UserModel
import com.google.firebase.firestore.FirebaseFirestore
import com.huawei.agconnect.auth.*
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

class AuthViewModel : ViewModel() {

    companion object {
        private val TAG = AuthViewModel::class.java.simpleName
    }

    private var profileImageLocalPath: String = ""
    var profileImageUrl: String = ""
    private var imageSelected = false
    var imageUploaded = false

    private val _uploadingImage = MutableLiveData<Resource<Boolean>>()
    val uploadingImage: LiveData<Resource<Boolean>> = _uploadingImage

    private val _createUserData = MutableLiveData<Resource<UserModel>>()
    val createUserData: LiveData<Resource<UserModel>> = _createUserData

    private val _userData = MutableLiveData<Resource<UserModel>>()
    val getUserData: LiveData<Resource<UserModel>> = _userData

    private val _signInWithHuaweiId = MutableLiveData<Resource<SignInResult>>()
    val signInWithHuaweiId: LiveData<Resource<SignInResult>> = _signInWithHuaweiId

    private val _signInWithHPhone = MutableLiveData<Resource<SignInResult>>()
    val signInWithHPhone: LiveData<Resource<SignInResult>> = _signInWithHPhone

    private val _newAccountWithPhoneResult = MutableLiveData<Resource<String>>()
    val newAccountWithPhoneResult: LiveData<Resource<String>> = _newAccountWithPhoneResult

    private val _resetPasswordResult = MutableLiveData<Resource<String>>()
    val resetPasswordResult: LiveData<Resource<String>> = _resetPasswordResult

    private val _phoneCodeResult = MutableLiveData<Resource<VerifyCodeResult>>()
    val phoneCodeResult: LiveData<Resource<VerifyCodeResult>> = _phoneCodeResult

    fun loginWithEmailAndPassword(
        countryCode: String,
        phoneNumber: String,
        password: String?
    ) {
        AGConnectAuth.getInstance().signOut()
        setSignInWithPhoneResult(Resource.loading())
        val credential =
            PhoneAuthProvider.credentialWithPassword(countryCode, phoneNumber, password)
        AGConnectAuth.getInstance().signIn(credential).addOnSuccessListener {
            // Obtain sign-in information.
            setSignInWithPhoneResult(Resource.success(it))
        }.addOnFailureListener {
            // onFail
            setSignInWithPhoneResult(Resource.error(it.message))
        }
    }

    private fun setSignInWithPhoneResult(result: Resource<SignInResult>) {
        _signInWithHPhone.value = result
    }


    fun getUserDetails(it: SignInResult) {
        FirebaseFirestore.getInstance()
            .collection(FirestoreReferences.UsersCollection.value())
            .document(it.user.uid).get()
            .addOnSuccessListener {
                try {
                    if (it.exists()) {
                        val user: UserModel = it.toObject(UserModel::class.java)!!
                        setUserData(Resource.success(user))
                    } else {
                        setUserData(Resource.error("User not found"))
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "getUserDetails:ins ${e}")
                    setUserData(Resource.error(e.message))
                }
            }.addOnFailureListener {
                Log.d(TAG, "getUserDetails: ${it}")
                setUserData(Resource.error(it.message))
            }
    }

    fun createUserToDatabase(userModel: UserModel) {
        userModel.id = AGConnectAuth.getInstance().currentUser?.uid

        FirebaseFirestore.getInstance()
            .collection(FirestoreReferences.UsersCollection.value())
            .document(userModel.id).get()
            .addOnSuccessListener {
                if (it.exists()) {
                    setCreateUserData(Resource.error("code: 203818038"))
                } else {
                    FirebaseFirestore.getInstance()
                        .collection(FirestoreReferences.UsersCollection.value())
                        .document(userModel.id).set(userModel)
                        .addOnSuccessListener {
                            setCreateUserData(Resource.success(userModel))
                        }.addOnFailureListener {
                            setCreateUserData(Resource.error(it.message))
                        }
                }
            }.addOnFailureListener {
                setUserData(Resource.error(it.message))
            }
    }

    private fun setCreateUserData(error: Resource<UserModel>) {
        _createUserData.value = error
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

    fun verifyPhoneNumber(
        countryCodeStr: String,
        phoneNumberStr: String,
        action: Int = VerifyCodeSettings.ACTION_REGISTER_LOGIN
    ) {
        setPhoneCodeResult(Resource.loading())
        val settings = VerifyCodeSettings.newBuilder()
            .action(action)
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
            if (it.message!!.contains("203818048")) {
                setPhoneCodeResult(Resource.success(null))
            } else {
                setPhoneCodeResult(Resource.error(it.message))
            }
        }
    }

    fun createAccountWithPHoneNumber(
        countryCode: String,
        phoneNumber: String,
        password: String?,
        otp: String
    ) {
        AGConnectAuth.getInstance().signOut()
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
        password: String?,
        otp: String
    ) {
        setNewAccountWithPhoneResult(Resource.loading())
        val credential =
            PhoneAuthProvider.credentialWithVerifyCode(countryCode, phoneNumber, password, otp)
        Log.d(
            TAG,
            "linkPhoneToHuaweiIDAccount: credential: ${AGConnectAuth.getInstance().currentUser.uid}"
        )
        Log.d(TAG, "linkPhoneToHuaweiIDAccount: credential: ${password}")
        AGConnectAuth.getInstance().currentUser.link(credential)
            .addOnSuccessListener {
                // onSuccess
                setNewAccountWithPhoneResult(Resource.success("updated"))
            }.addOnFailureListener {
                // onFail
                Log.d(TAG, "linkPhoneToHuaweiIDAccount: onFail: ${it.message}")
                setNewAccountWithPhoneResult(Resource.error(it.message))
            }
    }

    fun setNewAccountWithPhoneResult(result: Resource<String>) {
        _newAccountWithPhoneResult.value = result
    }

    fun setPhoneCodeResult(result: Resource<VerifyCodeResult>) {
        _phoneCodeResult.value = result
    }


    fun resetPassword(
        countryCode: String,
        phoneNumber: String,
        newPassword: String,
        verifyCode: String
    ) {
        setResetPasswordResultValue(Resource.loading())
        AGConnectAuth.getInstance().resetPassword(countryCode, phoneNumber, newPassword, verifyCode)
            .addOnSuccessListener {
                // onSuccess
                setResetPasswordResultValue(Resource.success("done"))
            }.addOnFailureListener {
                // onFail
                setResetPasswordResultValue(Resource.error(it.message))
            }
    }

    fun setResetPasswordResultValue(result: Resource<String>) {
        _resetPasswordResult.value = result
    }

    fun signInWithHuaweiId(activity: Activity) {
        AGConnectAuth.getInstance().signOut()
        setSignInWithHuaweiIdResponse(Resource.loading())
        AGConnectAuth.getInstance().signIn(activity, AGConnectAuthCredential.HMS_Provider)
            .addOnSuccessListener {
                Log.d(TAG, "signInWithHuaweiId: success ${it.user.uid}")
                setSignInWithHuaweiIdResponse(Resource.success(it))
            }.addOnFailureListener {
                Log.d(TAG, "signInWithHuaweiId:da ${it.message}")
                Log.d(TAG, "signInWithHuaweiId:fa ${it.localizedMessage}")
                setSignInWithHuaweiIdResponse(Resource.error(it.message))
            }
    }

    private fun setSignInWithHuaweiIdResponse(success: Resource<SignInResult>) {
        _signInWithHuaweiId.value = success
    }

    private fun setUserData(result: Resource<UserModel>) {
        _userData.value = result
    }

    fun setProfileImageLocalPath(path: String) {
        profileImageLocalPath = path
        setImageSelected(true)
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

                    Log.d(TAG, "uploadProfileImage: ${profileImageUrl}}")
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
            profileImageUrl = ""
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
        setUserData(Resource.empty())
        setDataLoading(Resource.empty())
        setCreateUserData(Resource.empty())
        setPhoneCodeResult(Resource.empty())
        setNewAccountWithPhoneResult(Resource.empty())
        setSignInWithHuaweiIdResponse(Resource.empty())
    }

    fun changeLanguage() {


    }

}