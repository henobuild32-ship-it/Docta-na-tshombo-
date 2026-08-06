-- 20260805100000_disable_email_confirmation.sql
-- Désactive la vérification email obligatoire lors de l'inscription
-- (auto-confirm des nouveaux comptes) afin que la connexion fonctionne
-- immédiatement avec les identifiants corrects.
--
-- Contexte du bug corrigé :
--   Sans auto-confirm, les comptes créés via signUp restent "email_not_confirmed".
--   Toute tentative de connexion (même avec le bon mot de passe) renvoie alors
--   l'erreur brute `email_not_confirmed` (avec l'URL du projet dans le message),
--   affichée telle quelle dans l'UI.
--
-- Correction à appliquer (Supabase hébergé) :
--   Dashboard > Authentication > Settings > Email :
--     - "Confirm email" : DÉSACTIVER (ou "Disable email confirmations" : ON)
--
-- Supabase hébergé ne permet pas de modifier ce réglage via SQL ; ce fichier
-- sert de trace et de référence. Si le projet est auto-hébergé, configurer :
--   GOTRUE_MAILER_AUTOCONFIRM=true
--
-- Pour les comptes DÉJÀ créés non confirmés, les confirmer en masse via la
-- console (Users > sélection > Confirm) ou via l'API Admin :
--   admin.auth.admin.updateUserById(uid, { email_confirm: true })

-- Bonus : profil par défaut déjà présent pour chaque nouveau compte.
-- (idempotent)
insert into public.profiles (id, first_name, last_name, role, is_active)
select u.id, coalesce(u.raw_user_meta_data->>'first_name', ''), coalesce(u.raw_user_meta_data->>'last_name', ''), coalesce(u.raw_user_meta_data->>'role', 'patient'), true
from auth.users u
on conflict (id) do nothing;
