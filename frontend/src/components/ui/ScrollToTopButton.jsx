import React, { useEffect, useState } from "react";

export default function ScrollToTopButton() {
  const [showScroll, setShowScroll] = useState(false);

  useEffect(() => {
    const handleScroll = () => {
      setShowScroll(window.scrollY > 200);
    };
    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, []);

  const scrollToTop = () => {
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  if (!showScroll) return null;

  return (
    <div className="fixed left-1/2 bottom-8 transform -translate-x-1/2 z-50 pointer-events-none">
      <button
        onClick={scrollToTop}
        className="pointer-events-auto bg-gradient-to-r from-violet-600 via-fuchsia-500 to-pink-500 text-white rounded-full shadow-2xl p-6 flex items-center justify-center hover:scale-110 transition-all duration-200 border-4 border-white dark:border-slate-950 bg-opacity-70 backdrop-blur-md"
        aria-label="Scroll to top"
        style={{ opacity: 0.7 }}
      >
        <svg
          width="32"
          height="32"
          viewBox="0 0 32 32"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          <circle
            cx="16"
            cy="16"
            r="15"
            stroke="white"
            strokeWidth="2"
            fill="none"
          />
          <path
            d="M16 22V10M16 10L10 16M16 10L22 16"
            stroke="white"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
          />
        </svg>
      </button>
    </div>
  );
}
