const functions = require("firebase-functions");
// The Firebase Admin SDK to access Firestore.
const admin = require("firebase-admin");
admin.initializeApp();
const db = admin.firestore();

exports.caseUpVotesCount = functions.firestore
    .document('cases/{caseId}/up_vote_users/{userId}')
    .onCreate((snap, context) => {
        const caseId = snap.data().caseId;
        return db.collection("cases").doc(caseId)
            .get().then((document) => {
                const newCount = document.data().upVotesCount += 1;
                db.collection("cases").doc(caseId)
                    .update({"upVotesCount": newCount})
                    .then((ds) => {
                        console.log("document updated succ");
                    });
            });
    });

exports.caseViewsCount = functions.firestore
    .document('cases/{caseId}/views/{userId}')
    .onCreate((snap, context) => {
        const caseId = snap.data().caseId;
        return db.collection("cases").doc(caseId)
            .get().then((document) => {
                const newCount = document.data().viewsCount += 1;
                db.collection("cases").doc(caseId)
                    .update({"viewsCount": newCount})
                    .then((ds) => {
                        console.log("document updated succ");
                    });
            });
    });

exports.caseCommentsCount = functions.firestore
    .document('cases/{caseId}/comments/{commentID}')
    .onCreate((snap, context) => {
        const caseId = snap.data().caseId;
        return db.collection("cases").doc(caseId)
            .get().then((document) => {
                const newCount = document.data().commentsCount += 1;
                db.collection("cases").doc(caseId)
                    .update({"commentsCount": newCount})
                    .then((ds) => {
                        console.log("document updated succ");
                    });
            });
    });

function updateUSerCount(userID, increase) {
    return db.collection("users").doc(userID)
        .get().then((document) => {
            let newCount;
            if (increase) {
                newCount = document.data().unReadChatCount += 1;
            } else {
                newCount = 0;
            }
            db.collection("users").doc(userID)
                .update({"unReadChatCount": newCount})
                .then((ds) => {
                    console.log("document updated succ");
                });
        });
}

exports.newMessageCound = functions.firestore
    .document('chat_heads/{chat_heads_id}')
    .onWrite((snap, context) => {
        const users = snap.before.data().users;
        const unReadCount = snap.before.data().unReadCount;
        const firstMessageSenderID = snap.before.data().lastMessageSenderID;
        const lastMessageSenderID = snap.after.data().lastMessageSenderID;

        if (firstMessageSenderID === lastMessageSenderID) {
            let anotherUserId;
            if (firstMessageSenderID === users[0]) {
                anotherUserId = users[0];
            } else {
                anotherUserId = users[1];
            }
            return updateUSerCount(anotherUserId, true).then(
                (document) => {
                    console.log("document updated succ");
                    return updateUSerCount(firstMessageSenderID, false).then(
                        (document) => {
                            console.log("document updated succ");
                        }
                    );
                }
            );
        } else {
            return db.collection("users").doc(lastMessageSenderID)
                .get().then((document) => {
                    const newCount = document.data().unReadChatCount += 1;
                    return db.collection("users").doc(lastMessageSenderID)
                        .update({"unReadChatCount": newCount})
                        .then((ds) => {
                            console.log("document updated succ");
                        });
                });
        }


    });
