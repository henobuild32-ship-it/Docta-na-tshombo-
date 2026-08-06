-- =============================================================
-- SEED DE DÉMONSTRATION — Docta na Tshombo
-- Complète les comptes créés via l'API Admin (voir seed_users.ps1).
-- Les comptes auth.users sont créés par seed_users.ps1 ; ce fichier
-- n'ajoute QUE les données métier : spécialités, profils médecins,
-- profil médical du patient, rendez-vous et avis de démonstration.
--
-- COMPTES DE DÉMONSTRATION (mot de passe identique : Demo1234!)
--   admin@doctanatshombo.cd   -> admin
--   patient@demo.cd           -> patient
--   dr.kabongo@demo.cd        -> médecin (médecine générale)
--   dr.mukendi@demo.cd        -> médecin (cardiologie)
--   dr.ngoy@demo.cd           -> médecin (pédiatrie)
--   dr.katumba@demo.cd        -> médecin (gynécologie)
--   dr.mwamba@demo.cd         -> médecin (dermatologie)
--   dr.ilunga@demo.cd         -> médecin (médecine interne)
--
-- Sûr à rejouer : chaque insertion est idempotente (ON CONFLICT DO NOTHING).
-- =============================================================

-- ---------- SPÉCIALITÉS ----------
insert into public.specialties (name, description, icon_name, display_order, is_active) values
  ('Médecine générale', 'Consultations de premier recours, suivi courant.', 'stethoscope', 1, true),
  ('Cardiologie', 'Cœur, tension artérielle et système cardiovasculaire.', 'heart', 2, true),
  ('Pédiatrie', 'Santé des nourrissons, enfants et adolescents.', 'baby', 3, true),
  ('Gynécologie', 'Santé de la femme et suivi de grossesse.', 'female', 4, true),
  ('Dermatologie', 'Peau, cheveux et ongles.', 'skin', 5, true),
  ('Médecine interne', 'Diagnostic et traitement des maladies de l''adulte.', 'user', 6, true),
  ('ORL', 'Oreilles, nez et gorge.', 'ear', 7, true),
  ('Ophtalmologie', 'Santé des yeux et de la vision.', 'eye', 8, true)
on conflict (name) do nothing;

-- ---------- PROFILS MÉDECINS (table doctors) ----------
-- Les id correspondent aux comptes créés par seed_users.ps1.
insert into public.doctors (
  id, name, specialty, address, price, bio, professional_number, rating, review_count,
  is_online, is_available, documents_verified, available_slots, photo_path, degree, education_level,
  study_duration, universities, trainings, graduation_year, has_documents, consultation_types
) values
  ('f8f0827e-bd36-48ae-8817-ff304db637b7', 'Dr Jean-Pierre Kabongo', 'Médecine générale', 'Avenue de la Paix, Kinshasa/Gombe', '15 000 CDF',
   'Médecin généraliste de proximité, plus de 12 ans d''expérience en santé communautaire.',
   'RG-2014-1147', 4.8, 32, true, true, true, '["2026-08-05 09:00","2026-08-05 11:00","2026-08-06 14:00"]',
   '', 'Doctorat en médecine', 'Spécialiste en médecine générale', '6 ans', '["Université de Kinshasa"]', '["Médecine communautaire","Prise en charge du paludisme"]', '2014', true, '["PRESENTIEL","TELECONSULTATION"]'),
  ('44d71488-e545-4f63-8a5f-935a14c84d5b', 'Dr Albert Mukendi', 'Cardiologie', 'Clinique du Fleuve, Kinshasa/Gombe', '25 000 CDF',
   'Cardiologue attentif aux maladies cardiovasculaires chroniques de l''adulte.',
   'RC-2011-0884', 4.9, 57, true, true, true, '["2026-08-05 10:00","2026-08-05 15:00"]',
   '', 'Doctorat en médecine', 'Spécialiste en cardiologie', '9 ans', '["Université de Kinshasa","Université de Lubumbashi"]', '["Échocardiographie","HTA sévère"]', '2011', true, '["PRESENTIEL","TELECONSULTATION"]'),
  ('1961ddba-940f-4d28-ad41-b583d43a045e', 'Dr Marie-Josée Ngoy', 'Pédiatrie', 'Centre médical Ngaliema, Kinshasa', '18 000 CDF',
   'Pédiatre dévouée au suivi des nourrissons et des enfants.',
   'RP-2016-2031', 4.7, 41, true, true, true, '["2026-08-05 08:30","2026-08-06 09:00"]',
   '', 'Doctorat en médecine', 'Spécialiste en pédiatrie', '6 ans', '["Université de Kinshasa"]', '["Vaccination","Nutrition infantile"]', '2016', true, '["PRESENTIEL","TELECONSULTATION"]'),
  ('4dca0d80-76a1-4fc7-8b8d-f3b58cc40b09', 'Dr Patricia Katumba', 'Gynécologie', 'Polyclinique des Mamans, Kinshasa/Limete', '22 000 CDF',
   'Gynécologue-obstétricienne, suivi de grossesse et santé de la femme.',
   'RG-2013-1560', 4.8, 63, true, true, true, '["2026-08-06 10:00","2026-08-07 13:00"]',
   '', 'Doctorat en médecine', 'Spécialiste en gynécologie-obstétrique', '7 ans', '["Université de Kinshasa","Université de Kisangani"]', '["Grossesse à risque","Echographie obstétricale"]', '2013', true, '["PRESENTIEL","TELECONSULTATION"]'),
  ('ef2c8d6a-e877-4ebb-8804-f00d3bd7b2a1', 'Dr Dieudonné Mwamba', 'Dermatologie', 'Cabinet Mwamba, Kinshasa/Gombe', '20 000 CDF',
   'Dermatologue spécialisé dans les pathologies cutanées tropicales.',
   'RD-2015-1744', 4.6, 28, true, true, true, '["2026-08-05 14:00","2026-08-07 09:30"]',
   '', 'Doctorat en médecine', 'Spécialiste en dermatologie', '6 ans', '["Université de Kinshasa"]', '["Dermatologie tropicale","Allergologie cutanée"]', '2015', true, '["PRESENTIEL","TELECONSULTATION"]'),
  ('59e3fe74-4b91-4e25-ad5c-69c778b16cb8', 'Dr Michel Ilunga', 'Médecine interne', 'Hôpital général de référence, Kinshasa/Ngaliema', '20 000 CDF',
   'Interniste pour le diagnostic et le suivi des maladies chroniques de l''adulte.',
   'RI-2010-0722', 4.9, 74, true, true, true, '["2026-08-05 09:30","2026-08-06 15:00"]',
   '', 'Doctorat en médecine', 'Spécialiste en médecine interne', '8 ans', '["Université de Kinshasa"]', '["Diabétologie","HTA et insuffisance rénale"]', '2010', true, '["PRESENTIEL","TELECONSULTATION"]')
