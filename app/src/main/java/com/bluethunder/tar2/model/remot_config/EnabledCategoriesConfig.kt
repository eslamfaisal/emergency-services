package com.bluethunder.tar2.model.remot_config

import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose
import org.greenrobot.greendao.annotation.Keep
import java.io.Serializable

@Keep
class EnabledCategoriesConfig : Serializable {
    @SerializedName("categories")
    @Expose
    var categories: List<String>? = null
}