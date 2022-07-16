package com.bluethunder.tar2.cloud_db

import android.content.Context
import com.huawei.agconnect.AGCRoutePolicy
import com.huawei.agconnect.AGConnectInstance
import com.huawei.agconnect.AGConnectOptionsBuilder
import com.huawei.agconnect.cloud.storage.core.AGCStorageManagement

object CloudStorageWrapper {

    lateinit var storageManagement: AGCStorageManagement
    fun initStorage(contexts: Context) {
        val cnOptions =
            AGConnectOptionsBuilder().setRoutePolicy(AGCRoutePolicy.SINGAPORE).build(contexts)
        val cnInstance = AGConnectInstance.buildInstance(cnOptions)
        storageManagement =
            AGCStorageManagement.getInstance(cnInstance, "tar2-storage-instance-74bao")
    }
}

