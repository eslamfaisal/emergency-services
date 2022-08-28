package com.bluethunder.tar2.ui.home.viewmodel

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bluethunder.tar2.cloud_db.FirestoreReferences
import com.bluethunder.tar2.model.Resource
import com.bluethunder.tar2.ui.case_details.CaseDetailsActivity
import com.bluethunder.tar2.ui.edit_case.model.CaseModel
import com.bluethunder.tar2.ui.home.fragments.CasesListFragment
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch


class MapScreenViewModel : ViewModel() {

    companion object {
        private val TAG = MapScreenViewModel::class.java.simpleName
    }

    private val _myCases = MutableLiveData<Resource<List<DocumentChange>>>()
    val casesList: LiveData<Resource<List<DocumentChange>>> = _myCases


    fun listenToCases() {
        setCasesValue(Resource.loading())
        val query: Query =
            FirebaseFirestore.getInstance().collection(FirestoreReferences.CasesCollection.value())
                .whereEqualTo(FirestoreReferences.CaseDeletedField.value(), false)

        query.addSnapshotListener { querySnapShot, error ->
            try {
                setCasesValue(Resource.success(querySnapShot!!.documentChanges))
            } catch (e: Exception) {
                setCasesValue(Resource.error(e.message!!))
            }
        }
    }

    private fun setCasesValue(success: Resource<List<DocumentChange>>) {
        _myCases.value = success
    }

    fun getCaseDetailsAndOpenIt(context: Activity, caseId: String) {
        FirebaseFirestore.getInstance()
            .collection(FirestoreReferences.CasesCollection.value())
            .document(caseId)
            .get().addOnCompleteListener {
                try {
                    val caseDetails = it.result.toObject(CaseModel::class.java)
                    openCaseDetails(context, caseDetails!!)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
    }

    @Throws(Exception::class)
    private fun openCaseDetails(context: Activity, caseDetails: CaseModel) {
        val intent = Intent(context, CaseDetailsActivity::class.java)
        intent.putExtra(CasesListFragment.EXTRA_CASE_MODEL, caseDetails)
        context.startActivity(intent)
    }
}
