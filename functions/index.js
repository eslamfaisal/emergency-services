const functions = require("firebase-functions");

exports.myFunction = functions.firestore
  .document('cases/{caseId}')
  .onWrite((change, context) => {

});
