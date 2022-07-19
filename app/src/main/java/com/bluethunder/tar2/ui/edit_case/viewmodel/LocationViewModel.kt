package com.bluethunder.tar2.ui.edit_case.viewmodel

import android.location.Location
import android.util.Log
import androidx.lifecycle.*
import com.huawei.hms.location.LocationRequest
import com.patloew.colocation.CoGeocoder
import com.patloew.colocation.CoLocation
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/* Copyright 2020 Patrick Löwenstein
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. */
class LocationViewModel(
    private val coLocation: CoLocation,
    private val coGeocoder: CoGeocoder
) : ViewModel(), LifecycleObserver {

    private val locationRequest: LocationRequest = LocationRequest.create()
        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        .setSmallestDisplacement(1f)
        .setNumUpdates(3)
        .setInterval(5000)
        .setFastestInterval(2500)



    private val mutableLocationUpdates: MutableLiveData<Location> = MutableLiveData()
    val locationUpdates: LiveData<Location> = mutableLocationUpdates

    private val mutableResolveSettingsEvent: MutableLiveData<CoLocation.SettingsResult.Resolvable> =
        MutableLiveData()

    val resolveSettingsEvent: LiveData<CoLocation.SettingsResult.Resolvable> =
        mutableResolveSettingsEvent

    private var locationUpdatesJob: Job? = null

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        startLocationUpdatesAfterCheck()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        locationUpdatesJob?.cancel()
        locationUpdatesJob = null
    }

    fun startLocationUpdatesAfterCheck() {
        viewModelScope.launch {
            when (val settingsResult = coLocation.checkLocationSettings(locationRequest)) {
                CoLocation.SettingsResult.Satisfied -> {
                    coLocation.getLastLocation()?.run(mutableLocationUpdates::postValue)

                }
                is CoLocation.SettingsResult.Resolvable -> mutableResolveSettingsEvent.postValue(
                    settingsResult
                )
                else -> { /* Ignore for now, we can't resolve this anyway */
                    Log.d(TAG, "startLocationUpdatesAfterCheck: Cannot resolve location settings")
                }
            }
        }
    }

    companion object {
        private const val TAG = "LocationViewModel"
    }
}