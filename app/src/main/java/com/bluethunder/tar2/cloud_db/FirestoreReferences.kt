package com.bluethunder.tar2.cloud_db

enum class FirestoreReferences(val value: String) {

    // region collections references
    UsersCollection("users"),
    CaseCategoriesCollection("case_categories");
    // endregion

    // region documents references

    // endregion

    // region fields references

    // endregion

    fun value(): String {
        return value
    }
}
