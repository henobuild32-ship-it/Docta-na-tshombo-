import { motion } from "framer-motion";

export function Reveal({
  children,
  className,
  delay = 0,
  y = 24,
}: {
  children: React.ReactNode;
  className?: string;
  delay?: number;
  y?: number;
}) {
  return (
    <motion.div
      className={className}
      initial={{ opacity: 0, y }}
      whileInView={{ opacity: 1, y: 0 }}
      viewport={{ once: true, margin: "-80px" }}
      transition={{ duration: 0.6, delay, ease: [0.21, 0.47, 0.32, 0.98] }}
    >
      {children}
    </motion.div>
  );
}

export function Eyebrow({
  children,
  light = false,
}: {
  children: React.ReactNode;
  light?: boolean;
}) {
  return (
    <p
      className={`text-xs font-semibold tracking-[0.2em] uppercase ${
        light ? "text-brand-300" : "text-brand-600"
      }`}
    >
      {children}
    </p>
  );
}

export function SectionTitle({
  eyebrow,
  title,
  description,
  light = false,
  align = "center",
}: {
  eyebrow: string;
  title: string;
  description?: string;
  light?: boolean;
  align?: "center" | "left";
}) {
  const alignment =
    align === "center" ? "text-center items-center" : "text-left items-start";
  return (
    <Reveal className={`flex flex-col gap-4 ${alignment} max-w-2xl mx-auto`}>
      <Eyebrow light={light}>{eyebrow}</Eyebrow>
      <h2
        className={`text-3xl sm:text-4xl md:text-5xl font-semibold tracking-tight leading-[1.1] text-balance ${
          light ? "text-white" : "text-ink"
        }`}
      >
        {title}
      </h2>
      {description && (
        <p
          className={`text-lg leading-relaxed ${
            light ? "text-white/70" : "text-ink-soft"
          }`}
        >
          {description}
        </p>
      )}
    </Reveal>
  );
}

export function ButtonLink({
  href,
  children,
  variant = "primary",
  className = "",
}: {
  href: string;
  children: React.ReactNode;
  variant?: "primary" | "secondary" | "ghost";
  className?: string;
}) {
  const base =
    "inline-flex items-center justify-center gap-2 rounded-full px-6 py-3 text-sm font-semibold transition-all duration-200 focus:outline-none focus-visible:ring-2 focus-visible:ring-brand-500";
  const variants = {
    primary:
      "bg-brand-600 text-white shadow-glow hover:bg-brand-500 hover:-translate-y-0.5",
    secondary:
      "bg-white text-ink border border-ink/10 shadow-card hover:border-brand-300 hover:text-brand-700",
    ghost: "text-brand-700 hover:text-brand-800",
  };
  return (
    <a href={href} className={`${base} ${variants[variant]} ${className}`}>
      {children}
    </a>
  );
}