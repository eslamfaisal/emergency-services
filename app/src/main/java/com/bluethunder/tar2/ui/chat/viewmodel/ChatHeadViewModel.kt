package com.bluethunder.tar2.ui.chat.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bluethunder.tar2.SessionConstants.currentLoggedInUserModel
import com.bluethunder.tar2.cloud_db.FirestoreReferences
import com.bluethunder.tar2.model.Resource
import com.bluethunder.tar2.ui.chat.model.ChatHead
import com.bluethunder.tar2.ui.edit_case.model.CaseCategoryModel
import com.bluethunder.tar2.ui.edit_case.model.CaseModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


class ChatHeadViewModel : ViewModel() {

    companion object {
        private val TAG = ChatHeadViewModel::class.java.simpleName
    }


    private val _dataRefreshLoading = MutableLiveData(false)
    val dataRefreshLoading: LiveData<Boolean> = _dataRefreshLoading

    private val _chatHeads = MutableLiveData<Resource<ArrayList<ChatHead>>>()
    val chatHeads: LiveData<Resource<ArrayList<ChatHead>>> = _chatHeads

    fun refresh() {
        setCasesValue(Resource.empty())
        getChatHeads()
    }

    fun getChatHeads() {
        setCasesValue(Resource.empty())
        setCasesValue(Resource.loading())
        val query: Query =
            FirebaseFirestore.getInstance().collection(FirestoreReferences.ChatHeadsCollection.value())
                .whereArrayContains(
                    FirestoreReferences.UsersField.value(),
                    currentLoggedInUserModel!!.id
                )

        query.addSnapshotListener { querySnapShot, error ->
            try {
                val casesList = ArrayList<ChatHead>()
                querySnapShot!!.documentChanges.forEach { document ->
                    casesList.add(document.document.toObject(ChatHead::class.java))
                }
                Log.d(TAG, "caseewModel: get my ce  ${casesList.size}")

                setCasesValue(Resource.success(casesList))
            } catch (e: Exception) {
                Log.d(TAG, "caseList: exception $e")
                setCasesValue(Resource.error(e.message!!))
            }
        }
    }

    private fun setCasesValue(success: Resource<ArrayList<ChatHead>>) {
        _dataRefreshLoading.value = false
        _chatHeads.value = success
    }

}
