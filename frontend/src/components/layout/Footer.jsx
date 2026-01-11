import { useTranslation } from "react-i18next";
import { FiGithub, FiLinkedin, FiMail } from "react-icons/fi";
import Container from "../../components/ui/Container";

export default function Footer() {
  const { t } = useTranslation();
  const year = new Date().getFullYear();

  const socialLinks = [
    { icon: <FiMail />, href: "mailto:contacto@martiniano.dev", label: t("footer.email") },
    { icon: <FiLinkedin />, href: "https://www.linkedin.com", label: t("footer.linkedin") },
    { icon: <FiGithub />, href: "https://github.com", label: t("footer.github") },
  ];

  return (
    <footer className="bg-white dark:bg-slate-950 border-t border-slate-200 dark:border-slate-800 py-12">
      <Container>
        <div className="flex flex-col md:flex-row items-center justify-between gap-6">
          
          <div className="text-center md:text-left">
            <span className="text-lg font-bold font-heading text-slate-900 dark:text-white block">
              martiniano.dev
            </span>
            <span className="text-sm text-slate-500 dark:text-slate-400 mt-1 block">
              © {year} {t("footer.rights")}
            </span>
          </div>

          <div className="flex gap-6">
            {socialLinks.map((link) => (
              <a
                key={link.label}
                href={link.href}
                target="_blank"
                rel="noreferrer"
                aria-label={link.label}
                className="text-slate-400 hover:text-primary-600 dark:hover:text-primary-400 transition-colors transform hover:scale-110"
              >
                <div className="w-6 h-6">
                  {link.icon}
                </div>
              </a>
            ))}
          </div>
        </div>
      </Container>
    </footer>
  );
}
