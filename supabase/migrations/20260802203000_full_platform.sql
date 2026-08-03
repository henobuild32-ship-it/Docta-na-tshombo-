create extension if not exists pgcrypto;
create extension if not exists btree_gist;

create type public.user_role as enum ('patient', 'doctor', 'admin');
create type public.appointment_status as enum ('pending', 'confirmed', 'cancelled', 'completed', 'no_show');

create table public.profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  email text not null,
  first_name text not null default '', last_name text not null default '', phone text not null default '',
  role public.user_role not null default 'patient', photo_path text, is_verified boolean not null default false,
  is_active boolean not null default true, is_senior_mode boolean not null default false,
  onesignal_subscription_id text, created_at timestamptz not null default now(), updated_at timestamptz not null default now()
);

create table public.doctors (
  id uuid primary key references public.profiles(id) on delete cascade,
  name text not null, specialty text not null, address text not null default '', price numeric(12,2), currency text default 'CDF',
  bio text not null default '', professional_number text not null, rating numeric(2,1) not null default 0,
  review_count integer not null default 0, is_online boolean not null default false, is_available boolean not null default false,
  documents_verified boolean not null default false, available_slots jsonb not null default '[]',
  created_at timestamptz not null default now(), updated_at timestamptz not null default now()
);

create table public.medical_profiles (
  patient_id uuid primary key references public.profiles(id) on delete cascade,
  birth_date date, gender text, blood_type text, allergies text[] not null default '{}', medical_history text[] not null default '{}',
  current_treatments text[] not null default '{}', emergency_contact_name text, emergency_contact_phone text,
  consent_to_share boolean not null default false, updated_at timestamptz not null default now()
);

create table public.appointments (
  id uuid primary key default gen_random_uuid(), patient_id uuid not null references public.profiles(id), doctor_id uuid not null references public.doctors(id),
  scheduled_at timestamptz not null, type text not null check(type in ('PRESENTIEL','TELECONSULTATION')), motif text not null,
  status public.appointment_status not null default 'pending', patient_note text not null default '', address text not null default '',
  created_by uuid not null default auth.uid(), created_at timestamptz not null default now(), updated_at timestamptz not null default now(),
  exclude using gist (
    doctor_id with =,
    tsrange(scheduled_at at time zone 'UTC', (scheduled_at at time zone 'UTC') + interval '30 minutes') with &&
  ) where (status in ('pending','confirmed'))
);

create table public.conversations (
  id uuid primary key default gen_random_uuid(), participant_ids uuid[] not null, participant_names text[] not null,
  last_message text not null default '', last_message_sender_id uuid, last_message_at timestamptz, created_at timestamptz not null default now(),
  check(cardinality(participant_ids)=2 and participant_ids[1] <> participant_ids[2])
);
create unique index conversations_pair on public.conversations (
  least(participant_ids[1], participant_ids[2]),
  greatest(participant_ids[1], participant_ids[2])
);

create table public.messages (
  id uuid primary key default gen_random_uuid(), conversation_id uuid not null references public.conversations(id) on delete cascade,
  sender_id uuid not null references public.profiles(id), sender_name text not null, body text not null default '', attachment_path text,
  status text not null default 'sent', created_at timestamptz not null default now()
);

create table public.prescriptions (
  id uuid primary key default gen_random_uuid(), doctor_id uuid not null references public.doctors(id), patient_id uuid not null references public.profiles(id),
  doctor_name text not null, patient_name text not null, diagnosis text not null default '', medicine text not null,
  dosage text not null, duration text not null default '', notes text not null default '', reference text unique,
  signature_hash text, pdf_path text, pdf_generated_at timestamptz, created_at timestamptz not null default now()
);

create table public.medication_reminders (
  id uuid primary key default gen_random_uuid(), patient_id uuid not null references public.profiles(id) on delete cascade,
  medicine_name text not null, dosage text not null, frequency text not null, time_of_day time not null,
  start_date date not null default current_date, end_date date, is_taken_today boolean not null default false,
  notes text not null default '', created_at timestamptz not null default now(), updated_at timestamptz not null default now()
);

