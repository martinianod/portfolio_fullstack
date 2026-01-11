import React from "react";
import { ThemeProvider } from "./contexts/ThemeContext";
import Navbar from "./components/layout/Navbar";
import Footer from "./components/layout/Footer";
import ScrollToTopButton from "./components/ui/ScrollToTopButton";
import Hero from "./features/hero/Hero";
import Projects from "./features/projects/Projects";
import Skills from "./features/services/Services"; // Temorarily aliased until renamed
import Contact from "./features/contact/Contact";
import Education from "./features/education/Education";

import { useEffect } from "react";
import { Analytics } from "./utils/analytics";

export default function App() {
  const [lang, setLang] = React.useState("es");
  
  useEffect(() => {
    Analytics.init();
    Analytics.trackPageView(window.location.pathname);
  }, []);

  const onLangToggle = (lng) => {
    import("./i18n/i18n").then(({ default: i18n }) => {
      i18n.changeLanguage(lng);
    });
    setLang(lng);
  };

  return (
    <ThemeProvider>
      <div className="min-h-screen bg-white dark:bg-slate-950 text-slate-900 dark:text-slate-100 transition-colors">
        <Navbar onLangToggle={onLangToggle} />
        <main>
          <Hero />
          <Projects />
          <Skills />
          <Education />
          <Contact />
        </main>
        <Footer />
        <ScrollToTopButton />
      </div>
    </ThemeProvider>
  );
}
