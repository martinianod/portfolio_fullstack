import { useState, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { motion } from "framer-motion";
import { FiSend, FiMail, FiDownload, FiCheckCircle, FiAlertCircle, FiLoader } from "react-icons/fi";
import { FaWhatsapp } from "react-icons/fa";
import Section from "../../components/ui/Section";
import Container from "../../components/ui/Container";
import Button from "../../components/ui/Button";
import { ContactService } from "../../services/api";

export default function Contact() {
  const [formData, setFormData] = useState({
    name: "",
    email: "",
    phone: "",
    company: "",
    budgetRange: "",
    projectType: "",
    message: "",
  });
  const [errors, setErrors] = useState({});
  const [status, setStatus] = useState(null);
  const [touched, setTouched] = useState({});
  const [honeypot, setHoneypot] = useState("");
  const { t, i18n } = useTranslation();

  const budgetOptions = [
    { value: "", label: t("contact.budget.select") || "Select budget range" },
    { value: "< $2k", label: "< $2,000" },
    { value: "$2k-$5k", label: "$2,000 - $5,000" },
    { value: "$5k-$10k", label: "$5,000 - $10,000" },
    { value: "$10k-$20k", label: "$10,000 - $20,000" },
    { value: "$20k+", label: "$20,000+" },
  ];

  const projectTypes = [
    { value: "", label: t("contact.project_type.select") || "Select project type" },
    { value: "Web Application", label: t("contact.project_type.web_app") || "Web Application" },
    { value: "E-Commerce", label: t("contact.project_type.ecommerce") || "E-Commerce" },
    { value: "Landing Page", label: t("contact.project_type.landing") || "Landing Page" },
    { value: "Mobile App", label: t("contact.project_type.mobile") || "Mobile App" },
    { value: "API/Backend", label: t("contact.project_type.api") || "API/Backend" },
    { value: "Consulting", label: t("contact.project_type.consulting") || "Consulting" },
    { value: "Other", label: t("contact.project_type.other") || "Other" },
  ];

  // Keep existing validation logic but cleaner
  const validateField = (name, value) => {
    const newErrors = { ...errors };
    // Validation logic (simplified for brevity but functional)
    if (name === "name" && !value.trim()) newErrors.name = t("contact.errors.name_required");
    else if (name === "name") delete newErrors.name;

    if (name === "email" && !value.trim()) newErrors.email = t("contact.errors.email_required");
    else if (name === "email" && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) newErrors.email = t("contact.errors.email_invalid");
    else if (name === "email") delete newErrors.email;

    if (name === "message" && !value.trim()) newErrors.message = t("contact.errors.message_required");
    else if (name === "message" && value.trim().length < 10) newErrors.message = t("contact.errors.message_min");
    else if (name === "message") delete newErrors.message;

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    if (touched[name]) validateField(name, value);
  };

  const handleBlur = (e) => {
    const { name, value } = e.target;
    setTouched((prev) => ({ ...prev, [name]: true }));
    validateField(name, value);
  };

  const onSubmit = async (e) => {
    e.preventDefault();
    
    // Honeypot check (anti-spam)
    if (honeypot) {
      console.warn("Bot detected");
      return;
    }
    
    setTouched({ name: true, email: true, message: true });
    
    // Quick full validation
    const nameValid = !(!formData.name.trim());
    const emailValid = !(!formData.email.trim() || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email));
    const msgValid = !(!formData.message.trim() || formData.message.trim().length < 10);

    if (nameValid && emailValid && msgValid) {
      setStatus("loading");
      try {
        await ContactService.sendLead({ 
          ...formData, 
          source: "portfolio_web" 
        });
        setStatus("success");
        setFormData({ 
          name: "", 
          email: "", 
          phone: "",
          company: "",
          budgetRange: "",
          projectType: "",
          message: "" 
        });
        setTouched({});
        setErrors({});
        setTimeout(() => setStatus(null), 5000);
      } catch (error) {
        console.error("Failed to send lead:", error);
        setStatus("error");
      }
    } else {
      setStatus("error");
      // Resun validation to show errors
      validateField("name", formData.name);
      validateField("email", formData.email);
      validateField("message", formData.message);
    }
  };

  const openWhatsApp = () => {
    const phone = import.meta.env.VITE_WHATSAPP_NUMBER || "+5491234567890";
    const message = encodeURIComponent(
      `Hola! Me interesa conversar sobre un proyecto.${formData.name ? `\n\nMi nombre: ${formData.name}` : ""}${formData.projectType ? `\nTipo de proyecto: ${formData.projectType}` : ""}${formData.message ? `\n\nMensaje: ${formData.message}` : ""}`
    );
    window.open(`https://wa.me/${phone.replace(/\D/g, "")}?text=${message}`, "_blank");
  };

  const inputClasses = (hasError) => `
    w-full px-4 py-3 bg-white dark:bg-slate-900 border rounded-xl outline-none transition-all duration-200
    ${hasError 
      ? "border-red-500 focus:border-red-500 focus:ring-2 focus:ring-red-500/20" 
      : "border-slate-200 dark:border-slate-800 focus:border-primary-500 focus:ring-2 focus:ring-primary-500/20"
    }
    text-slate-900 dark:text-white placeholder-slate-400
  `;

  return (
    <Section id="contact" background="subtle">
      <Container>
        <div className="grid lg:grid-cols-2 gap-12 lg:gap-24 items-start">
          {/* Left Column: Info & Copy */}
          <div>
            <motion.h2
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true }}
              className="text-3xl md:text-4xl font-bold font-heading text-slate-900 dark:text-slate-100 mb-6"
            >
              {t("contact.title")}
            </motion.h2>
            <motion.p
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true }}
              transition={{ delay: 0.1 }}
              className="text-lg text-slate-600 dark:text-slate-400 mb-8"
            >
              {t("contact.subtitle")}
            </motion.p>
            
            <div className="space-y-6">
              <a href="mailto:contacto@martiniano.dev" className="flex items-center gap-4 p-4 rounded-xl bg-white dark:bg-slate-800 border border-slate-100 dark:border-slate-700 shadow-sm hover:shadow-md transition-shadow group">
                <div className="w-12 h-12 rounded-lg bg-primary-50 dark:bg-primary-900/30 flex items-center justify-center text-primary-600 dark:text-primary-400 group-hover:scale-110 transition-transform">
                  <FiMail className="w-6 h-6" />
                </div>
                <div>
                  <p className="text-sm text-slate-500 dark:text-slate-400">Email</p>
                  <p className="font-semibold text-slate-900 dark:text-white">contacto@martiniano.dev</p>
                </div>
              </a>

               <div className="flex items-center gap-4 p-4 rounded-xl bg-white dark:bg-slate-800 border border-slate-100 dark:border-slate-700 shadow-sm">
                <Button 
                    href="/CV-MartinianoDAmbrosio.pdf"
                    target="_blank"
                    download
                    variant="secondary"
                    className="w-full justify-between group"
                  >
                    {t("contact.cv_button")}
                    <FiDownload className="group-hover:translate-y-1 transition-transform" />
                  </Button>
               </div>
            </div>
          </div>

          {/* Right Column: Form */}
          <motion.div
            initial={{ opacity: 0, x: 20 }}
            whileInView={{ opacity: 1, x: 0 }}
            viewport={{ once: true }}
            transition={{ delay: 0.2 }}
            className="bg-white dark:bg-slate-800 p-8 rounded-2xl shadow-xl border border-slate-100 dark:border-slate-700"
          >
            <form onSubmit={onSubmit} className="space-y-5">
              {/* Honeypot field - hidden from users */}
              <input
                type="text"
                name="website"
                value={honeypot}
                onChange={(e) => setHoneypot(e.target.value)}
                style={{ position: "absolute", left: "-9999px" }}
                tabIndex="-1"
                autoComplete="off"
              />
              
              <div className="grid md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">
                    {t("contact.name_placeholder") || "Name"} *
                  </label>
                  <input
                    type="text"
                    name="name"
                    value={formData.name}
                    onChange={handleChange}
                    onBlur={handleBlur}
                    className={inputClasses(touched.name && errors.name)}
                    placeholder="John Doe"
                  />
                  {touched.name && errors.name && (
                    <p className="mt-1 text-sm text-red-500 flex items-center gap-1">
                      <FiAlertCircle /> {errors.name}
                    </p>
                  )}
                </div>

                <div>
                  <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">
                    {t("contact.email_placeholder") || "Email"} *
                  </label>
                  <input
                    type="email"
                    name="email"
                    value={formData.email}
                    onChange={handleChange}
                    onBlur={handleBlur}
                    className={inputClasses(touched.email && errors.email)}
                    placeholder="john@company.com"
                  />
                  {touched.email && errors.email && (
                    <p className="mt-1 text-sm text-red-500 flex items-center gap-1">
                      <FiAlertCircle /> {errors.email}
                    </p>
                  )}
                </div>
              </div>

              <div className="grid md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">
                    {t("contact.phone_placeholder") || "Phone / WhatsApp"}
                  </label>
                  <input
                    type="tel"
                    name="phone"
                    value={formData.phone}
                    onChange={handleChange}
                    className={inputClasses(false)}
                    placeholder="+1 234 567 8900"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">
                    {t("contact.company_placeholder") || "Company"}
                  </label>
                  <input
                    type="text"
                    name="company"
                    value={formData.company}
                    onChange={handleChange}
                    className={inputClasses(false)}
                    placeholder="Your Company Inc."
                  />
                </div>
              </div>

              <div className="grid md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">
                    {t("contact.budget_label") || "Budget Range"}
                  </label>
                  <select
                    name="budgetRange"
                    value={formData.budgetRange}
                    onChange={handleChange}
                    className={inputClasses(false)}
                  >
                    {budgetOptions.map((opt) => (
                      <option key={opt.value} value={opt.value}>
                        {opt.label}
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">
                    {t("contact.project_type_label") || "Project Type"}
                  </label>
                  <select
                    name="projectType"
                    value={formData.projectType}
                    onChange={handleChange}
                    className={inputClasses(false)}
                  >
                    {projectTypes.map((opt) => (
                      <option key={opt.value} value={opt.value}>
                        {opt.label}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">
                   {t("contact.message_placeholder") || "Message"} *
                </label>
                <textarea
                  name="message"
                  value={formData.message}
                  onChange={handleChange}
                  onBlur={handleBlur}
                  rows={4}
                  className={inputClasses(touched.message && errors.message)}
                  placeholder="Tell me about your project..."
                />
                {touched.message && errors.message && (
                  <p className="mt-1 text-sm text-red-500 flex items-center gap-1">
                    <FiAlertCircle /> {errors.message}
                  </p>
                )}
              </div>

              <div className="flex flex-col sm:flex-row gap-3">
                <Button 
                  type="submit" 
                  variant="primary" 
                  className="flex-1 gap-2 justify-center" 
                  disabled={status === "success" || status === "loading"}
                >
                  {status === "loading" && <FiLoader className="animate-spin" />}
                  {status === "success" && <FiCheckCircle />}
                  {status !== "loading" && status !== "success" && <FiSend />}
                  {status === "success" 
                    ? t("contact.success") || "Sent!" 
                    : status === "loading" 
                    ? "Sending..." 
                    : t("contact.send") || "Send Message"}
                </Button>
                
                <Button
                  type="button"
                  onClick={openWhatsApp}
                  variant="secondary"
                  className="flex-1 gap-2 justify-center bg-green-600 hover:bg-green-700 text-white border-green-600"
                >
                  <FaWhatsapp className="text-xl" />
                  {t("contact.whatsapp") || "WhatsApp"}
                </Button>
              </div>
              
              {status === "error" && (
                <p className="text-center text-sm text-red-500 mt-2">
                  {t("contact.errors.validation_failed") || "Please check the form and try again."}
                </p>
              )}

              {status === "success" && (
                <p className="text-center text-sm text-green-600 dark:text-green-400 mt-2 flex items-center justify-center gap-2">
                  <FiCheckCircle /> {t("contact.success_message") || "Thank you! I'll get back to you soon."}
                </p>
              )}
            </form>
          </motion.div>
        </div>
      </Container>
    </Section>
  );
}
