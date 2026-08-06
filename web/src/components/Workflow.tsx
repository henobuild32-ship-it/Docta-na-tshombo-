import { Reveal, SectionTitle } from "./ui/primitives";
import { Download, UserPlus, CalendarCheck, Video, Pill } from "lucide-react";

export default function Workflow() {
  return (
    <section id="fonctionnement" className="py-20 sm:py-28 bg-white">
      <div className="mx-auto max-w-6xl px-5 sm:px-8">
        <SectionTitle
          eyebrow="Comment ça marche"
          title="Votre santé, étape par étape"
          description="Quatre étapes suffisent pour passer de l'installation à une consultation réussie — simples, rapides et accessibles à tous."
        />

        <div className="mt-14 grid gap-8 lg:grid-cols-4">
          {[
            {
              icon: Download,
              step: "01",
              title: "Installez l'application",
              text: "Téléchargez l'APK, créez votre profil patient en quelques minutes — aucune compétence technique requise.",
            },
            {
              icon: UserPlus,
              step: "02",
              title: "Choisissez votre praticien",
              text: "Parcourez les médecins et spécialistes vérifiés, comparez les avis patients et sélectionnez votre rendez-vous.",
            },
            {
              icon: CalendarCheck,
              step: "03",
              title: "Réservez votre rendez-vous",
              text: "Confirmez votre créneau, recevez un rappel automatique et ajoutez le rendez-vous à votre agenda.",
            },
            {
              icon: Video,
              step: "04",
              title: "Consultez et suivez",
              text: "Rejoignez la téléconsultation, recevez votre ordonnance numérique et suivez votre traitement avec des rappels.",
            },
          ].map((s, i) => (
            <Reveal key={s.step} delay={i * 0.08}>
              <div className="relative h-full rounded-2xl border border-ink/10 bg-cream p-6 shadow-card">
                <span className="absolute right-5 top-5 text-4xl font-bold text-brand-100">
                  {s.step}
                </span>
                <span className="grid h-11 w-11 place-items-center rounded-xl bg-brand-600 text-white shadow-glow">
                  <s.icon className="h-5 w-5" />
                </span>
                <h3 className="mt-4 text-base font-semibold text-ink">{s.title}</h3>
                <p className="mt-2 text-sm leading-relaxed text-ink-soft">{s.text}</p>
              </div>
            </Reveal>
          ))}
        </div>

        <Reveal delay={0.15} className="mt-10">
          <div className="flex flex-col sm:flex-row items-center justify-between gap-4 rounded-3xl bg-brand-600 px-8 py-7 text-white shadow-glow">
            <div className="flex items-center gap-4">
              <span className="grid h-12 w-12 place-items-center rounded-2xl bg-white/15">
                <Pill className="h-6 w-6" />
              </span>
              <div>
                <h3 className="text-lg font-semibold">
                  Le suivi de traitement, simplifié
                </h3>
                <p className="text-sm text-white/80">
                  Rappels programmés, doses et historique — tout est notifié à temps.
                </p>
              </div>
            </div>
            <span className="shrink-0 rounded-full bg-white px-4 py-2 text-xs font-bold text-brand-700">
              -96% d'oublis* estimés
            </span>
          </div>
        </Reveal>
      </div>
    </section>
  );
}