export const Analytics = {
  init: () => {
    if (import.meta.env.PROD) {
      // Initialize GA4 or Vercel Analytics here
      // Example: ReactGA.initialize('G-XXXXXXXXXX');
      console.log("Analytics initialized");
    }
  },

  trackPageView: (path) => {
    if (import.meta.env.PROD) {
      // ReactGA.send({ hitType: "pageview", page: path });
    } else {
      console.log(`[Analytics] Page View: ${path}`);
    }
  },

  trackEvent: (category, action, label) => {
    if (import.meta.env.PROD) {
      // ReactGA.event({ category, action, label });
    } else {
      console.log(`[Analytics] Event: ${category} - ${action} ${label ? `(${label})` : ""}`);
    }
  },
};
