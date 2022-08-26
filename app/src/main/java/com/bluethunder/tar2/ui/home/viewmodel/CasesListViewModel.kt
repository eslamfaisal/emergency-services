package com.bluethunder.tar2.ui.home.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bluethunder.tar2.SessionConstants.currentLoggedInUserModel
import com.bluethunder.tar2.cloud_db.FirestoreReferences
import com.bluethunder.tar2.model.Resource
import com.bluethunder.tar2.ui.edit_case.model.CaseCategoryModel
import com.bluethunder.tar2.ui.edit_case.model.CaseModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


class CasesListViewModel : ViewModel() {

    companion object {
        private val TAG = CasesListViewModel::class.java.simpleName
    }


    private val _dataRefreshLoading = MutableLiveData(false)
    val dataRefreshLoading: LiveData<Boolean> = _dataRefreshLoading

    private val _myCases = MutableLiveData<Resource<ArrayList<CaseModel>>>()
    val casesList: LiveData<Resource<ArrayList<CaseModel>>> = _myCases

    private val _categories = MutableLiveData<Resource<ArrayList<CaseCategoryModel>>>()
    val categories: LiveData<Resource<ArrayList<CaseCategoryModel>>> = _categories

    fun refresh() {
        setCasesValue(Resource.empty())
        setCategoriesValue(Resource.empty())
        getCategories()
    }

    fun getCategories() {
        setCategoriesValue(Resource.loading())
        FirebaseFirestore.getInstance()
            .collection(FirestoreReferences.CaseCategoriesCollection.value())
            .get().addOnSuccessListener { querySnapShot ->
                try {
                    val categoriesList = ArrayList<CaseCategoryModel>()
                    categoriesList.add(CaseCategoryModel("all", "الكل", "All", "ALL", 0))
                    querySnapShot.documents.forEach { document ->
                        categoriesList.add(document.toObject(CaseCategoryModel::class.java)!!)
                    }
                    categoriesList.sortBy { it.priority }
                    setCategoriesValue(Resource.success(categoriesList))
                    getCasesList(categoriesList.first())

                    Log.d(TAG, "caseewModel: get my ce  ${categoriesList.size}")
                } catch (e: Exception) {
                    Log.d(TAG, "caseList: exception $e")
                    setCategoriesValue(Resource.error(e.message!!))
                }
            }.addOnFailureListener {
                Log.d(TAG, "caseList: exception ${it.message!!}")
                setCategoriesValue(Resource.error(it.message!!))
            }
    }

    private fun setCategoriesValue(categoriesResponse: Resource<ArrayList<CaseCategoryModel>>) {
        _categories.value = categoriesResponse
    }

    fun getCasesList(category: CaseCategoryModel) {
        setCasesValue(Resource.empty())
        setCasesValue(Resource.loading())
        var query: Query =
            FirebaseFirestore.getInstance().collection(FirestoreReferences.CasesCollection.value())
//                .whereNotEqualTo(
//                    FirestoreReferences.UserIdField.value(),
//                    currentLoggedInUserModel!!.id
//                )

        if (category.reference != "ALL")
            query = query.whereEqualTo(FirestoreReferences.CaseCategoryId.value(), category.id)

        query.addSnapshotListener { querySnapShot, error ->
            try {
                val casesList = ArrayList<CaseModel>()
                querySnapShot!!.documentChanges.forEach { document ->
                    casesList.add(document.document.toObject(CaseModel::class.java))
                }
                Log.d(TAG, "caseewModel: get my ce  ${casesList.size}")

                casesList.sortByDescending { it.createdAt }
                setCasesValue(Resource.success(casesList))
            } catch (e: Exception) {
                Log.d(TAG, "caseList: exception $e")
                setCasesValue(Resource.error(e.message!!))
            }
        }
    }

    private fun setCasesValue(success: Resource<java.util.ArrayList<CaseModel>>) {
        _dataRefreshLoading.value = false
        _myCases.value = success
    }

}
