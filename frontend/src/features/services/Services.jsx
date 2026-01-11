import { useTranslation } from "react-i18next";
import { motion } from "framer-motion";
import { FiLayout, FiServer, FiTrendingUp } from "react-icons/fi";
import Section from "../../components/ui/Section";
import Container from "../../components/ui/Container";

export default function Services() {
  const { t } = useTranslation();

  const services = [
    {
      id: "frontend",
      icon: <FiLayout className="w-8 h-8" />,
      title: "Fullstack Development",
      description: "Construyo aplicaciones web end-to-end utilizando React, Node.js y Spring Boot con foco en UX.",
      color: "bg-blue-500",
    },
    {
      id: "architecture",
      icon: <FiServer className="w-8 h-8" />,
      title: "Arquitectura de Sistemas",
      description: "Diseño sistemas distribuidos, microservicios y APIs RESTful escalables y seguros.",
      color: "bg-violet-500",
    },
    {
      id: "consulting",
      icon: <FiTrendingUp className="w-8 h-8" />,
      title: "Consultoría Técnica",
      description: "Auditoría de código, optimización de performance, SEO técnico y mentoring de equipos.",
      color: "bg-cyan-500",
    },
  ];

  return (
    <Section id="services" background="alternate">
      <Container>
        <div className="text-center max-w-2xl mx-auto mb-16">
          <motion.h2
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            className="text-3xl md:text-4xl font-bold font-heading text-slate-900 dark:text-slate-100 mb-4"
          >
            {t("services.title")}
          </motion.h2>
          <motion.p
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            transition={{ delay: 0.1 }}
            className="text-lg text-slate-600 dark:text-slate-400"
          >
            {t("services.subtitle")}
          </motion.p>
        </div>

        <div className="grid md:grid-cols-3 gap-8">
          {services.map((service, index) => (
            <motion.div
              key={service.id}
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true }}
              transition={{ delay: index * 0.1 }}
              whileHover={{ y: -5 }}
              className="bg-white dark:bg-slate-800 p-8 rounded-2xl shadow-lg border border-slate-100 dark:border-slate-700 relative overflow-hidden group"
            >
              <div className={`absolute top-0 right-0 w-32 h-32 bg-gradient-to-br from-primary-500/10 to-transparent rounded-bl-full -mr-16 -mt-16 transition-transform group-hover:scale-150 duration-500`} />
              
              <div className="relative z-10">
                <div className={`w-14 h-14 rounded-xl ${service.color} flex items-center justify-center text-white mb-6 shadow-lg shadow-primary-500/20`}>
                  {service.icon}
                </div>
                <h3 className="text-xl font-bold text-slate-900 dark:text-white mb-3 font-heading">
                  {service.title}
                </h3>
                <p className="text-slate-600 dark:text-slate-400 leading-relaxed">
                  {service.description}
                </p>
              </div>
            </motion.div>
          ))}
        </div>
      </Container>
    </Section>
  );
}
