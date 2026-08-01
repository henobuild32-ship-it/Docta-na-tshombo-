# Supabase Storage — Configuration manuelle

Ce dossier contient les fichiers nécessaires au remplacement de Firebase Storage
par Supabase Storage, avec authentification sécurisée via Firebase Auth.

## Architecture

```
App Android
   │ 1. Firebase Auth (login Firebase) -> ID token
   ▼
Supabase Edge Function "exchange-token"
   │ 2. Vérifie la signature du token Firebase (JWKS Google)
   │ 3. Signe un JWT Supabase (role=authenticated, sub=uid Firebase)
   ▼
Supabase Storage
   │ 4. RLS compare request.auth.uid (= uid Firebase) au dossier du fichier
   ▼
Fichiers privés dans le bucket "docta-na-tshombo"
```

La clé service_role ne vit QUE côté serveur (variable d'environnement de
l'Edge Function). L'app n'utilise que la clé anon publique.

## Étapes à faire dans le Dashboard Supabase

### 1. Variables d'environnement de l'Edge Function

Dans **Supabase Dashboard > Edge Functions > exchange-token > Secrets**
(ou via CLI), définir :

```
SUPABASE_SERVICE_ROLE_KEY=<clé service_role du projet>
SUPABASE_JWT_SECRET=<JWT secret du projet>
FIREBASE_PROJECT_ID=docta-na-tshombo
```

- `SUPABASE_SERVICE_ROLE_KEY` : Dashboard > Settings > API > `service_role` secret
- `SUPABASE_JWT_SECRET` : Dashboard > Settings > API > JWT secret (clic "Reveal")
- `FIREBASE_PROJECT_ID` : l'ID du projet Firebase (`docta-na-tshombo`)

> ⚠️ Ne JAMAIS mettre la service_role key dans le code de l'app ou dans `.env`.
> Elle ne doit exister que comme secret de l'Edge Function côté serveur.

### 2. Déployer l'Edge Function

Depuis la racine du projet (CLI Supabase) :

```bash
supabase login
supabase link --project-ref ureopnzdiupnlhmhvdem
supabase secrets set SUPABASE_SERVICE_ROLE_KEY=... SUPABASE_JWT_SECRET=... FIREBASE_PROJECT_ID=docta-na-tshombo
supabase functions deploy exchange-token
```

### 3. Exécuter les politiques RLS

1. Vérifier que le bucket `docta-na-tshombo` est **privé**
   (Dashboard > Storage > docta-na-tshombo > Settings > Public bucket = OFF).
2. Ouvrir **SQL Editor** dans le Dashboard.
3. Coller le contenu de `storage-policies.sql` et exécuter.

## Vérification

Dans **SQL Editor**, tester que les politiques sont bien en place :

```sql
select policyname, cmd from pg_policies where tablename = 'objects';
```

Depuis l'app : se connecter, uploader une photo de profil, vérifier que le
fichier apparaît dans Storage sous `patients/{uid}/profile/...`.

## Fichiers

- `functions/exchange-token/index.ts` : Edge Function d'échange de token
- `config.toml` : configuration Supabase (déclare la fonction)
- `storage-policies.sql` : politiques RLS du bucket
- `../.env` : `SUPABASE_URL`, `SUPABASE_ANON_KEY`, `SUPABASE_BUCKET_NAME`
  (valeurs publiques, pas de secret)
