import { useTranslation } from "react-i18next";
import { motion } from "framer-motion";
import educationData from "../../data/education.json";
import Section from "../../components/ui/Section";
import Container from "../../components/ui/Container";

export default function Education() {
  const { t } = useTranslation();
  const studies = educationData.map((study) => ({
    ...study,
    degree: t(study.degree),
    institution: t(study.institution),
  }));

  return (
    <Section id="education">
      <Container>
        <div className="max-w-3xl mx-auto">
          <motion.h2
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            className="text-3xl md:text-4xl font-bold font-heading text-slate-900 dark:text-slate-100 mb-8 text-center"
          >
            {t("education.title")}
          </motion.h2>

          <div className="space-y-6">
            {studies.map((study, idx) => (
              <motion.div
                key={idx}
                initial={{ opacity: 0, x: -20 }}
                whileInView={{ opacity: 1, x: 0 }}
                viewport={{ once: true }}
                transition={{ delay: idx * 0.1 }}
                className="bg-white dark:bg-slate-800 p-6 rounded-xl shadow-sm border border-slate-100 dark:border-slate-700 hover:shadow-md transition-shadow"
              >
                <div className="flex flex-col md:flex-row md:items-center justify-between gap-2 mb-2">
                  <h3 className="text-xl font-bold text-primary-600 dark:text-primary-400 font-heading">
                    {study.degree}
                  </h3>
                  <span className="text-sm font-medium px-3 py-1 bg-slate-100 dark:bg-slate-700 text-slate-600 dark:text-slate-300 rounded-full w-fit">
                    {study.year}
                  </span>
                </div>
                
                <div className="text-slate-700 dark:text-slate-300 font-medium mb-2">
                  {study.institution}
                </div>
                
                {study.link && (
                  <a
                    href={study.link}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="inline-flex items-center text-sm text-accent-600 dark:text-accent-400 hover:underline mt-2"
                  >
                    {t("education.career_link")} →
                  </a>
                )}
              </motion.div>
            ))}
          </div>
        </div>
      </Container>
    </Section>
  );
}