create table public.notifications (
  id uuid primary key default gen_random_uuid(), user_id uuid not null references public.profiles(id) on delete cascade,
  title text not null, body text not null, type text not null, related_id uuid, is_read boolean not null default false,
  created_at timestamptz not null default now()
);

create or replace function public.handle_new_user() returns trigger language plpgsql security definer set search_path=public as $$
begin
  insert into public.profiles(id,email,first_name,last_name,phone,role)
  values(new.id,new.email,coalesce(new.raw_user_meta_data->>'first_name',''),coalesce(new.raw_user_meta_data->>'last_name',''),coalesce(new.raw_user_meta_data->>'phone',''),coalesce((new.raw_user_meta_data->>'role')::public.user_role,'patient'));
  return new;
end $$;
create trigger on_auth_user_created after insert on auth.users for each row execute function public.handle_new_user();

create or replace function public.touch_updated_at() returns trigger language plpgsql as $$ begin new.updated_at=now(); return new; end $$;
create trigger profiles_touch before update on public.profiles for each row execute function public.touch_updated_at();
create trigger doctors_touch before update on public.doctors for each row execute function public.touch_updated_at();
create trigger appointments_touch before update on public.appointments for each row execute function public.touch_updated_at();
create trigger reminders_touch before update on public.medication_reminders for each row execute function public.touch_updated_at();

create or replace function public.update_conversation_last_message() returns trigger language plpgsql security definer as $$
begin update public.conversations set last_message=new.body,last_message_sender_id=new.sender_id,last_message_at=new.created_at where id=new.conversation_id; return new; end $$;
create trigger message_updates_conversation after insert on public.messages for each row execute function public.update_conversation_last_message();

alter table public.profiles enable row level security; alter table public.doctors enable row level security;
alter table public.medical_profiles enable row level security; alter table public.appointments enable row level security;
alter table public.conversations enable row level security; alter table public.messages enable row level security;
alter table public.prescriptions enable row level security; alter table public.medication_reminders enable row level security;
alter table public.notifications enable row level security;

create policy profiles_self_select on public.profiles for select using(id=auth.uid());
create policy profiles_self_update on public.profiles for update using(id=auth.uid()) with check(id=auth.uid());
create policy doctors_authenticated_select on public.doctors for select to authenticated using(true);
create policy doctors_self_all on public.doctors for all using(id=auth.uid()) with check(id=auth.uid());
create policy medical_self_all on public.medical_profiles for all using(patient_id=auth.uid()) with check(patient_id=auth.uid());
create policy appointments_participants_select on public.appointments for select using(patient_id=auth.uid() or doctor_id=auth.uid());
create policy appointments_patient_insert on public.appointments for insert with check(patient_id=auth.uid() and status='pending');
create policy appointments_doctor_insert on public.appointments for insert with check(doctor_id=auth.uid() and status='confirmed');
create policy appointments_participants_update on public.appointments for update using(patient_id=auth.uid() or doctor_id=auth.uid());
create policy conversations_participant_all on public.conversations for all using(auth.uid()=any(participant_ids)) with check(auth.uid()=any(participant_ids));
create policy messages_participant_select on public.messages for select using(exists(select 1 from public.conversations c where c.id=conversation_id and auth.uid()=any(c.participant_ids)));
create policy messages_sender_insert on public.messages for insert with check(sender_id=auth.uid() and exists(select 1 from public.conversations c where c.id=conversation_id and auth.uid()=any(c.participant_ids)));
create policy prescriptions_participants_select on public.prescriptions for select using(patient_id=auth.uid() or doctor_id=auth.uid());
create policy prescriptions_doctor_insert on public.prescriptions for insert with check(doctor_id=auth.uid());
create policy reminders_self_all on public.medication_reminders for all using(patient_id=auth.uid()) with check(patient_id=auth.uid());
create policy notifications_self_select on public.notifications for select using(user_id=auth.uid());
create policy notifications_self_update on public.notifications for update using(user_id=auth.uid()) with check(user_id=auth.uid());

alter publication supabase_realtime add table public.appointments, public.conversations, public.messages, public.prescriptions, public.medication_reminders, public.notifications;
