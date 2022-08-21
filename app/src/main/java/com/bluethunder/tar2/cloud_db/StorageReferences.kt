package com.bluethunder.tar2.cloud_db

enum class StorageReferences(val value: String) {

    ChatImagesFolder("ChatImages");

    fun value(): String {
        return value
    }
}
