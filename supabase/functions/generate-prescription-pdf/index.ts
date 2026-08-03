import { createClient } from "https://esm.sh/@supabase/supabase-js@2";
import { PDFDocument, StandardFonts, rgb } from "npm:pdf-lib@1.17.1";

const cors = { "Access-Control-Allow-Origin": "*", "Access-Control-Allow-Headers": "authorization, apikey, content-type" };
Deno.serve(async (req) => {
  if (req.method === "OPTIONS") return new Response("ok", { headers: cors });
  try {
    const authorization = req.headers.get("Authorization") ?? "";
    const client = createClient(Deno.env.get("SUPABASE_URL")!, Deno.env.get("SUPABASE_ANON_KEY")!, { global: { headers: { Authorization: authorization } } });
    const { data: { user } } = await client.auth.getUser();
    if (!user) throw new Error("Authentification requise");
    const { prescriptionId } = await req.json();
    const { data: prescription, error } = await client.from("prescriptions").select("*").eq("id", prescriptionId).single();
    if (error || !prescription) throw new Error("Ordonnance introuvable");
    if (prescription.doctor_id !== user.id) throw new Error("Seul le médecin auteur peut générer ce PDF");
    const { data: doctor } = await client.from("doctors").select("specialty,professional_number,documents_verified").eq("id", user.id).single();
    if (!doctor?.documents_verified) throw new Error("Le compte médecin doit être vérifié");

    const pdf = await PDFDocument.create(); const page = pdf.addPage([595, 842]);
    const regular = await pdf.embedFont(StandardFonts.Helvetica); const bold = await pdf.embedFont(StandardFonts.HelveticaBold);
    const logoUrl = Deno.env.get("APP_LOGO_URL");
    if (logoUrl) try { const bytes = new Uint8Array(await (await fetch(logoUrl)).arrayBuffer()); const image = await pdf.embedJpg(bytes); page.drawImage(image, { x: 44, y: 760, width: 58, height: 58 }); } catch (_) { /* heading remains */ }
    page.drawText("DOCTA NA TSHOMBO", { x: 120, y: 798, size: 20, font: bold, color: rgb(.12,.28,.17) });
    page.drawText("ORDONNANCE MÉDICALE", { x: 120, y: 772, size: 14, font: bold });
    const lines = [
      `Médecin : ${prescription.doctor_name}`, `Spécialité : ${doctor.specialty}`, `N° professionnel : ${doctor.professional_number}`,
      `Patient : ${prescription.patient_name}`, `Diagnostic : ${prescription.diagnosis || "Non renseigné"}`,
      `Médicament : ${prescription.medicine}`, `Posologie : ${prescription.dosage}`, `Durée : ${prescription.duration}`,
      `Notes : ${prescription.notes || "Aucune"}`, `Référence : ${prescription.reference || prescription.id}`
    ];
    let y = 710; for (const line of lines) { page.drawText(line.slice(0, 100), { x: 48, y, size: 11, font: regular }); y -= 34; }
    const signedAt = new Date().toISOString();
    const integrity = await crypto.subtle.digest("SHA-256", new TextEncoder().encode(`${prescription.id}|${user.id}|${signedAt}`));
    const signatureHash = Array.from(new Uint8Array(integrity)).map(v => v.toString(16).padStart(2, "0")).join("");
    page.drawText("Signature numérique du médecin", { x: 340, y: 170, size: 11, font: bold });
    page.drawText(signatureHash.slice(0, 40), { x: 340, y: 150, size: 7, font: regular });
    page.drawText("Document généré électroniquement — vérifier la référence et l'intégrité.", { x: 48, y: 70, size: 8, font: regular });
    const bytes = await pdf.save(); const path = `patients/${prescription.patient_id}/prescriptions/${prescription.id}.pdf`;
    const admin = createClient(Deno.env.get("SUPABASE_URL")!, Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!);
    const upload = await admin.storage.from(Deno.env.get("SUPABASE_BUCKET_NAME") ?? "docta-na-tshombo").upload(path, bytes, { contentType: "application/pdf", upsert: true });
    if (upload.error) throw upload.error;
    await admin.from("prescriptions").update({ pdf_path: path, pdf_generated_at: signedAt, signature_hash: signatureHash }).eq("id", prescription.id);
    const { data: signed } = await admin.storage.from(Deno.env.get("SUPABASE_BUCKET_NAME") ?? "docta-na-tshombo").createSignedUrl(path, 300);
    return Response.json({ path, url: signed?.signedUrl, signatureHash }, { headers: cors });
  } catch (error) { return Response.json({ error: error instanceof Error ? error.message : "Erreur" }, { status: 400, headers: cors }); }
});
