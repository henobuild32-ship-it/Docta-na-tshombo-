-- Production hardening for the Firebase-free platform.
create extension if not exists btree_gist;

create table if not exists public.care_relationships (
  id uuid primary key default gen_random_uuid(),
  patient_id uuid not null references public.profiles(id) on delete cascade,
  doctor_id uuid not null references public.doctors(id) on delete cascade,
  status text not null default 'active' check (status in ('pending','active','revoked')),
  consent_granted_at timestamptz,
  revoked_at timestamptz,
  created_at timestamptz not null default now(),
  unique(patient_id, doctor_id)
);

create table if not exists public.doctor_schedules (
  id uuid primary key default gen_random_uuid(), doctor_id uuid not null references public.doctors(id) on delete cascade,
  weekday smallint not null check (weekday between 0 and 6), start_time time not null, end_time time not null,
  slot_minutes smallint not null default 30 check (slot_minutes between 10 and 240), is_active boolean not null default true,
  check(start_time < end_time), unique(doctor_id, weekday, start_time)
);
create table if not exists public.doctor_absences (
  id uuid primary key default gen_random_uuid(), doctor_id uuid not null references public.doctors(id) on delete cascade,
  starts_at timestamptz not null, ends_at timestamptz not null, reason text not null default '', created_at timestamptz not null default now(),
  check(starts_at < ends_at)
);
create table if not exists public.consultations (
  id uuid primary key default gen_random_uuid(), appointment_id uuid not null unique references public.appointments(id) on delete cascade,
  doctor_id uuid not null references public.doctors(id), patient_id uuid not null references public.profiles(id),
  notes text not null default '', diagnosis text not null default '', started_at timestamptz, ended_at timestamptz,
  created_at timestamptz not null default now(), updated_at timestamptz not null default now()
);
create table if not exists public.vital_signs (
  id uuid primary key default gen_random_uuid(), patient_id uuid not null references public.profiles(id) on delete cascade,
  consultation_id uuid references public.consultations(id) on delete set null, systolic integer, diastolic integer,
  heart_rate integer, temperature_c numeric(4,1), oxygen_saturation numeric(5,2), weight_kg numeric(6,2),
  source text not null default 'manual' check(source in ('manual','certified_device')), measured_at timestamptz not null default now(),
  created_by uuid not null default auth.uid()
);
create table if not exists public.reviews (
  id uuid primary key default gen_random_uuid(), doctor_id uuid not null references public.doctors(id) on delete cascade,
  patient_id uuid not null references public.profiles(id) on delete cascade, appointment_id uuid references public.appointments(id) on delete set null,
  rating smallint not null check(rating between 1 and 5), comment text not null default '', created_at timestamptz not null default now(),
  unique(patient_id, appointment_id)
);
create table if not exists public.specialties (
  id uuid primary key default gen_random_uuid(), name text not null unique, description text not null default '', icon_name text not null default '',
  display_order integer not null default 0, is_active boolean not null default true
);
create table if not exists public.user_devices (
  id uuid primary key default gen_random_uuid(), user_id uuid not null references public.profiles(id) on delete cascade,
  onesignal_subscription_id text not null unique, platform text not null check(platform in ('android','web')),
  enabled boolean not null default true, last_seen_at timestamptz not null default now(), created_at timestamptz not null default now()
);
create table if not exists public.audit_logs (
  id bigint generated always as identity primary key, actor_id uuid, action text not null, entity_type text not null,
  entity_id text, metadata jsonb not null default '{}', created_at timestamptz not null default now()
);

create index if not exists appointments_patient_date_idx on public.appointments(patient_id, scheduled_at desc);
create index if not exists appointments_doctor_date_idx on public.appointments(doctor_id, scheduled_at desc);
create index if not exists messages_conversation_date_idx on public.messages(conversation_id, created_at);
create index if not exists notifications_user_date_idx on public.notifications(user_id, created_at desc);
create index if not exists care_relationships_lookup_idx on public.care_relationships(doctor_id, patient_id, status);

create or replace function public.is_admin() returns boolean language sql stable security definer set search_path=public as $$
  select exists(select 1 from public.profiles where id=auth.uid() and role='admin' and is_active);
$$;
create or replace function public.is_doctor() returns boolean language sql stable security definer set search_path=public as $$
  select exists(select 1 from public.profiles where id=auth.uid() and role='doctor' and is_active);
$$;
create or replace function public.has_patient_access(target_patient uuid) returns boolean language sql stable security definer set search_path=public as $$
  select target_patient=auth.uid() or public.is_admin() or exists(
    select 1 from public.care_relationships where patient_id=target_patient and doctor_id=auth.uid() and status='active' and consent_granted_at is not null
  );
$$;

-- Prevent privilege escalation through profile updates.
create or replace function public.protect_profile_fields() returns trigger language plpgsql security definer set search_path=public as $$
begin
  if not public.is_admin() then
    new.role := old.role; new.is_verified := old.is_verified; new.is_active := old.is_active;
  end if;
  return new;
