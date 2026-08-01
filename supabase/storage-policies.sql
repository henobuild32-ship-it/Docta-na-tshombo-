-- ============================================================
-- Politiques RLS pour le bucket "docta-na-tshombo" (Supabase Storage)
--
-- À exécuter dans Supabase Dashboard > SQL Editor.
--
-- PRINCIPE DE SÉCURITÉ :
-- L'app s'authentifie via Firebase. L'Edge Function "exchange-token"
-- vérifie le token Firebase et signe un JWT Supabase avec :
--   sub  = uid Firebase
--   role = authenticated
-- Les politiques ci-dessous utilisent auth.uid() (= uid Firebase)
-- pour restreindre l'accès aux dossiers de l'utilisateur.
--
-- Structure du bucket :
--   patients/{patientId}/profile|prescriptions|medical-documents|analyses|radiology|ultrasound
--   doctors/{doctorId}/documents
--   conversations/{conversationId}/attachments
-- ============================================================

-- 1) Autoriser la lecture d'un dossier patients/{uid} par le patient lui-même
create policy "patients: lecture par le patient propriétaire"
on storage.objects for select
to authenticated
using (
  bucket_id = 'docta-na-tshombo'
  and (storage.foldername(name))[1] = 'patients'
  and (storage.foldername(name))[2] = auth.uid()::text
);

-- 2) Autoriser l'upload dans le dossier patients/{uid} par le patient lui-même
create policy "patients: upload par le patient propriétaire"
on storage.objects for insert
to authenticated
with check (
  bucket_id = 'docta-na-tshombo'
  and (storage.foldername(name))[1] = 'patients'
  and (storage.foldername(name))[2] = auth.uid()::text
);

-- 3) Autoriser la mise à jour (écrasement) des fichiers du patient propriétaire
create policy "patients: mise à jour par le patient propriétaire"
on storage.objects for update
to authenticated
using (
  bucket_id = 'docta-na-tshombo'
  and (storage.foldername(name))[1] = 'patients'
  and (storage.foldername(name))[2] = auth.uid()::text
);

-- 4) Autoriser la suppression dans le dossier patients/{uid} par le propriétaire
create policy "patients: suppression par le patient propriétaire"
on storage.objects for delete
to authenticated
using (
  bucket_id = 'docta-na-tshombo'
  and (storage.foldername(name))[1] = 'patients'
  and (storage.foldername(name))[2] = auth.uid()::text
);

-- 5) Autoriser la lecture du dossier doctors/{uid} par le médecin propriétaire
create policy "doctors: lecture par le médecin propriétaire"
on storage.objects for select
to authenticated
using (
  bucket_id = 'docta-na-tshombo'
  and (storage.foldername(name))[1] = 'doctors'
  and (storage.foldername(name))[2] = auth.uid()::text
);

-- 6) Autoriser l'upload dans doctors/{uid} par le médecin propriétaire
create policy "doctors: upload par le médecin propriétaire"
on storage.objects for insert
to authenticated
with check (
  bucket_id = 'docta-na-tshombo'
  and (storage.foldername(name))[1] = 'doctors'
  and (storage.foldername(name))[2] = auth.uid()::text
);

-- 7) Autoriser la mise à jour des fichiers doctors/{uid} par le propriétaire
create policy "doctors: mise à jour par le médecin propriétaire"
on storage.objects for update
to authenticated
using (
  bucket_id = 'docta-na-tshombo'
  and (storage.foldername(name))[1] = 'doctors'
  and (storage.foldername(name))[2] = auth.uid()::text
);

-- 8) Autoriser la suppression dans doctors/{uid} par le propriétaire
create policy "doctors: suppression par le médecin propriétaire"
on storage.objects for delete
to authenticated
using (
  bucket_id = 'docta-na-tshombo'
  and (storage.foldername(name))[1] = 'doctors'
  and (storage.foldername(name))[2] = auth.uid()::text
);

-- NOTE : les pièces jointes de conversations (conversations/{id}/attachments)
-- ne sont PAS couvertes ici car l'accès dépend des participants de la
-- conversation, stockés dans Firestore. Pour les activer proprement, il faut
-- un mapping conversationId -> participants côté backend, ou restreindre via
-- un claim custom. À activer plus tard si le chat gère des pièces jointes.
