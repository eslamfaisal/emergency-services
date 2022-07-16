/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2020. All rights reserved.
 * Generated by the CloudDB ObjectType compiler.  DO NOT EDIT!
 */
package com.bluethunder.tar2.ui.auth.model

import androidx.annotation.Keep
import com.huawei.agconnect.cloud.database.CloudDBZoneObject
import com.huawei.agconnect.cloud.database.annotations.Indexes
import com.huawei.agconnect.cloud.database.annotations.PrimaryKeys
import java.io.Serializable

@Keep
@PrimaryKeys("id")
@Indexes("id:id", "email:email")
class UserModel : CloudDBZoneObject(UserModel::class.java), Serializable {
    var id: String? = null
    var email: String? = null
    var pushToken: String? = null
    var name: String? = null
    var password: String? = null
    var phone: String? = null
    var imageUrl: String? = null

    override fun toString(): String {
        return "UserModel(id=$id, email=$email, pushToken=$pushToken, name=$name, password=$password, phone=$phone, imageUrl=$imageUrl)"
    }
}