end $$;
drop trigger if exists profiles_protect_fields on public.profiles;
create trigger profiles_protect_fields before update on public.profiles for each row execute function public.protect_profile_fields();

-- Appointment creation is centralized and validates identity, role, schedule and absences.
create or replace function public.create_appointment(
  p_patient_id uuid, p_doctor_id uuid, p_scheduled_at timestamptz, p_type text, p_motif text,
  p_patient_note text default '', p_address text default ''
) returns public.appointments language plpgsql security definer set search_path=public as $$
declare result public.appointments; creator_role public.user_role;
begin
  select role into creator_role from public.profiles where id=auth.uid() and is_active;
  if creator_role='patient' and auth.uid()<>p_patient_id then raise exception 'patient_identity_mismatch'; end if;
  if creator_role='doctor' and auth.uid()<>p_doctor_id then raise exception 'doctor_identity_mismatch'; end if;
  if creator_role is null then raise exception 'unauthenticated_or_inactive'; end if;
  if p_scheduled_at <= now() then raise exception 'appointment_must_be_in_future'; end if;
  if exists(select 1 from public.doctor_absences where doctor_id=p_doctor_id and p_scheduled_at < ends_at and p_scheduled_at+interval '30 min' > starts_at) then
    raise exception 'doctor_absent';
  end if;
  insert into public.appointments(patient_id,doctor_id,scheduled_at,type,motif,status,patient_note,address,created_by)
  values(p_patient_id,p_doctor_id,p_scheduled_at,p_type,p_motif,case when creator_role='doctor' then 'confirmed' else 'pending' end,p_patient_note,p_address,auth.uid())
  returning * into result;
  insert into public.care_relationships(patient_id,doctor_id,status,consent_granted_at) values(p_patient_id,p_doctor_id,'active',now())
  on conflict(patient_id,doctor_id) do update set status='active',consent_granted_at=coalesce(public.care_relationships.consent_granted_at,now()),revoked_at=null;
  return result;
end $$;
revoke all on function public.create_appointment(uuid,uuid,timestamptz,text,text,text,text) from public;
grant execute on function public.create_appointment(uuid,uuid,timestamptz,text,text,text,text) to authenticated;

alter table public.care_relationships enable row level security;
alter table public.doctor_schedules enable row level security;
alter table public.doctor_absences enable row level security;
alter table public.consultations enable row level security;
alter table public.vital_signs enable row level security;
alter table public.reviews enable row level security;
alter table public.specialties enable row level security;
alter table public.user_devices enable row level security;
alter table public.audit_logs enable row level security;

create policy care_participants_select on public.care_relationships for select using(patient_id=auth.uid() or doctor_id=auth.uid() or public.is_admin());
create policy care_patient_consent on public.care_relationships for update using(patient_id=auth.uid()) with check(patient_id=auth.uid());
create policy schedules_read on public.doctor_schedules for select to authenticated using(true);
create policy schedules_doctor_write on public.doctor_schedules for all using(doctor_id=auth.uid()) with check(doctor_id=auth.uid() and public.is_doctor());
create policy absences_read on public.doctor_absences for select to authenticated using(true);
create policy absences_doctor_write on public.doctor_absences for all using(doctor_id=auth.uid()) with check(doctor_id=auth.uid() and public.is_doctor());
create policy consultations_participants on public.consultations for select using(patient_id=auth.uid() or doctor_id=auth.uid() or public.is_admin());
create policy consultations_doctor_write on public.consultations for all using(doctor_id=auth.uid()) with check(doctor_id=auth.uid() and public.is_doctor());
create policy vital_access on public.vital_signs for select using(public.has_patient_access(patient_id));
create policy vital_insert on public.vital_signs for insert with check((patient_id=auth.uid() or public.has_patient_access(patient_id)) and created_by=auth.uid());
create policy reviews_read on public.reviews for select to authenticated using(true);
create policy reviews_patient_write on public.reviews for insert with check(patient_id=auth.uid() and exists(select 1 from public.appointments a where a.id=appointment_id and a.patient_id=auth.uid() and a.doctor_id=doctor_id and a.status='completed'));
create policy specialties_read on public.specialties for select using(is_active or public.is_admin());
create policy devices_self on public.user_devices for all using(user_id=auth.uid()) with check(user_id=auth.uid());
create policy audit_admin_read on public.audit_logs for select using(public.is_admin());

drop policy if exists medical_self_all on public.medical_profiles;
create policy medical_authorized_select on public.medical_profiles for select using(public.has_patient_access(patient_id));
create policy medical_patient_write on public.medical_profiles for all using(patient_id=auth.uid()) with check(patient_id=auth.uid());

-- Direct table inserts are disabled: callers must use create_appointment().
drop policy if exists appointments_patient_insert on public.appointments;
drop policy if exists appointments_doctor_insert on public.appointments;

do $$ begin
  alter publication supabase_realtime add table public.consultations, public.care_relationships;
exception when duplicate_object then null; end $$;
