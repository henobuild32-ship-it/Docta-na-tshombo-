import { useState } from "react";
import { AnimatePresence, motion } from "framer-motion";
import { Plus } from "lucide-react";
import { Reveal, SectionTitle } from "./ui/primitives";
import { APP } from "../lib/config";

const faqs = [
  {
    q: "Sur quels téléphones l'application fonctionne-t-elle ?",
    a: `L'application est conçue pour Android (${APP.android}). Elle est optimisée pour fonctionner même sur les appareils d'entrée de gamme et avec une connexion limitée, avec un mode hors-ligne pour les fonctionnalités essentielles.`,
  },
  {
    q: "Que se passe-t-il si je n'ai pas de connexion internet ?",
    a: "Vous pouvez consulter vos rendez-vous, vos ordonnances et vos rappels de traitement hors-ligne. L'application se synchronise automatiquement dès que la connexion revient.",
  },
  {
    q: "Mes données de santé sont-elles protégées ?",
    a: "Oui. Vos informations médicales sont stockées de manière sécurisée, et vous gardez le contrôle sur leur partage avec les professionnels de santé que vous consultez.",
  },
  {
    q: "Puis-je utiliser l'application en lingala ou en swahili ?",
    a: "Oui. L'interface est disponible en français, en lingala et en swahili pour répondre aux besoins des utilisateurs à travers toute la RDC.",
  },
  {
    q: "Comment fonctionne le rappel de médicaments ?",
    a: "Après réception de votre ordonnance, vous programmez simplement vos rappels de prise. L'application vous notifie à chaque heure de prise pour que vous ne les oubliiez jamais.",
  },
  {
    q: "L'application fait-elle office de diagnostic médical ?",
    a: "Non. Docta na Tshombo facilite la mise en relation avec des professionnels de santé et le suivi de vos traitements, mais ne remplace jamais un avis médical professionnel.",
  },
  {
    q: "Comment installer le fichier APK ?",
    a: "Téléchargez le fichier APK depuis le bouton dédié, puis autorisez l'installation d'applications provenant de sources inconnues sur votre téléphone. Après installation, l'application est immédiatement utilisable.",
  },
];

export default function Faq() {
  const [open, setOpen] = useState<number | null>(0);

  return (
    <section id="faq" className="py-20 sm:py-28">
      <div className="mx-auto max-w-3xl px-5 sm:px-8">
        <SectionTitle
          eyebrow="FAQ"
          title="Questions fréquentes"
          description="Tout ce qu'il faut savoir avant de commencer."
        />

        <div className="mt-12 space-y-3">
          {faqs.map((f, i) => {
            const isOpen = open === i;
            return (
              <Reveal key={f.q} delay={i * 0.04}>
                <div className="overflow-hidden rounded-2xl border border-ink/10 bg-white shadow-card">
                  <button
                    className="flex w-full items-center justify-between gap-4 px-5 py-4 text-left"
                    onClick={() => setOpen(isOpen ? null : i)}
                    aria-expanded={isOpen}
                  >
                    <span className="text-[15px] font-semibold text-ink">{f.q}</span>
                    <motion.span
                      animate={{ rotate: isOpen ? 45 : 0 }}
                      transition={{ duration: 0.2 }}
                      className="grid h-7 w-7 shrink-0 place-items-center rounded-full bg-brand-50 text-brand-700"
                    >
                      <Plus className="h-4 w-4" />
                    </motion.span>
                  </button>
                  <AnimatePresence initial={false}>
                    {isOpen && (
                      <motion.div
                        initial={{ height: 0, opacity: 0 }}
                        animate={{ height: "auto", opacity: 1 }}
                        exit={{ height: 0, opacity: 0 }}
                        transition={{ duration: 0.25 }}
                      >
                        <p className="px-5 pb-5 text-sm leading-relaxed text-ink-soft">
                          {f.a}
                        </p>
                      </motion.div>
                    )}
                  </AnimatePresence>
                </div>
              </Reveal>
            );
          })}
        </div>
      </div>
    </section>
  );
}