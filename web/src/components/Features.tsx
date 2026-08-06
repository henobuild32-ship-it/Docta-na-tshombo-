import { Reveal, SectionTitle } from "./ui/primitives";
import {
  CalendarCheck,
  Video,
  Pill,
  FileText,
  Bell,
  Star,
  Users,
  Languages,
} from "lucide-react";

export default function Features() {
  return (
    <section id="fonctionnalites" className="py-20 sm:py-28 bg-white">
      <div className="mx-auto max-w-6xl px-5 sm:px-8">
        <SectionTitle
          eyebrow="Fonctionnalités"
          title="Tout ce qu'il faut pour prendre soin de sa santé"
          description="Une application complète, conçue pour couvrir l'ensemble du parcours de soins — de la prise de rendez-vous au suivi du traitement."
        />

        <div className="mt-14 grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
          {[
            {
              icon: CalendarCheck,
              title: "Rendez-vous simplifiés",
              text: "Réservez, modifiez ou annulez vos rendez-vous en quelques gestes, avec rappel automatique avant la consultation.",
            },
            {
              icon: Video,
              title: "Téléconsultation",
              text: "Consultez un médecin à distance par appel vidéo, même lorsque le déplacement jusqu'à la structure de santé est impossible.",
            },
            {
              icon: FileText,
              title: "Ordonnances numériques",
              text: "Vos ordonnances, examens et résultats sont centralisés, lisibles et partageables avec n'importe quel professionnel de santé.",
            },
            {
              icon: Pill,
              title: "Rappels de médicaments",
              text: "Un assistant vous rappelle chaque prise, pour ne plus jamais oublier un traitement, même en cas de charge importante.",
            },
            {
              icon: Users,
              title: "Professionnels de confiance",
              text: "Médecins, spécialistes et structures de santé vérifiés, avec notation et avis des patients pour guider votre choix.",
            },
            {
              icon: Bell,
              title: "Notifications intelligentes",
              text: "Rappels de rendez-vous, nouvelles ordonnances et messages reçus au bon moment — même avec une connexion instable.",
            },
            {
              icon: Languages,
              title: "Multilingue",
              text: "Français, lingala et swahili disponibles pour rendre la santé accessible à toutes et à tous.",
            },
            {
              icon: Star,
              title: "Suivi personnalisé",
              text: "Votre historique médical, vos constantes et vos prochaines étapes de soin regroupés dans un espace unique.",
            },
          ].map((f, i) => (
            <Reveal key={f.title} delay={(i % 4) * 0.08}>
              <div className="group h-full rounded-2xl border border-ink/10 bg-cream p-6 shadow-card transition-all hover:-translate-y-1 hover:border-brand-200 hover:bg-white hover:shadow-lg">
                <span className="grid h-11 w-11 place-items-center rounded-xl bg-brand-600 text-white shadow-glow">
                  <f.icon className="h-5 w-5" />
                </span>
                <h3 className="mt-4 text-base font-semibold text-ink">{f.title}</h3>
                <p className="mt-2 text-sm leading-relaxed text-ink-soft">{f.text}</p>
              </div>
            </Reveal>
          ))}
        </div>
      </div>
    </section>
  );
}