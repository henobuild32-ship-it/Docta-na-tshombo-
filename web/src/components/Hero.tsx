import { motion } from "framer-motion";
import { Download, PlayCircle, ShieldCheck, Zap, Globe, BellRing } from "lucide-react";
import { APP } from "../lib/config";

export default function Hero() {
  return (
    <section id="top" className="relative overflow-hidden pt-32 pb-20 sm:pt-40 sm:pb-28">
      <div className="pointer-events-none absolute inset-0 -z-10">
        <div className="absolute -top-40 left-1/2 h-[560px] w-[900px] -translate-x-1/2 rounded-full bg-brand-200/40 blur-3xl" />
        <div className="absolute bottom-0 right-0 h-72 w-72 rounded-full bg-accent-500/10 blur-3xl" />
      </div>

      <div className="mx-auto grid max-w-6xl items-center gap-16 px-5 sm:px-8 lg:grid-cols-[1.05fr_0.95fr]">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.7, ease: [0.21, 0.47, 0.32, 0.98] }}
          className="text-center lg:text-left"
        >
          <div className="inline-flex items-center gap-2 rounded-full border border-brand-200 bg-white/70 px-3.5 py-1.5 text-xs font-medium text-brand-700 shadow-card backdrop-blur">
            <span className="h-2 w-2 rounded-full bg-brand-500 animate-pulse" />
            Application officielle · {APP.android}
          </div>

          <h1 className="mt-6 text-4xl sm:text-5xl md:text-6xl font-semibold tracking-tight leading-[1.05] text-balance">
            Des soins de santé{" "}
            <span className="text-brand-700">à portée de main</span>, où que vous soyez
          </h1>

          <p className="mx-auto mt-5 max-w-xl text-lg text-ink-soft lg:mx-0">
            {APP.name} connecte patients et professionnels de santé en RDC : prise de
            rendez-vous, téléconsultation et suivi de traitement, le tout dans une seule
            application pensée pour la réalité du terrain.
          </p>

          <div className="mt-8 flex flex-col sm:flex-row items-center justify-center lg:justify-start gap-3">
            <a
              href={APP.apkUrl}
              className="inline-flex items-center gap-2 rounded-full bg-brand-600 px-7 py-3.5 text-sm font-semibold text-white shadow-glow hover:bg-brand-500 hover:-translate-y-0.5 transition-all"
            >
              <Download className="h-4 w-4" />
              Télécharger l'application
            </a>
            <a
              href="#application"
              className="inline-flex items-center gap-2 rounded-full border border-ink/10 bg-white px-7 py-3.5 text-sm font-semibold text-ink shadow-card hover:border-brand-300 transition-colors"
            >
              <PlayCircle className="h-4 w-4 text-brand-600" />
              Découvrir l'application
            </a>
          </div>

          <div className="mt-10 grid grid-cols-3 gap-4 max-w-md mx-auto lg:mx-0">
            {[
              { icon: ShieldCheck, label: "Données sécurisées" },
              { icon: Zap, label: "Mise en relation rapide" },
              { icon: Globe, label: "Pensé pour le Congo" },
            ].map((s) => (
              <div key={s.label} className="text-center lg:text-left">
                <s.icon className="mx-auto lg:mx-0 h-5 w-5 text-brand-600" />
                <p className="mt-2 text-xs font-medium text-ink-soft">{s.label}</p>
              </div>
            ))}
          </div>
        </motion.div>

        <motion.div
          initial={{ opacity: 0, y: 30 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.8, delay: 0.15, ease: [0.21, 0.47, 0.32, 0.98] }}
          className="relative mx-auto w-[300px] sm:w-[340px]"
        >
          <PhoneMock />
        </motion.div>
      </div>
    </section>
  );
}

function PhoneMock() {
  return (
    <div className="relative rounded-[2.6rem] border border-ink/10 bg-white p-2.5 shadow-2xl">
      <div className="overflow-hidden rounded-[2.1rem] bg-gradient-to-b from-brand-800 to-brand-950 text-white">
        <div className="px-5 pt-6 pb-5">
          <div className="flex items-center justify-between">
            <p className="text-[11px] font-medium text-brand-200">Bonjour, Awa 👋</p>
            <BellRing className="h-4 w-4 text-brand-200" />
          </div>
          <h3 className="mt-1 text-lg font-semibold">Tableau de bord</h3>
        </div>
        <div className="px-4 pb-5">
          <div className="rounded-2xl bg-white/10 p-4">
            <p className="text-[11px] text-brand-200">Prochain rendez-vous</p>
            <div className="mt-2 flex items-end justify-between">
              <div>
                <p className="text-sm font-semibold">Dr M. Ilunga</p>
                <p className="text-[11px] text-brand-200">Cardiologie · 14h30</p>
              </div>
              <span className="rounded-full bg-brand-400/20 px-2.5 py-1 text-[10px] font-semibold text-brand-100">
                Rappel programmé
              </span>
            </div>
          </div>

          <div className="mt-3 grid grid-cols-2 gap-2.5">
            {[
              { label: "Rendez-vous", value: "3", color: "bg-brand-400/20 text-brand-100" },
              { label: "Ordonnances", value: "2", color: "bg-accent-500/20 text-amber-100" },
            ].map((c) => (
              <div key={c.label} className={`rounded-xl p-3 ${c.color}`}>
                <p className="text-lg font-bold">{c.value}</p>
                <p className="text-[10px] opacity-90">{c.label}</p>
              </div>
            ))}
          </div>

          <div className="mt-3 flex items-center gap-2 rounded-xl bg-white/10 px-3 py-2.5">
            <span className="grid h-8 w-8 place-items-center rounded-lg bg-brand-500 text-[11px] font-bold">
              14:30
            </span>
            <div className="flex-1">
              <p className="text-[11px] font-medium">Téléconsultation</p>
              <p className="text-[10px] text-brand-200">Rejoindre la visio</p>
            </div>
            <PlayCircle className="h-5 w-5 text-brand-300" />
          </div>
        </div>
      </div>
      <div className="absolute left-1/2 top-2 h-1.5 w-16 -translate-x-1/2 rounded-full bg-ink/15" />
    </div>
  );
}