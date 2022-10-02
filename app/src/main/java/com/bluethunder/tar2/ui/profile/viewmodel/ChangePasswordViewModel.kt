package com.bluethunder.tar2.ui.profile.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bluethunder.tar2.model.Resource
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.auth.VerifyCodeResult
import com.huawei.agconnect.auth.VerifyCodeSettings
import java.util.*

class ChangePasswordViewModel : ViewModel() {

    companion object {
        private val TAG = ChangePasswordViewModel::class.java.simpleName
    }

    private val _resetPasswordResult = MutableLiveData<Resource<String>>()
    val resetPasswordResult: LiveData<Resource<String>> = _resetPasswordResult

    private val _phoneCodeResult = MutableLiveData<Resource<VerifyCodeResult>>()
    val phoneCodeResult: LiveData<Resource<VerifyCodeResult>> = _phoneCodeResult


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

}