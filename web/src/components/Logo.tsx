import { APP } from "../lib/config";

export function Logo({ light = false }: { light?: boolean }) {
  return (
    <a href="#top" className="flex items-center gap-2.5" aria-label={APP.name}>
      <span className="relative grid place-items-center h-9 w-9 rounded-xl bg-brand-600 shadow-glow">
        <svg viewBox="0 0 24 24" className="h-5 w-5" fill="none" aria-hidden="true">
          <path
            d="M12 20s6-4.6 6-9.2a3.4 3.4 0 0 0-6-2.2 3.4 3.4 0 0 0-6 2.2C6 15.4 12 20 12 20Z"
            stroke="white"
            strokeWidth="1.7"
            strokeLinejoin="round"
          />
          <path
            d="M12 19v-5.2m0 0-2-1.4-1.2 2m3.2-0.6 2-1.4 1.2 2"
            stroke="white"
            strokeWidth="1.7"
            strokeLinecap="round"
            strokeLinejoin="round"
          />
        </svg>
      </span>
      <span
        className={`text-[15px] font-semibold tracking-tight ${
          light ? "text-white" : "text-ink"
        }`}
      >
        {APP.name}
      </span>
    </a>
  );
}