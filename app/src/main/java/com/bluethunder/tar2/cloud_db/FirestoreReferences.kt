package com.bluethunder.tar2.cloud_db

enum class FirestoreReferences(val value: String) {

    // region collections references
    UsersCollection("users"),
    CaseCategoriesCollection("case_categories"),
    CasesCollection("cases"),
    CommentsCollection("comments"),
    // endregion

    // region documents references

    // endregion

    // region fields references
    CaseCategoryId("categoryId"),
    CreatedAtField("createdAt"),
    UserIdField("userId");
    // endregion

    fun value(): String {
        return value
    }
}
