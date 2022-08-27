package com.bluethunder.tar2.cloud_db

enum class FirestoreReferences(val value: String) {

    // region collections references
    UsersCollection("users"),
    CaseCategoriesCollection("case_categories"),
    CasesCollection("cases"),
    CommentsCollection("comments"),
    ChatHeadsCollection("chat_heads"),
    MessagesCollection("messages"),
    // endregion

    // region documents references

    // endregion

    // region fields references
    CaseCategoryId("categoryId"),
    CreatedAtField("createdAt"),
    LastMessageField("lastMessage"),
    LastMessageAtField("lastMessageAt"),
    DateField("date"),
    UsersField("users"),
    IsDeletedField("isDeleted"),
    UserIdField("userId");
    // endregion

    fun value(): String {
        return value
    }
}
