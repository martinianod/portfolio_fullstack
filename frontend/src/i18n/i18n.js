import i18n from "i18next";
import { initReactI18next } from "react-i18next";

const resources = {
  es: {
    translation: {
      nav: {
        home: "Inicio",
        projects: "Casos de Éxito",
        services: "Soluciones",
        skills: "Habilidades",
        about: "Sobre Mí",
        contact: "Contacto",
        education: "Educación"
      },
      hero: {
        title: "Construyo Productos Digitales Escalables para Empresas que Quieren Crecer",
        subtitle:
          "Senior Fullstack Developer especializado en transformar ideas complejas en software de alto rendimiento.",
        cta: "Hablemos hoy",
        cta_primary: "Hablemos hoy",
        cta_secondary: "Ver mis resultados",
        badge: "Disponible para nuevos proyectos",
      },
      projects: {
        title: "Casos de Éxito",
        subtitle: "Soluciones reales que generaron impacto de negocio",
        error: "Error cargando proyectos:",
      },
      services: {
        title: "Soluciones Tecnológicas",
        subtitle: "Cómo puedo ayudarte a escalar tu negocio",
      },
      education: {
        title: "Estudios Académicos",
        degree1: "Tecnicatura universitaria en Sistemas Informáticos",
        institution1: "Universidad Tecnologica Nacional - UTN-FRA",
        degree2: "Diplomatura en Desarrollo Web",
        institution2: "Universidad Tecnológica Nacional",
        career_link: "Ver descripción de la carrera",
      },
      contact: {
        title: "Hablemos de tu Proyecto",
        subtitle: "¿Listo para construir algo extraordinario?",
        name_placeholder: "Tu nombre",
        email_placeholder: "Tu email profesional",
        message_placeholder: "Cuéntame sobre tu proyecto...",
        send: "Enviar mensaje",
        cv_button: "Descargar CV Profesional",
        success: "¡Mensaje recibido! Te contactaré en breve.",
        errors: {
          name_required: "El nombre es requerido",
          email_required: "El email es requerido",
          email_invalid: "Por favor ingresa un email válido",
          message_required: "El mensaje es requerido",
          message_min: "El mensaje debe tener al menos 10 caracteres",
          validation_failed: "Por favor revisa los campos",
        },
      },
      project_card: {
        demo: "Ver Demo",
        code: "Ver Código",
      },
      footer: {
        rights: "Todos los derechos reservados",
        email: "Email",
        linkedin: "LinkedIn",
        github: "GitHub",
      },
    },
  },
  en: {
    translation: {
      nav: {
        home: "Home",
        projects: "Case Studies",
        services: "Solutions",
        skills: "Skills",
        about: "About",
        contact: "Contact",
        education: "Education"
      },
      hero: {
        title: "I Build Scalable Digital Products for Growing Companies",
        subtitle:
          "Senior Fullstack Developer specialized in transforming complex ideas into high-performance software.",
        cta: "Let's Talk",
        cta_primary: "Let's Talk",
        cta_secondary: "View Results",
        badge: "Available for new projects",
      },
      projects: {
        title: "Case Studies",
        subtitle: "Real solutions that generated business impact",
        error: "Error loading projects:",
      },
      services: {
        title: "Tech Solutions",
        subtitle: "How I can help you scale your business",
      },
      education: {
        title: "Academic Studies",
        degree1: "Information Systems Engineering",
        institution1: "National University of Central Buenos Aires",
        degree2: "Web Development Diploma",
        institution2: "National Technological University",
        career_link: "See career description",
      },
      contact: {
        title: "Let's Talk About Your Project",
        subtitle: "Ready to build something extraordinary?",
        name_placeholder: "Your Name",
        email_placeholder: "Your Professional Email",
        message_placeholder: "Tell me about your project...",
        send: "Send Message",
        cv_button: "Download Professional CV",
        success: "Message received! I'll get back to you shortly.",
        errors: {
          name_required: "Name is required",
          email_required: "Email is required",
          email_invalid: "Please enter a valid email",
          message_required: "Message is required",
          message_min: "Message must be at least 10 characters",
          validation_failed: "Please check the fields",
        },
      },
      project_card: {
        demo: "View Demo",
        code: "View Code",
      },
      footer: {
        rights: "All rights reserved",
        email: "Email",
        linkedin: "LinkedIn",
        github: "GitHub",
      },
    },
  },
};

i18n.use(initReactI18next).init({
  resources,
  lng: "es",
  fallbackLng: "es",
  interpolation: { escapeValue: false },
});

export default i18n;
