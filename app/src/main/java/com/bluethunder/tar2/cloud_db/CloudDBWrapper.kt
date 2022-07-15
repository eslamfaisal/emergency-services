package com.bluethunder.tar2.cloud_db

import android.content.Context
import android.util.Log
import com.bluethunder.tar2.Tar2Application
import com.bluethunder.tar2.ui.auth.model.ObjectTypeInfoHelper
import com.huawei.agconnect.AGConnectInstance
import com.huawei.agconnect.AGConnectOptionsBuilder
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.cloud.database.AGConnectCloudDB
import com.huawei.agconnect.cloud.database.CloudDBZone
import com.huawei.agconnect.cloud.database.CloudDBZoneConfig
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException

object CloudDBWrapper {
    private const val TAG = "CloudDBWrapper"
    private var mCloudDB: AGConnectCloudDB = AGConnectCloudDB.getInstance()
    var mUsersCloudDBZone: CloudDBZone? = null

    fun setStorageLocation(context: Context?) {
        val builder = AGConnectOptionsBuilder()
            .setRoutePolicy(Tar2Application.regionRoutePolicy)
        val instance = AGConnectInstance.buildInstance(builder.build(context))
        mCloudDB = AGConnectCloudDB.getInstance(instance, AGConnectAuth.getInstance())
    }

    fun createObjectType() {
        try {
            mCloudDB.createObjectType(ObjectTypeInfoHelper.getObjectTypeInfo())
        } catch (e: AGConnectCloudDBException) {
            Log.w(TAG, "createObjectType: " + e.message)
        }
    }

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
            "userss",
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

    fun closConnections() {
        mCloudDB.closeCloudDBZone(mUsersCloudDBZone)
    }

}

