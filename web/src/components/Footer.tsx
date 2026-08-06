import { Logo } from "./Logo";
import { APP, CREATOR, NAV_LINKS } from "../lib/config";
import { Mail, MapPin, Heart } from "lucide-react";

const resources = [
  { label: "FAQ", href: "#faq" },
  { label: "Comparatif", href: "#application" },
  { label: "Fonctionnalités", href: "#fonctionnalites" },
  { label: "Téléchargement APK", href: APP.apkUrl },
];

export default function Footer() {
  return (
    <footer className="border-t border-ink/10 bg-white">
      <div className="mx-auto max-w-6xl px-5 py-14 sm:px-8">
        <div className="grid gap-10 md:grid-cols-[1.4fr_1fr_1fr]">
          <div>
            <Logo />
            <p className="mt-4 max-w-sm text-sm leading-relaxed text-ink-soft">
              {APP.name} connecte les patients et les professionnels de santé en RDC
              pour rendre les soins accessibles à toutes et à tous.
            </p>
            <div className="mt-5 space-y-1.5 text-sm text-ink-soft">
              <p className="flex items-center gap-2">
                <MapPin className="h-4 w-4 text-brand-600" />
                {CREATOR.location}
              </p>
              <p className="flex items-center gap-2">
                <Mail className="h-4 w-4 text-brand-600" />
                contact@doctanatshombo.com
              </p>
            </div>
          </div>

          <div>
            <h3 className="text-sm font-semibold text-ink">Navigation</h3>
            <ul className="mt-4 space-y-2.5">
              {NAV_LINKS.map((l) => (
                <li key={l.href}>
                  <a
                    href={l.href}
                    className="text-sm text-ink-soft hover:text-brand-700 transition-colors"
                  >
                    {l.label}
                  </a>
                </li>
              ))}
            </ul>
          </div>

          <div>
            <h3 className="text-sm font-semibold text-ink">Ressources</h3>
            <ul className="mt-4 space-y-2.5">
              {resources.map((r) => (
                <li key={r.label}>
                  <a
                    href={r.href}
                    className="text-sm text-ink-soft hover:text-brand-700 transition-colors"
                  >
                    {r.label}
                  </a>
                </li>
              ))}
            </ul>
          </div>
        </div>

        <div className="mt-12 flex flex-col sm:flex-row items-center justify-between gap-3 border-t border-ink/10 pt-6">
          <p className="text-xs text-ink-soft">
            © {new Date().getFullYear()} {APP.name}. Tous droits réservés.
          </p>
          <p className="flex items-center gap-1.5 text-xs text-ink-soft">
            <Heart className="h-3.5 w-3.5 fill-brand-600 text-brand-600" />
            {CREATOR.madeBy}
          </p>
        </div>
      </div>
    </footer>
  );
}