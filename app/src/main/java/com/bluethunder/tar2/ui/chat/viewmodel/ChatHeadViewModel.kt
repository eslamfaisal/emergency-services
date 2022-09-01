package com.bluethunder.tar2.ui.chat.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bluethunder.tar2.SessionConstants.currentLoggedInUserModel
import com.bluethunder.tar2.cloud_db.FirestoreReferences
import com.bluethunder.tar2.model.Resource
import com.bluethunder.tar2.ui.chat.model.ChatHead
import com.bluethunder.tar2.ui.edit_case.model.CaseModel
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlin.jvm.Throws


class ChatHeadViewModel : ViewModel() {

    companion object {
        private val TAG = ChatHeadViewModel::class.java.simpleName
    }


    private val _dataRefreshLoading = MutableLiveData(false)
    val dataRefreshLoading: LiveData<Boolean> = _dataRefreshLoading

    private val _addedChatHeads = MutableLiveData<Resource<ArrayList<ChatHead>>>()
    val addedChatHeads: LiveData<Resource<ArrayList<ChatHead>>> = _addedChatHeads

    private val _modifiedChatHeads = MutableLiveData<Resource<ArrayList<ChatHead>>>()
    val modifiedChatHeads: LiveData<Resource<ArrayList<ChatHead>>> = _modifiedChatHeads

    fun refresh() {
        setCasesValue(Resource.empty())
        getChatHeads()
    }

    fun getChatHeads() {
        setCasesValue(Resource.loading())
        val query: Query =
            FirebaseFirestore.getInstance()
                .collection(FirestoreReferences.ChatHeadsCollection.value())
                .whereArrayContains(
                    FirestoreReferences.UsersField.value(),
                    currentLoggedInUserModel!!.id
                )

        query.addSnapshotListener { querySnapShot, error ->
            try {

                handleAddedList(querySnapShot)
                handleModifiedList(querySnapShot)

            } catch (e: Exception) {
                Log.d(TAG, "caseList: exception $e")
                setCasesValue(Resource.error(e.message!!))
            }
        }
    }

    @Throws(Exception::class)
    private fun handleAddedList(querySnapShot: QuerySnapshot?) {
        querySnapShot?.let {
            val casesList = ArrayList<ChatHead>()
            val addedList =
                querySnapShot.documentChanges.filter {
                    it.type == DocumentChange.Type.ADDED
                }
            addedList.forEach { document ->
                casesList.add(document.document.toObject(ChatHead::class.java))
            }
            setCasesValue(Resource.success(casesList))
        }
    }

    @Throws(Exception::class)
    private fun handleModifiedList(querySnapShot: QuerySnapshot?) {
        querySnapShot?.let {
            val casesList = ArrayList<ChatHead>()
            val addedList =
                querySnapShot.documentChanges.filter {
                    it.type == DocumentChange.Type.MODIFIED
                }
            addedList.forEach { document ->
                casesList.add(document.document.toObject(ChatHead::class.java))
            }
            setCasesValue(Resource.success(casesList))
        }
    }

    private fun setCasesValue(success: Resource<ArrayList<ChatHead>>) {
        _dataRefreshLoading.value = false
        _addedChatHeads.value = success
    }

    private fun setModifiedCasesValue(success: Resource<ArrayList<ChatHead>>) {
        _modifiedChatHeads.value = success
    }

}
