import { Reveal, SectionTitle } from "./ui/primitives";
import { Check, X, Sparkles } from "lucide-react";

export default function Comparison() {
  const criteria = [
    "Rendez-vous en ligne 24/7",
    "Téléconsultation vidéo",
    "Ordonnances numériques",
    "Rappels de médicaments",
    "Mode hors-ligne",
    "Messagerie sécurisée",
    "Historique médical centralisé",
    "Disponible en français, lingala et swahili",
  ];

  return (
    <section className="py-20 sm:py-28 bg-white">
      <div className="mx-auto max-w-5xl px-5 sm:px-8">
        <SectionTitle
          eyebrow="Comparatif"
          title="Pourquoi choisir Docta na Tshombo ?"
          description="Une seule application regroupe ce que les approches traditionnelles ne peuvent pas offrir — ni les appels téléphoniques, ni les files d'attente, ni les carnets papier."
        />

        <Reveal className="mt-14">
          <div className="overflow-x-auto rounded-3xl border border-ink/10 bg-white shadow-2xl">
            <table className="w-full min-w-[640px] border-collapse text-left">
              <thead>
                <tr className="border-b border-ink/10">
                  <th className="p-5 text-sm font-semibold text-ink">Critères</th>
                  <th className="p-5 text-sm font-semibold text-ink-soft text-center">
                    Parcours classique
                  </th>
                  <th className="bg-brand-600 p-5 text-center">
                    <span className="inline-flex items-center gap-1.5 text-sm font-bold text-white">
                      <Sparkles className="h-4 w-4" />
                      Docta na Tshombo
                    </span>
                  </th>
                </tr>
              </thead>
              <tbody>
                {criteria.map((c, i) => (
                  <tr
                    key={c}
                    className={`border-b border-ink/5 ${
                      i % 2 === 1 ? "bg-cream/50" : ""
                    }`}
                  >
                    <td className="p-4 pl-5 text-sm text-ink">{c}</td>
                    <td className="p-4 text-center">
                      <X className="mx-auto h-4 w-4 text-ink/30" />
                    </td>
                    <td className="bg-brand-600/5 p-4 text-center">
                      <Check className="mx-auto h-4 w-4 text-brand-600" />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </Reveal>
      </div>
    </section>
  );
}