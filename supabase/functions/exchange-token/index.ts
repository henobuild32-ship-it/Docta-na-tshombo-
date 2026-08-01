// Edge Function: exchange-token
// Échange un ID token Firebase contre un JWT Supabase signé.
// La service_role key reste uniquement côté serveur (jamais dans l'app).
//
// Variables d'environnement requises (supabase secrets set ...):
//   - SERVICE_ROLE_KEY  : la clé service_role du projet
//   - JWT_SECRET        : le JWT secret du projet (Dashboard > Settings > API)
//   - FIREBASE_PROJECT_ID : l'ID du projet Firebase (ex: docta-na-tshombo)
// NB: le CLI Supabase refuse les noms commençant par "SUPABASE_".
//
// Appel depuis le client:
//   POST {supabaseUrl}/functions/v1/exchange-token
//   Authorization: Bearer <SUPABASE_ANON_KEY>
//   Content-Type: application/json
//   Body: { "firebaseToken": "<ID token Firebase>" }
//
// Réponse:
//   { "accessToken": "<JWT Supabase signé (sub=uid Firebase, role=authenticated)>", "expiresAt": ... }

import { serve } from "https://deno.land/std@0.224.0/http/server.ts";
import { create as createJwt } from "https://deno.land/x/djwt@v3.0.2/mod.ts";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers":
    "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
};

// Récupère les clés publiques JWKS du projet Firebase (Google).
async function fetchFirebaseJwks(): Promise<Record<string, JsonWebKey>> {
  const projectId = Deno.env.get("FIREBASE_PROJECT_ID");
  if (!projectId) {
    throw new Error("FIREBASE_PROJECT_ID manquante");
  }
  const url = `https://www.googleapis.com/service_accounts/v1/jwk/securetoken@system.gserviceaccount.com/${projectId}`;
  const resp = await fetch(url);
  if (!resp.ok) {
    throw new Error(`Impossible de récupérer les clés Firebase: ${resp.status}`);
  }
  const data = await resp.json();
  const keys: Record<string, JsonWebKey> = {};
  for (const key of data.keys ?? []) {
    keys[key.kid] = key;
  }
  return keys;
}

// Vérifie cryptographiquement un ID token Firebase.
async function verifyFirebaseToken(
  token: string
): Promise<Record<string, unknown>> {
  const projectId = Deno.env.get("FIREBASE_PROJECT_ID");
  const parts = token.split(".");
  if (parts.length !== 3) {
    throw new Error("Token Firebase malformé");
  }

  // Décoder le header pour récupérer le kid
  const headerJson = decodePart(parts[0]);
  const kid = headerJson.kid as string;

  // Décoder le payload
  const payload = decodePart(parts[1]);

  // Vérifier l'audience (doit être le project_id Firebase)
  if (payload.aud !== projectId) {
    throw new Error("Audience invalide");
  }

  // Vérifier l'émetteur
  if (payload.iss !== `https://securetoken.google.com/${projectId}`) {
    throw new Error("Émetteur invalide");
  }

  // Vérifier l'expiration
  const now = Math.floor(Date.now() / 1000);
  if (payload.exp && payload.exp < now) {
    throw new Error("Token expiré");
  }

  // Vérifier la signature avec la clé publique JWKS
  const jwks = await fetchFirebaseJwks();
  const jwk = jwks[kid];
  if (!jwk) {
    throw new Error("Clé de signature introuvable");
  }

  const publicKey = await crypto.subtle.importKey(
    "jwk",
    jwk,
    { name: "RSASSA-PKCS1-v1_5", hash: "SHA-256" },
    false,
    ["verify"]
  );

  const data = new TextEncoder().encode(parts[0] + "." + parts[1]);
  const signature = base64urlToBytes(parts[2]);

  const valid = await crypto.subtle.verify(
    "RSASSA-PKCS1-v1_5",
    publicKey,
    signature,
    data
  );

  if (!valid) {
    throw new Error("Signature invalide");
  }

  return payload;
}

function decodePart(part: string): Record<string, unknown> {
  return JSON.parse(bytesToText(base64urlToBytes(part)));
}

function base64urlToBytes(input: string): Uint8Array {
  const b64 = input.replace(/-/g, "+").replace(/_/g, "/");
  const padded = b64.padEnd(b64.length + ((4 - (b64.length % 4)) % 4), "=");
  const bin = atob(padded);
  const bytes = new Uint8Array(bin.length);
  for (let i = 0; i < bin.length; i++) {
    bytes[i] = bin.charCodeAt(i);
  }
  return bytes;
}

function bytesToText(bytes: Uint8Array): string {
  return new TextDecoder().decode(bytes);
}

async function handleExchange(req: Request): Promise<Response> {
  const body = await req.json();
  const firebaseToken = body.firebaseToken as string;

  if (!firebaseToken) {
    return new Response(JSON.stringify({ error: "firebaseToken manquant" }), {
      status: 400,
      headers: { ...corsHeaders, "Content-Type": "application/json" },
    });
  }

  const serviceRoleKey = Deno.env.get("SERVICE_ROLE_KEY");
  const jwtSecret = Deno.env.get("JWT_SECRET");

  if (!serviceRoleKey || !jwtSecret) {
    return new Response(
      JSON.stringify({ error: "Configuration serveur manquante" }),
      { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );
  }

  try {
    // 1. Vérifier cryptographiquement le token Firebase
    const decoded = await verifyFirebaseToken(firebaseToken);
    const uid = decoded.uid as string;
    const email = (decoded.email as string) ?? null;

    if (!uid) {
      return new Response(
        JSON.stringify({ error: "Token Firebase invalide : uid manquant" }),
        { status: 401, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    // 2. Signer un JWT Supabase (service_role key = secret serveur)
    const now = Math.floor(Date.now() / 1000);
    const accessToken = await createJwt(
      { alg: "HS256", typ: "JWT" },
      {
        sub: uid,
        email: email,
        role: "authenticated",
        aud: "authenticated",
        iss: "supabase",
        ref: "docta-na-tshombo",
        iat: now,
        exp: now + 3600, // 1 heure
      },
      jwtSecret
    );

    return new Response(
      JSON.stringify({ accessToken, expiresAt: now + 3600 }),
      { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );
  } catch (e) {
    return new Response(
      JSON.stringify({ error: (e as Error).message ?? "Erreur interne" }),
      { status: 401, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );
  }
}

serve(async (req: Request) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }
  if (req.method !== "POST") {
    return new Response(
      JSON.stringify({ error: "Méthode non autorisée" }),
      { status: 405, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );
  }
  return await handleExchange(req);
});
