const admin = require("firebase-admin");

// Initialize Firebase Admin SDK
const serviceAccount = require("./serviceAccountKey.json"); // Download from Firebase Console
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
});

async function deleteUnverifiedUsers() {
  try {
    const listUsersResult = await admin.auth().listUsers();
    const users = listUsersResult.users;

    const unverifiedUsers = users.filter((user) => !user.emailVerified);

    if (unverifiedUsers.length > 0) {
      await admin.auth().deleteUsers(unverifiedUsers.map((user) => user.uid));
      console.log(`Deleted ${unverifiedUsers.length} unverified users.`);
    } else {
      console.log("No unverified users found.");
    }
  } catch (error) {
    console.error("Error deleting users:", error);
  }
}

deleteUnverifiedUsers();