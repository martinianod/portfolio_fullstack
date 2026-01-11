import { useTranslation } from "react-i18next";
import { useTheme } from "../../contexts/ThemeContext";
import logo from "../../assets/martiniano-dev-logo.png";

export default function Navbar({ onLangToggle }) {
  const { t, i18n } = useTranslation();
  const { theme, toggleTheme } = useTheme();

  return (
    <header className="sticky top-0 z-50 backdrop-blur supports-[backdrop-filter]:bg-white/70 dark:supports-[backdrop-filter]:bg-slate-950/70 border-b border-slate-200 dark:border-slate-800">
      <nav className="container flex items-center justify-between py-4">
        <a
          href="#home"
          className="flex items-center gap-2 font-semibold text-lg text-slate-900 dark:text-slate-100"
        >
          <img src={logo} alt="Logo" className="w-7 h-7 rounded-full shadow" />
          martiniano.dev
        </a>
        <ul className="hidden md:flex gap-6 text-sm">
          <li>
            <a
              href="#home"
              className="text-slate-600 dark:text-slate-300 hover:text-violet-600 dark:hover:text-violet-400 transition-colors"
            >
              {t("nav.home")}
            </a>
          </li>
          <li>
            <a
              href="#projects"
              className="text-slate-600 dark:text-slate-300 hover:text-violet-600 dark:hover:text-violet-400 transition-colors"
            >
              {t("nav.projects")}
            </a>
          </li>
          <li>
            <a
              href="#skills"
              className="text-slate-600 dark:text-slate-300 hover:text-violet-600 dark:hover:text-violet-400 transition-colors"
            >
              {t("nav.skills")}
            </a>
          </li>
          <li>
            <a
              href="#education"
              className="text-slate-600 dark:text-slate-300 hover:text-violet-600 dark:hover:text-violet-400 transition-colors"
            >
              {t("nav.education")}
            </a>
          </li>
          <li>
            <a
              href="#contact"
              className="text-slate-600 dark:text-slate-300 hover:text-violet-600 dark:hover:text-violet-400 transition-colors"
            >
              {t("nav.contact")}
            </a>
          </li>
        </ul>
        <div className="flex items-center gap-3">
          <button
            onClick={toggleTheme}
            className="w-9 h-9 flex items-center justify-center rounded-lg border border-slate-300 dark:border-slate-700 hover:border-violet-500 dark:hover:border-violet-500 transition-colors"
            aria-label="toggle theme"
          >
            {theme === "dark" ? (
              <svg
                className="w-4 h-4 text-slate-600 dark:text-slate-300"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z"
                />
              </svg>
            ) : (
              <svg
                className="w-4 h-4 text-slate-600 dark:text-slate-300"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z"
                />
              </svg>
            )}
          </button>
          <button
            className="w-9 h-9 flex items-center justify-center rounded-lg border border-slate-300 dark:border-slate-700 text-xs text-slate-600 dark:text-slate-300 hover:border-violet-500 dark:hover:border-violet-500 transition-colors"
            onClick={() => onLangToggle(i18n.language === "es" ? "en" : "es")}
            aria-label="toggle language"
          >
            {i18n.language === "es" ? "EN" : "ES"}
          </button>
          <a
            href="#contact"
            className="btn text-xs md:text-sm bg-gradient-to-r from-violet-600 via-fuchsia-500 to-pink-500 text-white font-semibold px-4 py-2 rounded-lg shadow transition-all duration-200 hover:scale-105"
          >
            {t("hero.cta")}
          </a>
        </div>
      </nav>
    </header>
  );
}
