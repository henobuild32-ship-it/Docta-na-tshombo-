import { useEffect, useState } from "react";
import { AnimatePresence, motion } from "framer-motion";
import { Menu, X, Download } from "lucide-react";
import { Logo } from "./Logo";
import { NAV_LINKS, APP } from "../lib/config";

export default function Navbar() {
  const [scrolled, setScrolled] = useState(false);
  const [open, setOpen] = useState(false);

  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 12);
    onScroll();
    window.addEventListener("scroll", onScroll, { passive: true });
    return () => window.removeEventListener("scroll", onScroll);
  }, []);

  return (
    <header
      className="fixed inset-x-0 top-0 z-50 transition-all duration-300"
      style={{
        boxShadow: scrolled ? "0 8px 30px -12px rgb(12 27 26 / 0.12)" : "none",
      }}
    >
      <div
        className={`transition-colors duration-300 ${
          scrolled ? "bg-cream/90 backdrop-blur-md" : "bg-transparent"
        }`}
      >
        <nav className="mx-auto max-w-6xl px-5 sm:px-8">
          <div className="flex h-16 items-center justify-between">
            <Logo />

            <div className="hidden md:flex items-center gap-8">
              {NAV_LINKS.map((l) => (
                <a
                  key={l.href}
                  href={l.href}
                  className="text-sm font-medium text-ink-soft hover:text-ink transition-colors"
                >
                  {l.label}
                </a>
              ))}
            </div>

            <div className="hidden md:block">
              <a
                href={APP.apkUrl}
                className="inline-flex items-center gap-2 rounded-full bg-ink text-white px-5 py-2.5 text-sm font-semibold hover:bg-brand-700 transition-colors"
              >
                <Download className="h-4 w-4" />
                Télécharger l'APK
              </a>
            </div>

            <button
              className="md:hidden grid place-items-center h-10 w-10 text-ink"
              onClick={() => setOpen((v) => !v)}
              aria-label="Menu"
            >
              {open ? <X className="h-6 w-6" /> : <Menu className="h-6 w-6" />}
            </button>
          </div>
        </nav>

        <AnimatePresence>
          {open && (
            <motion.div
              initial={{ opacity: 0, height: 0 }}
              animate={{ opacity: 1, height: "auto" }}
              exit={{ opacity: 0, height: 0 }}
              className="md:hidden overflow-hidden bg-cream/95 backdrop-blur-md border-t border-ink/5"
            >
              <div className="flex flex-col gap-1 px-6 py-4">
                {NAV_LINKS.map((l) => (
                  <a
                    key={l.href}
                    href={l.href}
                    onClick={() => setOpen(false)}
                    className="py-3 text-[15px] font-medium text-ink-soft hover:text-brand-700"
                  >
                    {l.label}
                  </a>
                ))}
                <a
                  href={APP.apkUrl}
                  className="mt-3 inline-flex items-center justify-center gap-2 rounded-full bg-ink text-white px-5 py-3 text-sm font-semibold"
                >
                  <Download className="h-4 w-4" />
                  Télécharger l'APK
                </a>
              </div>
            </motion.div>
          )}
        </AnimatePresence>
      </div>
    </header>
  );
}