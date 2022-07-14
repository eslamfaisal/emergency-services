package com.bluethunder.tar2.cloud_db

import android.util.Log
import com.huawei.agconnect.cloud.database.AGConnectCloudDB
import com.huawei.agconnect.cloud.database.CloudDBZone
import com.huawei.agconnect.cloud.database.CloudDBZoneConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch

object CloudDBWrapper {
    private const val TAG = "CloudDBWrapper"
    private var mCloudDB: AGConnectCloudDB = AGConnectCloudDB.getInstance()
    var mUsersCloudDBZone: CloudDBZone? = null

//    fun openUsersCloudDBZoneV2(complete: (Boolean) -> Unit) {
//        CoroutineScope(Main).launch {
//            try {
//                val mUsersConfig = CloudDBZoneConfig(
//                    "users",
//                    CloudDBZoneConfig.CloudDBZoneSyncProperty.CLOUDDBZONE_CLOUD_CACHE,
//                    CloudDBZoneConfig.CloudDBZoneAccessProperty.CLOUDDBZONE_PUBLIC
//                )
//                mUsersConfig.persistenceEnabled = true
//                val usersTask = mCloudDB.openCloudDBZone2(mUsersConfig, true)
//
//                if (usersTask.isSuccessful) {
//                    Log.d(TAG, "openUsersCloudDBZoneV2: success")
//                    mUsersCloudDBZone = usersTask.result
//                } else {
//                    complete(false)
//                    Log.d(TAG, "openUsersCloudDBZoneV2: ${usersTask.exception?.message}")
//                    return@launch
//                }
//
//                Log.d(TAG, "openUsersCloudDBZoneV2: finish open")
//                // finish the task
//                complete(true)
//            } catch (e: Exception) {
//                Log.d(TAG, "openUsersCloudDBZoneV2: " + e.message)
//                complete(false)
//                e.printStackTrace()
//            }
//        }
//    }

    fun openUsersCloudDBZoneV2(complete: (Boolean) -> Unit) {
        val mUsersConfig = CloudDBZoneConfig(
            "users",
            CloudDBZoneConfig.CloudDBZoneSyncProperty.CLOUDDBZONE_CLOUD_CACHE,
            CloudDBZoneConfig.CloudDBZoneAccessProperty.CLOUDDBZONE_PUBLIC
        )
        mUsersConfig.persistenceEnabled = true
        val usersTask = mCloudDB.openCloudDBZone2(mUsersConfig, true)
        usersTask.addOnCompleteListener {
            if (it.isSuccessful) {
                Log.d(TAG, "openUsersCloudDBZoneV2: success")
                mUsersCloudDBZone = it.result
            } else {
                complete(false)
                Log.d(TAG, "openUsersCloudDBZoneV2: ${it.exception?.message}")
                return@addOnCompleteListener
            }

            Log.d(TAG, "openUsersCloudDBZoneV2: finish open")
            // finish the task
            complete(true)
        }

    }

    fun closConnections(){
        mCloudDB.closeCloudDBZone(mUsersCloudDBZone)
    }

}

