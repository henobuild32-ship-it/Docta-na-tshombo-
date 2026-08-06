import { Reveal, SectionTitle } from "./ui/primitives";
import {
  ResponsiveContainer,
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  BarChart,
  Bar,
} from "recharts";
import {
  Home,
  CalendarCheck,
  Video,
  Pill,
  MessageSquare,
  FileText,
  Bell,
  Activity,
  HeartPulse,
} from "lucide-react";

const vitals = [
  { d: "Lun", tension: 90 },
  { d: "Mar", tension: 82 },
  { d: "Mer", tension: 88 },
  { d: "Jeu", tension: 76 },
  { d: "Ven", tension: 84 },
  { d: "Sam", tension: 80 },
  { d: "Dim", tension: 78 },
];

const weekly = [
  { d: "L", rdv: 4 },
  { d: "M", rdv: 7 },
  { d: "M", rdv: 5 },
  { d: "J", rdv: 9 },
  { d: "V", rdv: 6 },
  { d: "S", rdv: 3 },
  { d: "D", rdv: 2 },
];

export default function Product() {
  return (
    <section id="application" className="py-20 sm:py-28">
      <div className="mx-auto max-w-6xl px-5 sm:px-8">
        <SectionTitle
          eyebrow="L'application"
          title="Un tableau de bord clair, pensé pour le quotidien"
          description="Une interface épurée qui met l'essentiel au premier plan : vos rendez-vous, vos traitements et vos échanges avec les professionnels de santé."
        />

        <Reveal delay={0.1} className="mt-14">
          <div className="relative">
            <div className="pointer-events-none absolute -inset-6 -z-10 rounded-[2.5rem] bg-gradient-to-b from-brand-100/60 to-transparent blur-2xl" />
            <div className="rounded-3xl border border-ink/10 bg-white shadow-2xl overflow-hidden">
              <div className="flex items-center gap-2 border-b border-ink/5 bg-cream/60 px-5 py-3">
                <span className="h-3 w-3 rounded-full bg-red-300" />
                <span className="h-3 w-3 rounded-full bg-amber-300" />
                <span className="h-3 w-3 rounded-full bg-emerald-300" />
                <p className="ml-3 text-xs font-medium text-ink-soft">
                  Docta na Tshombo — Tableau de bord patient
                </p>
              </div>

              <div className="grid md:grid-cols-[220px_1fr]">
                <Sidebar />
                <Dashboard />
              </div>
            </div>
          </div>
        </Reveal>

        <div className="mt-8 grid gap-4 sm:grid-cols-3">
          {[
            { icon: Activity, label: "Suivi des constantes", text: "Tension, rythme cardiaque et plus, visualisés au fil du temps." },
            { icon: MessageSquare, label: "Messagerie sécurisée", text: "Échangez avec votre médecin sans intermédiaire." },
            { icon: HeartPulse, label: "Profil de santé complet", text: "Historique, allergies et traitements en cours réunis." },
          ].map((c) => (
            <Reveal key={c.label}>
              <div className="h-full rounded-2xl border border-ink/10 bg-white p-5 shadow-card">
                <c.icon className="h-5 w-5 text-brand-600" />
                <h3 className="mt-3 text-sm font-semibold text-ink">{c.label}</h3>
                <p className="mt-1 text-sm text-ink-soft">{c.text}</p>
              </div>
            </Reveal>
          ))}
        </div>
      </div>
    </section>
  );
}

function Sidebar() {
  const items = [
    { icon: Home, label: "Accueil", active: true },
    { icon: CalendarCheck, label: "Rendez-vous" },
    { icon: Video, label: "Téléconsultation" },
    { icon: Pill, label: "Traitements" },
    { icon: MessageSquare, label: "Messages" },
    { icon: FileText, label: "Ordonnances" },
  ];
  return (
    <div className="hidden md:flex flex-col gap-1 border-r border-ink/5 bg-cream/40 p-4">
      {items.map((i) => (
        <span
          key={i.label}
          className={`flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium ${
            i.active
              ? "bg-brand-600 text-white shadow-glow"
              : "text-ink-soft hover:bg-ink/5 hover:text-ink"
          }`}
        >
          <i.icon className="h-4 w-4" />
          {i.label}
        </span>
      ))}
      <div className="mt-auto rounded-xl bg-brand-50 p-3">
        <p className="text-xs font-semibold text-brand-700">Rappel</p>
        <p className="mt-1 text-[11px] text-ink-soft">
          Prendre Doliprane 500 mg · 18h00
        </p>
      </div>
    </div>
  );
}

