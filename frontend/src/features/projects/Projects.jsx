import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { motion } from "framer-motion";
import ProjectCard from "./ProjectCard";
import projectsData from "../../data/projects.json";
import Section from "../../components/ui/Section";
import Container from "../../components/ui/Container";

export default function Projects() {
  const [projects, setProjects] = useState([]);
  const [error, setError] = useState(null);
  const { t } = useTranslation();

  useEffect(() => {
    // Simulating data check or future async load
    try {
      setProjects(projectsData);
    } catch (err) {
      setError(err.message);
    }
  }, []);

  return (
    <Section id="projects">
      <Container>
        <div className="text-center max-w-3xl mx-auto mb-16">
          <motion.h2
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            className="text-3xl md:text-4xl font-bold font-heading text-slate-900 dark:text-slate-100 mb-4"
          >
            {t("projects.title")}
          </motion.h2>
          <motion.p
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            transition={{ delay: 0.1 }}
            className="text-lg text-slate-600 dark:text-slate-400"
          >
            {t("projects.subtitle")}
          </motion.p>
        </div>

        {error && (
          <div className="p-4 bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 rounded-lg text-center">
            {t("projects.error")} {error}
          </div>
        )}

        <div className="grid gap-8 sm:grid-cols-2 lg:grid-cols-3">
          {projects.map((p, index) => (
            <ProjectCard key={p.id} project={p} index={index} />
          ))}
        </div>
      </Container>
    </Section>
  );
}
