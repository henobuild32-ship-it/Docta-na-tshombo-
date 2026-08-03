-- Align the backend schema with the Android app data model.
-- Adds the denormalized display columns the app reads/writes so the Kotlin
-- models map 1:1 to PostgREST columns (production-ready, no Firebase).

-- profiles: optional medical / practitioner fields the app UI uses.
alter table public.profiles add column if not exists birth_date text not null default '';
alter table public.profiles add column if not exists gender text not null default '';
alter table public.profiles add column if not exists blood_type text not null default '';
alter table public.profiles add column if not exists allergies jsonb not null default '[]';
alter table public.profiles add column if not exists medical_history jsonb not null default '[]';
alter table public.profiles add column if not exists specialty text not null default '';
alter table public.profiles add column if not exists rpps_number text not null default '';

-- doctors: photo + education profile + document flags used by the app.
alter table public.doctors add column if not exists photo_path text not null default '';
alter table public.doctors add column if not exists degree text not null default '';
alter table public.doctors add column if not exists education_level text not null default '';
alter table public.doctors add column if not exists study_duration text not null default '';
alter table public.doctors add column if not exists universities jsonb not null default '[]';
alter table public.doctors add column if not exists trainings jsonb not null default '[]';
alter table public.doctors add column if not exists graduation_year text not null default '';
alter table public.doctors add column if not exists has_documents boolean not null default false;
alter table public.doctors add column if not exists consultation_types jsonb not null default '["PRESENTIEL","TELECONSULTATION"]';

-- appointments: denormalized display columns (the app stores display strings).
alter table public.appointments add column if not exists patient_name text not null default '';
alter table public.appointments add column if not exists doctor_name text not null default '';
alter table public.appointments add column if not exists doctor_specialty text not null default '';
alter table public.appointments add column if not exists doctor_avatar text not null default '';
alter table public.appointments add column if not exists date text not null default '';
alter table public.appointments add column if not exists time text not null default '';

-- conversations/messages: mirror fields the app model expects.
alter table public.messages add column if not exists attachment_name text not null default '';

-- doctors.price is used as a display string in the app ("15 000 CDF").
alter table public.doctors alter column price type text using price::text;
