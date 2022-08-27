package com.bluethunder.tar2.ui.case_details.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluethunder.tar2.SessionConstants
import com.bluethunder.tar2.cloud_db.FirestoreReferences
import com.bluethunder.tar2.model.Resource
import com.bluethunder.tar2.networking.RetrofitClient
import com.bluethunder.tar2.ui.auth.model.UserModel
import com.bluethunder.tar2.ui.case_details.model.*
import com.bluethunder.tar2.ui.edit_case.model.CaseCategoryModel
import com.bluethunder.tar2.ui.edit_case.model.CaseModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class CaseDetailsViewModel : ViewModel() {

    companion object {
        private val TAG = CaseDetailsViewModel::class.java.simpleName
    }

    private val _onSelectedTabIndex = MutableLiveData(0)
    val onSelectedTabIndex: LiveData<Int> = _onSelectedTabIndex

    private val _caseCategory = MutableLiveData<CaseCategoryModel?>()
    val caseCategory: LiveData<CaseCategoryModel?> = _caseCategory

    private val _currentCaseDetails = MutableLiveData<CaseModel?>()
    val currentCaseDetails: LiveData<CaseModel?> = _currentCaseDetails

    private val _currentCaseUserDetails = MutableLiveData<UserModel?>()
    val currentCaseUserDetails: LiveData<UserModel?> = _currentCaseUserDetails

    private val _caseLocationDistance = MutableLiveData<Resource<LocationDistanceModel?>>()
    val caseLocationDistance: LiveData<Resource<LocationDistanceModel?>> = _caseLocationDistance

    private val _dataLoading = MutableLiveData(false)
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _commentsList = MutableLiveData<Resource<List<CommentModel>>>()
    val commentsList: LiveData<Resource<List<CommentModel>>> = _commentsList

    fun refresh() {
        _dataLoading.value = true
        viewModelScope.launch {
            _dataLoading.value = false
        }
    }

    fun setOnMapSelected(index: Int) {
        Log.d(TAG, "setOnMapSelected: $index")
        viewModelScope.launch {
            _onSelectedTabIndex.value = index
        }
    }

    fun listenToComments(caseId: String) {
        FirebaseFirestore.getInstance().collection(FirestoreReferences.CasesCollection.value())
            .document(caseId)
            .collection(FirestoreReferences.CommentsCollection.value())
            .orderBy(FirestoreReferences.CreatedAtField.value(), Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e(TAG, "listenToComments: ", error)
                    setCommentsValue(Resource.error(error.message))
                } else {
                    val list: MutableList<CommentModel> = ArrayList()
                    value!!.documentChanges.forEach { document ->
                        val comment = document.document.toObject(CommentModel::class.java)
                        list.add(comment)
                    }
                    setCommentsValue(Resource.success(list))
                }
            }
    }

    fun listenToCaseDetails(caseId: String) {
        FirebaseFirestore.getInstance()
            .collection(FirestoreReferences.CasesCollection.value())
            .document(caseId)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e(TAG, "listenToComments: ", error)
                } else {
                    val case = value!!.toObject(CaseModel::class.java)
                    _currentCaseDetails.value = case
                }
            }
    }

    fun listenToCaseUserDetails(caseId: String) {
        FirebaseFirestore.getInstance()
            .collection(FirestoreReferences.UsersCollection.value())
            .document(caseId)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e(TAG, "listenToComments: ", error)
                } else {
                    val case = value!!.toObject(UserModel::class.java)
                    _currentCaseUserDetails.value = case
                }
            }
    }

    fun getCaseCategory(categoryId: String) {
        FirebaseFirestore.getInstance()
            .collection(FirestoreReferences.CaseCategoriesCollection.value())
            .document(categoryId)
            .get().addOnCompleteListener {
                try {
                    if (it.isSuccessful) {
                        val category = it.result.toObject(CaseCategoryModel::class.java)
                        _caseCategory.value = category
                    } else {
                        Log.e(TAG, "getCaseCategory: ", it.exception)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "getCaseCategory: ", e)
                }

            }
    }

    fun setCommentsValue(value: Resource<List<CommentModel>>) {
        _commentsList.value = value
    }

    fun sendComment(caseId: String, comment: CommentModel) {
        FirebaseFirestore.getInstance().collection("cases")
            .document(caseId)
            .collection("comments")
            .add(comment)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d(TAG, "sendComment: success")
                } else {
                    Log.e(TAG, "sendComment: ", it.exception)
                }
            }
    }

    fun sendUpVote(caseId: String) {
        FirebaseFirestore.getInstance().collection("cases")
            .document(caseId)
            .collection("up_vote_users")
            .document(SessionConstants.currentLoggedInUserModel!!.id)
            .set(
                hashMapOf(
                    "userId" to SessionConstants.currentLoggedInUserModel!!.id,
                    "caseId" to caseId
                )
            )
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d(TAG, "sendUpVote: success")
                } else {
                    Log.e(TAG, "sendUpVote: ", it.exception)
                }
            }
    }

    fun sendView(caseId: String) {
        FirebaseFirestore.getInstance().collection("cases")
            .document(caseId)
            .collection("views")
            .document(SessionConstants.currentLoggedInUserModel!!.id)
            .set(
                hashMapOf(
                    "userId" to SessionConstants.currentLoggedInUserModel!!.id,
                    "caseId" to caseId
                )
            )
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d(TAG, "sendUpVote: success")
                } else {
                    Log.e(TAG, "sendUpVote: ", it.exception)
                }
            }
    }

    fun getCaseLocationDistance(latitude: Double, longitude: Double) {
        setCaseLocationDistance(Resource.loading())
        val body = LocationDistanceRequestBody(
            origin = Origin(
                lat = SessionConstants.myCurrentLocation!!.latitude,
                lng = SessionConstants.myCurrentLocation!!.longitude,
            ),
            destination = Destination(
                lat = latitude,
                lng = longitude,
            )
        )
        RetrofitClient.retrofitMap.getCaseDistance(body)
            .enqueue(object : Callback<LocationDistanceModel> {
                override fun onResponse(
                    call: Call<LocationDistanceModel>,
                    response: Response<LocationDistanceModel>
                ) {
                    if (response.isSuccessful) {
                        setCaseLocationDistance(Resource.success(response.body()))
                        Log.d(TAG, "getCaseLocationDistance: success")
                    } else {
                        Log.e(TAG, "getCaseLocationDistance:  err ")
                        setCaseLocationDistance(Resource.error(response.message()))
                    }
                }

                override fun onFailure(call: Call<LocationDistanceModel>, t: Throwable) {
                    Log.e(TAG, "getCaseLocationDistance: ", t)
                    setCaseLocationDistance(Resource.error(t.message))
                }
            })
    }

    fun setCaseLocationDistance(value: Resource<LocationDistanceModel?>) {
        _caseLocationDistance.value = value
    }

    fun deleteCase() {
        Log.d(TAG, "deleteCase: ${currentCaseDetails.value!!.id!!}")
        FirebaseFirestore.getInstance().collection("cases")
            .document(currentCaseDetails.value!!.id!!).update(
                mapOf(
                    FirestoreReferences.IsDeletedField.value() to true
                )
            ).addOnCompleteListener {
                Log.d(TAG, "deleteCase: ")
            }
    }

    fun reportCase() {
        FirebaseFirestore.getInstance().collection("reports")
            .document(currentCaseDetails.value!!.id!!)
            .set(
                mapOf(
                    "caseID" to currentCaseDetails.value!!.id!!,
                    "userId" to SessionConstants.currentLoggedInUserModel!!.id!!
                )
            ).addOnCompleteListener { }
    }


}
