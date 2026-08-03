const {initializeTestEnvironment, assertSucceeds, assertFails} = require("@firebase/rules-unit-testing");
const fs = require("fs");

describe("Docta Firestore rules", () => {
  let env;
  beforeAll(async () => {
    env = await initializeTestEnvironment({
      projectId: "docta-test",
      firestore: {rules: fs.readFileSync("../firestore.rules", "utf8")}
    });
  });
  afterAll(() => env.cleanup());

  test("patient cannot read another user profile", async () => {
    const db = env.authenticatedContext("patient-a").firestore();
    await assertFails(db.doc("users/patient-b").get());
  });

  test("patient can create own pending appointment", async () => {
    await env.withSecurityRulesDisabled(async context => {
      await context.firestore().doc("users/patient-a").set({uid: "patient-a", role: "patient"});
      await context.firestore().doc("doctors/doctor-a").set({userId: "doctor-a"});
    });
    const db = env.authenticatedContext("patient-a").firestore();
    await assertSucceeds(db.doc("appointments/a1").set({patientId: "patient-a", patientName: "Patient", doctorId: "doctor-a", doctorName: "Docteur", status: "pending"}));
  });

  test("conversation with identical participants is rejected", async () => {
    const db = env.authenticatedContext("patient-a").firestore();
    await assertFails(db.doc("conversations/self").set({participantIds: ["patient-a", "patient-a"], participantNames: ["A", "A"]}));
  });
});
