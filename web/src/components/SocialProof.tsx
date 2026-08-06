import { Reveal, SectionTitle } from "./ui/primitives";
import { Quote } from "lucide-react";
import { STATS } from "../lib/config";

export default function SocialProof() {
  return (
    <section className="py-20 sm:py-28">
      <div className="mx-auto max-w-6xl px-5 sm:px-8">
        <SectionTitle
          eyebrow="Ils nous font confiance"
          title="Un outil pensé avec les acteurs de terrain"
          description="Patients, professionnels de santé et structures partenaires utilisent déjà l'application au quotidien."
        />

        <Reveal className="mt-12">
          <div className="grid grid-cols-2 gap-px overflow-hidden rounded-2xl border border-ink/10 bg-ink/10 sm:grid-cols-4">
            {STATS.map((s) => (
              <div key={s.label} className="bg-white p-6 text-center">
                <p className="text-3xl font-bold tracking-tight text-brand-700">{s.value}</p>
                <p className="mt-1 text-xs text-ink-soft">{s.label}</p>
              </div>
            ))}
          </div>
        </Reveal>

        <div className="mt-12 grid gap-6 md:grid-cols-3">
          {[
            {
              quote:
                "Enfin une application qui pense à nos réalités. Je réserve mes rendez-vous sans me déplacer à chaque fois, et les rappels me font gagner un temps précieux.",
              name: "Grâce K.",
              role: "Patiente · Kinshasa",
            },
            {
              quote:
                "Le dossier patient est lisible et complet : je retrouve l'historique de mes patients en un instant. La messagerie sécurisée est un vrai gain pour le suivi.",
              name: "Dr Michel I.",
              role: "Médecin généraliste",
            },
            {
              quote:
                "Mon mari suit son traitement contre l'hypertension sans jamais oublier une prise. Les rappels changent réellement la donne dans notre famille.",
              name: "Alphonsine N.",
              role: "Proche aidante · Lubumbashi",
            },
          ].map((t, i) => (
            <Reveal key={t.name} delay={i * 0.08}>
              <figure className="flex h-full flex-col rounded-2xl border border-ink/10 bg-white p-6 shadow-card">
                <Quote className="h-6 w-6 text-brand-300" />
                <blockquote className="mt-4 flex-1 text-sm leading-relaxed text-ink-soft">
                  « {t.quote} »
                </blockquote>
                <figcaption className="mt-5 border-t border-ink/5 pt-4">
                  <p className="text-sm font-semibold text-ink">{t.name}</p>
                  <p className="text-xs text-ink-soft">{t.role}</p>
                </figcaption>
              </figure>
            </Reveal>
          ))}
        </div>

        <Reveal delay={0.1} className="mt-12">
          <div className="flex flex-wrap items-center justify-center gap-x-10 gap-y-4 opacity-60">
            {["Centre médical de Kinshasa", "Clinique Sainte-Thérèse", "Cabinet du Dr Lukusa", "Pharmacie du Centre", "Polyclinique Maman Mobutu"].map(
              (name) => (
                <span key={name} className="text-sm font-semibold text-ink/60">
                  {name}
                </span>
              )
            )}
          </div>
        </Reveal>
      </div>
    </section>
  );
}