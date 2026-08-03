import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const cors = { "Access-Control-Allow-Origin": "*", "Access-Control-Allow-Headers": "authorization, apikey, content-type" };
Deno.serve(async (req) => {
  if (req.method === "OPTIONS") return new Response("ok", { headers: cors });
  try {
    const auth = req.headers.get("Authorization") ?? "";
    const supabase = createClient(Deno.env.get("SUPABASE_URL")!, Deno.env.get("SUPABASE_ANON_KEY")!, { global: { headers: { Authorization: auth } } });
    const { data: { user } } = await supabase.auth.getUser();
    if (!user) throw new Error("Authentification requise");
    const body = await req.json();
    const { data: actor } = await supabase.from("profiles").select("role,is_active").eq("id", user.id).single();
    if (!actor?.is_active) throw new Error("Compte inactif");
    const targetId = String(body.userId ?? "");
    if (!targetId || !body.title || !body.body) throw new Error("Notification incomplète");
    if (actor.role !== "admin" && targetId !== user.id) {
      const { data: relation } = await supabase.from("care_relationships").select("id")
        .or(`and(doctor_id.eq.${user.id},patient_id.eq.${targetId}),and(patient_id.eq.${user.id},doctor_id.eq.${targetId})`)
        .eq("status", "active").maybeSingle();
      if (!relation) throw new Error("Destinataire non autorisé");
    }
    const admin = createClient(Deno.env.get("SUPABASE_URL")!, Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!);
    await admin.from("notifications").insert({ user_id: targetId, title: body.title, body: body.body, type: body.type ?? "general", related_id: body.relatedId ?? null });
    const appId = Deno.env.get("ONESIGNAL_APP_ID");
    const apiKey = Deno.env.get("ONESIGNAL_REST_API_KEY");
    if (!appId || !apiKey) throw new Error("OneSignal n'est pas configuré");
    const response = await fetch("https://api.onesignal.com/notifications", {
      method: "POST", headers: { "Content-Type": "application/json", Authorization: `Key ${apiKey}` },
      body: JSON.stringify({ app_id: appId, include_aliases: { external_id: [targetId] }, target_channel: "push",
        headings: { fr: String(body.title), en: String(body.title) }, contents: { fr: String(body.body), en: String(body.body) },
        data: { type: body.type ?? "general", relatedId: body.relatedId ?? null } })
    });
    const result = await response.json();
    if (!response.ok) throw new Error(`OneSignal: ${JSON.stringify(result)}`);
    return Response.json({ ok: true, id: result.id }, { headers: cors });
  } catch (error) {
    return Response.json({ error: error instanceof Error ? error.message : "Erreur" }, { status: 400, headers: cors });
  }
});