on conflict (id) do nothing;

-- ---------- PROFIL MÉDICAL DU PATIENT DÉMO ----------
insert into public.medical_profiles (patient_id, birth_date, gender, blood_type, allergies, medical_history, current_treatments, emergency_contact_name, emergency_contact_phone, consent_to_share, updated_at)
values ('ed626ed5-6ed6-4d5d-bab0-063c1fe42ca1', '1992-03-14', 'female', 'O+', '{Pénicilline}', '{Asthme léger}', '{}', 'Kabila Kalume', '+243 810 999 888', true, now())
on conflict (patient_id) do nothing;

-- ---------- UN RENDEZ-VOUS FUTUR DE DÉMONSTRATION ----------
insert into public.appointments (id, patient_id, doctor_id, scheduled_at, type, motif, status, patient_note, address, created_by, patient_name, doctor_name, doctor_specialty, doctor_avatar, date, time)
select
  '00000000-0000-0000-0000-000000000101',
  'ed626ed5-6ed6-4d5d-bab0-063c1fe42ca1',
  '44d71488-e545-4f63-8a5f-935a14c84d5b',
  now() + interval '2 days',
  'TELECONSULTATION', 'Suivi de la tension artérielle', 'pending', 'Dernier contrôle il y a trois mois.', 'Téléconsultation en ligne sécurisée',
  'ed626ed5-6ed6-4d5d-bab0-063c1fe42ca1',
  'Grâce Kalume', 'Dr Albert Mukendi', 'Cardiologie', '',
  to_char(now() + interval '2 days', 'YYYY-MM-DD'), '10:00'
where not exists (select 1 from public.appointments where id = '00000000-0000-0000-0000-000000000101');

-- ---------- AVIS DE DÉMONSTRATION ----------
insert into public.reviews (id, doctor_id, patient_id, appointment_id, rating, comment)
select
  '00000000-0000-0000-0000-000000000201',
  '44d71488-e545-4f63-8a5f-935a14c84d5b',
  'ed626ed5-6ed6-4d5d-bab0-063c1fe42ca1',
  null, 5, 'Consultation claire et rassurante, bien expliquée.'
where not exists (select 1 from public.reviews where id = '00000000-0000-0000-0000-000000000201');
