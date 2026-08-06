import { Reveal, SectionTitle } from "./ui/primitives";
import { Clock3, Stethoscope, Pill, WifiOff, ArrowRight } from "lucide-react";

export default function Problem() {
  return (
    <section id="probleme" className="py-20 sm:py-28">
      <div className="mx-auto max-w-6xl px-5 sm:px-8">
        <SectionTitle
          eyebrow="Le constat"
          title="Accéder à un médecin ne devrait pas être un parcours du combattant"
          description="Dans de nombreuses régions de RDC, prendre un rendez-vous médical reste un défi quotidien : déplacements longs, files d'attente interminables, ordonnances perdues et informations éparpillées."
        />

        <div className="mt-14 grid gap-6 md:grid-cols-2 lg:grid-cols-4">
          {[
            {
              icon: Clock3,
              title: "Des heures d'attente",
              text: "Des journées entières perdues dans les couloirs d'hôpitaux pour un rendez-vous qui n'arrive jamais à l'heure.",
            },
            {
              icon: WifiOff,
              title: "Connexion instable",
              text: "Les outils de santé en ligne échouent quand la connexion est faible — l'application fonctionne aussi hors-ligne.",
            },
            {
              icon: Stethoscope,
              title: "Suivi fragmenté",
              text: "Aucun historique partagé : chaque consultation repart de zéro, sans contexte ni continuité des soins.",
            },
            {
              icon: Pill,
              title: "Traitements oubliés",
              text: "Les rappels de médicaments existent rarement, et les ordonnances finissent trop souvent perdues ou illisibles.",
            },
          ].map((c, i) => (
            <Reveal key={c.title} delay={i * 0.08}>
              <div className="group h-full rounded-2xl border border-ink/10 bg-white p-6 shadow-card transition-all hover:-translate-y-1 hover:border-brand-200 hover:shadow-lg">
                <span className="grid h-11 w-11 place-items-center rounded-xl bg-brand-50 text-brand-600">
                  <c.icon className="h-5 w-5" />
                </span>
                <h3 className="mt-4 text-base font-semibold text-ink">{c.title}</h3>
                <p className="mt-2 text-sm leading-relaxed text-ink-soft">{c.text}</p>
              </div>
            </Reveal>
          ))}
        </div>

        <Reveal delay={0.2} className="mt-12">
          <div className="rounded-3xl bg-ink p-8 sm:p-10 text-center text-white relative overflow-hidden">
            <div className="pointer-events-none absolute -right-20 -top-20 h-64 w-64 rounded-full bg-brand-500/20 blur-3xl" />
            <p className="text-xs font-semibold uppercase tracking-[0.2em] text-brand-300">
              La solution
            </p>
            <h3 className="mx-auto mt-3 max-w-2xl text-2xl sm:text-3xl font-semibold tracking-tight text-balance">
              Un espace unique qui réunit le patient et son médecin, pensé pour les
              réalités du terrain congolais
            </h3>
            <a
              href="#fonctionnalites"
              className="mt-6 inline-flex items-center gap-2 rounded-full bg-brand-600 px-6 py-3 text-sm font-semibold text-white hover:bg-brand-500 transition-colors"
            >
              Voir les fonctionnalités
              <ArrowRight className="h-4 w-4" />
            </a>
          </div>
        </Reveal>
      </div>
    </section>
  );
}