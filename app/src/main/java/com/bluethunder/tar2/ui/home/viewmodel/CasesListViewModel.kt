package com.bluethunder.tar2.ui.home.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bluethunder.tar2.SessionConstants
import com.bluethunder.tar2.cloud_db.FirestoreReferences
import com.bluethunder.tar2.model.Resource
import com.bluethunder.tar2.ui.edit_case.model.CaseCategoryModel
import com.bluethunder.tar2.ui.edit_case.model.CaseModel
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot


class CasesListViewModel : ViewModel() {

    companion object {
        private val TAG = CasesListViewModel::class.java.simpleName
    }


    private val _dataRefreshLoading = MutableLiveData(false)
    val dataRefreshLoading: LiveData<Boolean> = _dataRefreshLoading

    private val _addedCases = MutableLiveData<Resource<MutableList<CaseModel>>>()
    val addedCasesList: LiveData<Resource<MutableList<CaseModel>>> = _addedCases

    private val _deletedCases = MutableLiveData<Resource<MutableList<CaseModel>>>()
    val deletedCases: LiveData<Resource<MutableList<CaseModel>>> = _deletedCases

    private val _categories = MutableLiveData<Resource<ArrayList<CaseCategoryModel>>>()
    val categories: LiveData<Resource<ArrayList<CaseCategoryModel>>> = _categories

    fun refresh() {
        setAddedCasesValue(Resource.empty())
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
                    SessionConstants.enabledCategories?.let { enabledReeferences ->
                        val configList = ArrayList<CaseCategoryModel>()
                        categoriesList.forEach { categoryModel ->
                            if (enabledReeferences.contains(categoryModel.reference)) {
                                configList.add(categoryModel)
                            }
                        }
                        setCategoriesValue(Resource.success(configList))
                        getCasesList(configList.first())
                    } ?: kotlin.run {
                        setCategoriesValue(Resource.success(categoriesList))
                        getCasesList(categoriesList.first())
                    }
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
        setAddedCasesValue(Resource.empty())
        setAddedCasesValue(Resource.loading())
        var query: Query =
            FirebaseFirestore.getInstance().collection(FirestoreReferences.CasesCollection.value())
                .whereEqualTo(FirestoreReferences.CaseDeletedField.value(), false)
//                .whereNotEqualTo(
//                    FirestoreReferences.UserIdField.value(),
//                    currentLoggedInUserModel!!.id
//                )

        if (category.reference != "ALL")
            query = query.whereEqualTo(FirestoreReferences.CaseCategoryId.value(), category.id)

        query.addSnapshotListener { querySnapShot, error ->
            try {
                handleAddedList(querySnapShot)
                handleDeletedList(querySnapShot)
            } catch (e: Exception) {
                Log.d(TAG, "caseList: exception $e")
                setAddedCasesValue(Resource.error(e.message!!))
            }
        }
    }

    private fun handleAddedList(querySnapShot: QuerySnapshot?) {
        if (querySnapShot == null) return
        val casesList = ArrayList<CaseModel>()
        val addedList =
            querySnapShot.documentChanges.filter {
                it.type == DocumentChange.Type.ADDED ||
                        it.type == DocumentChange.Type.MODIFIED
            }
        addedList.forEach { document ->
            casesList.add(document.document.toObject(CaseModel::class.java))
        }
        setAddedCasesValue(Resource.success(casesList))
        setDeletedCasesValue(Resource.success(casesList.filter { it.caseDeleted }.toMutableList()))
    }

    private fun handleDeletedList(querySnapShot: QuerySnapshot?) {
        if (querySnapShot == null) return
        val casesList = ArrayList<CaseModel>()
        val addedList =
            querySnapShot.documentChanges.filter {
                it.type == DocumentChange.Type.REMOVED
            }
        addedList.forEach { document ->
            casesList.add(document.document.toObject(CaseModel::class.java))
        }
        setDeletedCasesValue(Resource.success(casesList))
    }

    private fun setAddedCasesValue(success: Resource<MutableList<CaseModel>>) {
        _dataRefreshLoading.value = false
        _addedCases.value = success
    }

    private fun setDeletedCasesValue(success: Resource<MutableList<CaseModel>>) {
        _deletedCases.value = success
    }

}
