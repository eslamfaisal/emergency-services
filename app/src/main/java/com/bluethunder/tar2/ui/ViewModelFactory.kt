package com.bluethunder.tar2.ui

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.bluethunder.tar2.ui.auth.viewmodel.AuthViewModel
import com.bluethunder.tar2.ui.edit_case.viewmodel.EditCaseViewModel
import com.bluethunder.tar2.ui.home.viewmodel.HomeViewModel
import com.bluethunder.tar2.ui.home.viewmodel.MapScreenViewModel
import com.bluethunder.tar2.ui.home.viewmodel.MyCasesViewModel

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
            isAssignableFrom(AuthViewModel::class.java) ->
                AuthViewModel()
            isAssignableFrom(EditCaseViewModel::class.java) ->
                EditCaseViewModel()
            isAssignableFrom(MyCasesViewModel::class.java) ->
                MyCasesViewModel()
            else ->
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    } as T
}
