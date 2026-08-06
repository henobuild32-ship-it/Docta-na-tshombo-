-- Présentation de l'application, affichée à chaque nouvelle personne connectée
-- (patients comme praticiens) tant que le flag n'a pas été marqué vu pour cet utilisateur.
alter table public.profiles add column if not exists presentation_seen boolean not null default false;
