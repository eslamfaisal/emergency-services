package com.bluethunder.tar2.ui

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.bluethunder.tar2.ui.auth.viewmodel.AuthViewModel
import com.bluethunder.tar2.ui.case_details.viewmodel.CaseDetailsViewModel
import com.bluethunder.tar2.ui.chat.viewmodel.ChatHeadViewModel
import com.bluethunder.tar2.ui.edit_case.viewmodel.EditCaseViewModel
import com.bluethunder.tar2.ui.home.viewmodel.*
import com.bluethunder.tar2.ui.profile.viewmodel.ChangePasswordViewModel
import com.bluethunder.tar2.ui.profile.viewmodel.ProfileViewModel
import com.bluethunder.tar2.ui.splash.viewmodel.SplashViewModel

/**
 * Factory for all ViewModels.
 */
@Suppress("UNCHECKED_CAST")
class ViewModelFactory constructor(
    private val context: Context,
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {

    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ) = with(modelClass) {
        when {
            isAssignableFrom(MapScreenViewModel::class.java) ->
                MapScreenViewModel()
            isAssignableFrom(HomeViewModel::class.java) ->
                HomeViewModel()
            isAssignableFrom(NotificationsViewModel::class.java) ->
                NotificationsViewModel(context)
            isAssignableFrom(AuthViewModel::class.java) ->
                AuthViewModel()
            isAssignableFrom(EditCaseViewModel::class.java) ->
                EditCaseViewModel()
            isAssignableFrom(MyCasesViewModel::class.java) ->
                MyCasesViewModel()
            isAssignableFrom(CasesListViewModel::class.java) ->
                CasesListViewModel()
            isAssignableFrom(CaseDetailsViewModel::class.java) ->
                CaseDetailsViewModel()
            isAssignableFrom(MyLocationViewModel::class.java) ->
                MyLocationViewModel()
            isAssignableFrom(SplashViewModel::class.java) ->
                SplashViewModel()
            isAssignableFrom(ChatHeadViewModel::class.java) ->
                ChatHeadViewModel()
            isAssignableFrom(ProfileViewModel::class.java) ->
                ProfileViewModel()
            isAssignableFrom(ChangePasswordViewModel::class.java) ->
                ChangePasswordViewModel()
            else ->
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    } as T
}
