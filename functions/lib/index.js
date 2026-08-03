"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.prescriptionCreated = exports.messageCreated = exports.appointmentUpdated = exports.appointmentCreated = exports.getPrescriptionDownloadUrl = void 0;
const app_1 = require("firebase-admin/app");
const firestore_1 = require("firebase-admin/firestore");
const messaging_1 = require("firebase-admin/messaging");
const firestore_2 = require("firebase-functions/v2/firestore");
const https_1 = require("firebase-functions/v2/https");
const params_1 = require("firebase-functions/params");
const pdfkit_1 = __importDefault(require("pdfkit"));
const crypto_1 = require("crypto");
const path_1 = __importDefault(require("path"));
(0, app_1.initializeApp)();
const db = (0, firestore_1.getFirestore)();
const supabaseUrlSecret = (0, params_1.defineSecret)("SUPABASE_URL");
const supabaseServiceRoleSecret = (0, params_1.defineSecret)("SUPABASE_SERVICE_ROLE_KEY");
const supabaseBucketSecret = (0, params_1.defineSecret)("SUPABASE_BUCKET_NAME");
async function uploadPdfToSupabase(storagePath, buffer, reference) {
    const supabaseUrl = supabaseUrlSecret.value();
    const supabaseServiceRoleKey = supabaseServiceRoleSecret.value();
    const supabaseBucket = supabaseBucketSecret.value() || "docta-na-tshombo";
    if (!supabaseUrl || !supabaseServiceRoleKey)
        throw new Error("Secrets Supabase manquants pour générer le PDF");
    const response = await fetch(`${supabaseUrl}/storage/v1/object/${supabaseBucket}/${storagePath}`, {
        method: "POST",
        headers: {
            apikey: supabaseServiceRoleKey,
            Authorization: `Bearer ${supabaseServiceRoleKey}`,
            "Content-Type": "application/pdf",
            "x-upsert": "true",
            "Content-Disposition": `attachment; filename=\"${reference}.pdf\"`
        },
        body: new Uint8Array(buffer)
    });
    if (!response.ok)
        throw new Error(`Supabase Storage PDF upload failed (${response.status})`);
}
async function createSupabaseSignedUrl(storagePath) {
    const supabaseUrl = supabaseUrlSecret.value();
    const supabaseServiceRoleKey = supabaseServiceRoleSecret.value();
    const supabaseBucket = supabaseBucketSecret.value() || "docta-na-tshombo";
    if (!supabaseUrl || !supabaseServiceRoleKey)
        throw new Error("Secrets Supabase manquants");
    const response = await fetch(`${supabaseUrl}/storage/v1/object/sign/${supabaseBucket}/${storagePath}`, {
        method: "POST",
        headers: {
            apikey: supabaseServiceRoleKey,
            Authorization: `Bearer ${supabaseServiceRoleKey}`,
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ expiresIn: 300 })
    });
    if (!response.ok)
        throw new Error(`Supabase signed URL failed (${response.status})`);
    const data = await response.json();
    const signedPath = data.signedURL || data.signedUrl;
    if (!signedPath)
        throw new Error("Supabase n'a pas retourné d'URL signée");
    return signedPath.startsWith("http") ? signedPath : `${supabaseUrl}/storage/v1${signedPath}`;
}
exports.getPrescriptionDownloadUrl = (0, https_1.onCall)({
    secrets: [supabaseUrlSecret, supabaseServiceRoleSecret, supabaseBucketSecret]
}, async (request) => {
    const uid = request.auth?.uid;
    if (!uid)
        throw new https_1.HttpsError("unauthenticated", "Connexion requise");
    const prescriptionId = String(request.data?.prescriptionId || "");
    if (!prescriptionId)
        throw new https_1.HttpsError("invalid-argument", "Identifiant ordonnance requis");
    const snapshot = await db.doc(`prescriptions/${prescriptionId}`).get();
    if (!snapshot.exists)
        throw new https_1.HttpsError("not-found", "Ordonnance introuvable");
    const prescription = snapshot.data();
    if (uid !== prescription.patientId && uid !== prescription.doctorId) {
        throw new https_1.HttpsError("permission-denied", "Accès refusé");
    }
    if (!prescription.pdfPath)
        throw new https_1.HttpsError("failed-precondition", "PDF en cours de génération");
    return { url: await createSupabaseSignedUrl(prescription.pdfPath) };
});
async function notifyUser(userId, title, body, data) {
    const user = await db.doc(`users/${userId}`).get();
    const token = user.get("fcmToken");
    await db.collection("notifications").add({ userId, title, body, data, isRead: false, createdAt: firestore_1.FieldValue.serverTimestamp() });
    if (token)
        await (0, messaging_1.getMessaging)().send({ token, notification: { title, body }, data });
}
exports.appointmentCreated = (0, firestore_2.onDocumentCreated)("appointments/{appointmentId}", async (event) => {
    const appointment = event.data?.data();
    if (!appointment)
        return;
    await notifyUser(appointment.doctorId, "Nouveau rendez-vous", `${appointment.patientName} demande un rendez-vous le ${appointment.date}`, { type: "appointment", appointmentId: event.params.appointmentId });
});
exports.appointmentUpdated = (0, firestore_2.onDocumentUpdated)("appointments/{appointmentId}", async (event) => {
    const before = event.data?.before.data();
    const after = event.data?.after.data();
    if (!before || !after || before.status === after.status)
        return;
    const recipient = after.status === "pending" ? after.doctorId : after.patientId;
    await notifyUser(recipient, "Rendez-vous mis à jour", `Nouveau statut : ${after.status}`, { type: "appointment", appointmentId: event.params.appointmentId });
});
exports.messageCreated = (0, firestore_2.onDocumentCreated)("messages/{messageId}", async (event) => {
    const message = event.data?.data();
    if (!message)
        return;
    const conversation = await db.doc(`conversations/${message.conversationId}`).get();
    const recipient = conversation.get("participantIds").find(id => id !== message.senderId);
    if (recipient)
        await notifyUser(recipient, `Message de ${message.senderName}`, message.text || "Pièce jointe", { type: "message", conversationId: message.conversationId });
});
exports.prescriptionCreated = (0, firestore_2.onDocumentCreated)({
    document: "prescriptions/{prescriptionId}",
    secrets: [supabaseUrlSecret, supabaseServiceRoleSecret, supabaseBucketSecret]
}, async (event) => {
    const prescription = event.data?.data();
    if (!prescription)
        return;
    const [doctorDocument, doctorUserDocument] = await Promise.all([
        db.doc(`doctors/${prescription.doctorId}`).get(),
        db.doc(`users/${prescription.doctorId}`).get()
    ]);
    const doctor = doctorDocument.data() || {};
    const doctorUser = doctorUserDocument.data() || {};
    const reference = prescription.reference || `ORD-${event.params.prescriptionId.toUpperCase()}`;
    const signatureHash = (0, crypto_1.createHash)("sha256").update(JSON.stringify({ ...prescription, reference })).digest("hex");
    const pdf = new pdfkit_1.default({ size: "A4", margin: 48, info: {
            Title: `Ordonnance ${reference}`,
            Author: prescription.doctorName || "Docta na Tshombo",
            Subject: "Ordonnance médicale numérique",
            Keywords: "ordonnance, santé, Docta na Tshombo"
        } });
    const chunks = [];
    pdf.on("data", chunk => chunks.push(chunk));
    const finished = new Promise(resolve => pdf.on("end", () => resolve(Buffer.concat(chunks))));
    const logoPath = path_1.default.resolve(__dirname, "../assets/app-icon.jpg");
    pdf.image(logoPath, 48, 38, { fit: [72, 72] });
    pdf.fillColor("#24533D").fontSize(22).text("DOCTA NA TSHOMBO", 132, 45);
    pdf.fillColor("#52665D").fontSize(10).text("Plateforme numérique de coordination des soins", 132, 74);
    pdf.moveTo(48, 122).lineTo(547, 122).strokeColor("#7BAA92").lineWidth(1.5).stroke();
    pdf.fillColor("#1F2937").fontSize(18).text("ORDONNANCE MÉDICALE", 48, 142, { align: "center" });
    pdf.moveDown(0.5).fontSize(9).fillColor("#52665D").text(`Référence : ${reference}`, { align: "center" });
    pdf.text(`Émise le : ${new Date().toLocaleString("fr-FR", { timeZone: "Europe/Paris" })}`, { align: "center" });
    pdf.roundedRect(48, 205, 499, 82, 8).fillAndStroke("#F1F7F3", "#B7D1C2");
    pdf.fillColor("#24533D").fontSize(11).text("PRATICIEN PRESCRIPTEUR", 62, 218);
    pdf.fillColor("#1F2937").fontSize(11).text(prescription.doctorName || "Non renseigné", 62, 238);
    pdf.fontSize(9).text(`Spécialité : ${doctor.specialty || doctorUser.specialty || "Non renseignée"}`, 62, 255);
    pdf.text(`N° professionnel : ${doctor.rpps || doctorUser.rppsNumber || "Non renseigné"}`, 290, 255);
    pdf.roundedRect(48, 302, 499, 72, 8).fillAndStroke("#FFF9F5", "#E5C4AE");
    pdf.fillColor("#8A4B2F").fontSize(11).text("PATIENT", 62, 315);
    pdf.fillColor("#1F2937").fontSize(11).text(prescription.patientName || "Non renseigné", 62, 335);
    pdf.fontSize(9).text(`Identifiant dossier : ${prescription.patientId || "Non renseigné"}`, 62, 353);
    let y = 397;
    const section = (title, value) => {
        pdf.fillColor("#24533D").fontSize(11).text(title, 48, y);
        y = pdf.y + 5;
        pdf.fillColor("#1F2937").fontSize(11).text(value || "Non renseigné", 62, y, { width: 470, lineGap: 3 });
        y = pdf.y + 14;
    };
    section("DIAGNOSTIC / MOTIF", prescription.diagnosis || "Non renseigné");
    section("MÉDICAMENT OU TRAITEMENT", prescription.medicinesSummary);
    section("POSOLOGIE", prescription.dosage || "Voir les indications du prescripteur");
    section("DURÉE", prescription.duration || "Non renseignée");
    if (prescription.notes)
        section("CONSEILS ET PRÉCAUTIONS", prescription.notes);
    const signatureY = Math.max(y + 8, 650);
    pdf.moveTo(330, signatureY).lineTo(535, signatureY).strokeColor("#7BAA92").stroke();
    pdf.fillColor("#24533D").fontSize(10).text("Signature numérique du praticien", 330, signatureY + 8, { align: "center", width: 205 });
    pdf.fillColor("#52665D").fontSize(7).text(signatureHash, 330, signatureY + 26, { width: 205, align: "center" });
    pdf.moveTo(48, 760).lineTo(547, 760).strokeColor("#D1D5DB").stroke();
    pdf.fillColor("#6B7280").fontSize(7).text("Document généré par Docta na Tshombo. Vérifiez la référence et l’empreinte avant toute délivrance. En cas d’urgence, contactez immédiatement un professionnel de santé.", 48, 770, { width: 499, align: "center" });
    pdf.end();
    const buffer = await finished;
    const storagePath = `patients/${prescription.patientId}/prescriptions/${event.params.prescriptionId}.pdf`;
    await uploadPdfToSupabase(storagePath, buffer, reference);
    await event.data?.ref.update({
        reference,
        signatureHash,
        pdfPath: storagePath,
        pdfGeneratedAt: firestore_1.FieldValue.serverTimestamp()
    });
    await notifyUser(prescription.patientId, "Nouvelle ordonnance", `${prescription.doctorName} a publié une ordonnance`, { type: "prescription", prescriptionId: event.params.prescriptionId });
});