function Dashboard() {
  return (
    <div className="p-5 sm:p-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h3 className="text-lg font-semibold text-ink">Vue d'ensemble</h3>
          <p className="text-xs text-ink-soft">Cette semaine · 12 mars 2026</p>
        </div>
        <span className="inline-flex items-center gap-1.5 rounded-full bg-emerald-50 px-3 py-1 text-xs font-semibold text-emerald-700">
          <span className="h-1.5 w-1.5 rounded-full bg-emerald-500" />
          Plan actif
        </span>
      </div>

      <div className="mt-5 grid gap-4 sm:grid-cols-3">
        {[
          { label: "Rendez-vous", value: "12", delta: "+4 cette semaine" },
          { label: "Téléconsultations", value: "7", delta: "+2 cette semaine" },
          { label: "Traitements suivis", value: "3", delta: "À jour" },
        ].map((s) => (
          <div key={s.label} className="rounded-2xl border border-ink/10 bg-cream/60 p-4">
            <p className="text-xs text-ink-soft">{s.label}</p>
            <p className="mt-1 text-2xl font-bold text-ink">{s.value}</p>
            <p className="mt-0.5 text-[11px] font-medium text-brand-600">{s.delta}</p>
          </div>
        ))}
      </div>

      <div className="mt-4 grid gap-4 lg:grid-cols-2">
        <div className="rounded-2xl border border-ink/10 p-4">
          <div className="flex items-center justify-between">
            <p className="text-sm font-semibold text-ink">Tension artérielle</p>
            <span className="text-[11px] text-ink-soft">7 derniers jours</span>
          </div>
          <div className="mt-3 h-36">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={vitals} margin={{ top: 4, right: 4, left: -18, bottom: 0 }}>
                <defs>
                  <linearGradient id="vitalGrad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stopColor="#0d9488" stopOpacity={0.35} />
                    <stop offset="100%" stopColor="#0d9488" stopOpacity={0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" vertical={false} />
                <XAxis dataKey="d" tick={{ fontSize: 10, fill: "#6b7280" }} axisLine={false} tickLine={false} />
                <YAxis tick={{ fontSize: 10, fill: "#6b7280" }} axisLine={false} tickLine={false} />
                <Tooltip contentStyle={{ fontSize: 11, borderRadius: 12, border: "1px solid #e5e7eb" }} />
                <Area type="monotone" dataKey="tension" stroke="#0d9488" strokeWidth={2} fill="url(#vitalGrad)" />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="rounded-2xl border border-ink/10 p-4">
          <div className="flex items-center justify-between">
            <p className="text-sm font-semibold text-ink">Activité de la semaine</p>
            <span className="text-[11px] text-ink-soft">Consultations</span>
          </div>
          <div className="mt-3 h-36">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={weekly} margin={{ top: 4, right: 4, left: -18, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" vertical={false} />
                <XAxis dataKey="d" tick={{ fontSize: 10, fill: "#6b7280" }} axisLine={false} tickLine={false} />
                <YAxis tick={{ fontSize: 10, fill: "#6b7280" }} axisLine={false} tickLine={false} />
                <Tooltip contentStyle={{ fontSize: 11, borderRadius: 12, border: "1px solid #e5e7eb" }} />
                <Bar dataKey="rdv" fill="#0d9488" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>

      <div className="mt-4 flex items-center gap-3 rounded-2xl border border-brand-200 bg-brand-50 px-4 py-3">
        <Bell className="h-4 w-4 shrink-0 text-brand-600" />
        <p className="text-xs text-ink-soft">
          <span className="font-semibold text-ink">Rappel :</span> consultation de suivi
          avec Dr M. Ilunga demain à 14h30. Un rappel vous sera envoyé automatiquement.
        </p>
      </div>
    </div>
  );
}