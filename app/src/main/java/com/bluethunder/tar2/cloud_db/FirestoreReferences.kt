package com.bluethunder.tar2.cloud_db

enum class FirestoreReferences(val value: String) {

    // region collections references
    UsersCollection("users"),
    CaseCategoriesCollection("case_categories"),
    CasesCollection("cases"),
    // endregion

    // region documents references

    // endregion

    // region fields references
    UserIdField("categoryId"),
    CreatedAtField("createdAt"),
    CaseCategoryId("userId");
    // endregion

    fun value(): String {
        return value
    }
}
