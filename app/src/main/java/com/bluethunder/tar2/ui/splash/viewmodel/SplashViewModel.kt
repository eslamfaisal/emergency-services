package com.bluethunder.tar2.ui.splash.viewmodel

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.ViewModel
import com.bluethunder.tar2.cloud_db.FirestoreReferences
import com.bluethunder.tar2.ui.case_details.CaseDetailsActivity
import com.bluethunder.tar2.ui.edit_case.model.CaseModel
import com.bluethunder.tar2.ui.home.fragments.CasesListFragment
import com.google.firebase.firestore.FirebaseFirestore


class SplashViewModel : ViewModel() {

    companion object {
        private val TAG = SplashViewModel::class.java.simpleName
    }

    fun getCaseDetailsAndOpenIt(context: Activity, caseId: String) {
        FirebaseFirestore.getInstance()
            .collection(FirestoreReferences.CasesCollection.value())
            .document(caseId)
            .get().addOnCompleteListener {
                try {
                    val caseDetails = it.result.toObject(CaseModel::class.java)
                    openCaseDetails(context, caseDetails!!)
                    context.finish()
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
