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
