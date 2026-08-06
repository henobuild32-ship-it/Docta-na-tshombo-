import { motion } from "framer-motion";
import { Download, Smartphone, ShieldCheck } from "lucide-react";
import { APP } from "../lib/config";

export default function FinalCta() {
  return (
    <section className="py-20 sm:py-28">
      <div className="mx-auto max-w-5xl px-5 sm:px-8">
        <motion.div
          initial={{ opacity: 0, y: 24 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true, margin: "-80px" }}
          transition={{ duration: 0.7, ease: [0.21, 0.47, 0.32, 0.98] }}
          className="relative overflow-hidden rounded-[2.5rem] bg-ink px-8 py-14 sm:px-14 sm:py-20 text-center text-white"
        >
          <div className="pointer-events-none absolute -left-24 -top-24 h-72 w-72 rounded-full bg-brand-500/25 blur-3xl" />
          <div className="pointer-events-none absolute -bottom-24 -right-24 h-72 w-72 rounded-full bg-accent-500/15 blur-3xl" />

          <div className="relative">
            <span className="inline-flex items-center gap-2 rounded-full border border-white/15 bg-white/5 px-4 py-1.5 text-xs font-semibold text-brand-200">
              <ShieldCheck className="h-3.5 w-3.5" />
              Application officielle
            </span>
            <h2 className="mx-auto mt-6 max-w-2xl text-3xl sm:text-4xl md:text-5xl font-semibold tracking-tight leading-[1.1] text-balance">
              Votre santé mérite mieux qu'une file d'attente
            </h2>
            <p className="mx-auto mt-4 max-w-xl text-lg text-white/70">
              Rejoignez des milliers d'utilisateurs qui prennent soin d'eux avec{" "}
              {APP.name}. Installation en quelques minutes.
            </p>

            <div className="mt-9 flex flex-col sm:flex-row items-center justify-center gap-3">
              <a
                href={APP.apkUrl}
                className="inline-flex items-center gap-2 rounded-full bg-brand-500 px-8 py-4 text-sm font-bold text-white hover:bg-brand-400 hover:-translate-y-0.5 transition-all shadow-glow"
              >
                <Download className="h-4 w-4" />
                Télécharger l'APK ({APP.apkSize})
              </a>
              <span className="inline-flex items-center gap-2 text-sm text-white/70">
                <Smartphone className="h-4 w-4" />
                {APP.android}
              </span>
            </div>

            <p className="mt-6 text-xs text-white/40">
              Fichier vérifié et signé · {APP.language}
            </p>
          </div>
        </motion.div>
      </div>
    </section>
  );
}