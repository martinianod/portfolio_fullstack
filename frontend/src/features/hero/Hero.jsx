import { useTranslation } from "react-i18next";
import { motion } from "framer-motion";
import Button from "../../components/ui/Button";
import Container from "../../components/ui/Container";

export default function Hero() {
  const { t } = useTranslation();

  return (
    <section
      id="home"
      className="relative min-h-[90vh] flex items-center pt-20 overflow-hidden bg-slate-50 dark:bg-slate-950"
    >
      {/* Background Elements */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute top-0 left-1/4 w-96 h-96 bg-primary-500/10 rounded-full blur-3xl mix-blend-multiply dark:mix-blend-screen animate-blob" />
        <div className="absolute top-0 right-1/4 w-96 h-96 bg-accent-500/10 rounded-full blur-3xl mix-blend-multiply dark:mix-blend-screen animate-blob animation-delay-2000" />
        <div className="absolute -bottom-32 left-1/3 w-96 h-96 bg-pink-500/10 rounded-full blur-3xl mix-blend-multiply dark:mix-blend-screen animate-blob animation-delay-4000" />
        <div className="absolute inset-0 bg-[url('/grid.svg')] opacity-[0.03] dark:opacity-[0.05]" />
      </div>

      <Container className="relative z-10">
        <div className="flex flex-col items-center text-center max-w-4xl mx-auto">
          {/* Badge */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5 }}
            className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-primary-50 dark:bg-primary-900/30 border border-primary-100 dark:border-primary-800 text-primary-600 dark:text-primary-300 text-sm font-medium mb-8"
          >
            <span className="relative flex h-2 w-2">
              <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-primary-400 opacity-75"></span>
              <span className="relative inline-flex rounded-full h-2 w-2 bg-primary-500"></span>
            </span>
            {t("hero.badge")}
          </motion.div>

          {/* Headline */}
          <motion.h1
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.1 }}
            className="text-4xl md:text-6xl lg:text-7xl font-bold font-heading text-slate-900 dark:text-slate-100 tracking-tight leading-tight"
          >
            {t("hero.title")}
          </motion.h1>

          {/* Subtitle */}
          <motion.p
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.2 }}
            className="mt-6 text-lg md:text-xl text-slate-600 dark:text-slate-400 max-w-2xl"
          >
            {t("hero.subtitle")}
          </motion.p>

          {/* CTAs */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.3 }}
            className="mt-10 flex flex-col sm:flex-row gap-4 w-full sm:w-auto"
          >
            <Button href="#contact" variant="primary" size="lg">
              {t("hero.cta_primary")}
            </Button>
            <Button href="#projects" variant="secondary" size="lg">
              {t("hero.cta_secondary")}
            </Button>
          </motion.div>
        </div>

        {/* Tech Stack Strip */}
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ duration: 1, delay: 0.5 }}
          className="mt-24 pt-8 border-t border-slate-200 dark:border-slate-800"
        >
          <p className="text-center text-sm font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider mb-6">
            Tech Stack of Choice
          </p>
          <div className="flex flex-wrap justify-center gap-6 md:gap-10 opacity-70 grayscale transition-all hover:grayscale-0">
            {["java", "spring", "react", "typescript", "nodejs", "amazonwebservices", "docker", "kubernetes", "jenkins", "postgresql", "mysql", "mongodb", "redis", "jest"].map(
              (tech) => {
                let imgUrl = `https://cdn.jsdelivr.net/gh/devicons/devicon/icons/${tech}/${tech}-original.svg`;
                
                // Exemptions for icons that look better in inconsistent versions
                if (tech === "amazonwebservices") {
                  imgUrl = "https://cdn.jsdelivr.net/gh/devicons/devicon/icons/amazonwebservices/amazonwebservices-original-wordmark.svg";
                } else if (tech === "jest") {
                  imgUrl = `https://cdn.jsdelivr.net/gh/devicons/devicon/icons/${tech}/${tech}-plain.svg`;
                }

                return (
                  <img
                    key={tech}
                    src={imgUrl}
                    alt={tech}
                    className="h-8 md:h-10 w-auto transition-transform hover:scale-110"
                    loading="lazy"
                    title={tech.charAt(0).toUpperCase() + tech.slice(1)} // Simple tooltip
                  />
                );
              }
            )}
          </div>
        </motion.div>
      </Container>
    </section>
  );
}
