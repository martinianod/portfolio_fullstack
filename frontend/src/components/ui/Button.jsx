import { motion } from "framer-motion";
import PropTypes from "prop-types";

export default function Button({
  children,
  variant = "primary",
  size = "md",
  className = "",
  onClick,
  href,
  target,
  rel,
  type = "button",
  ...props
}) {
  const baseStyles =
    "inline-flex items-center justify-center font-semibold rounded-xl transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed";

  const variants = {
    primary:
      "bg-primary-600 hover:bg-primary-500 text-white shadow-lg hover:shadow-primary-500/30 focus:ring-primary-500",
    secondary:
      "bg-white dark:bg-slate-800 text-slate-900 dark:text-white border border-slate-200 dark:border-slate-700 hover:bg-slate-50 dark:hover:bg-slate-700 shadow-sm focus:ring-slate-500",
    accent:
      "bg-accent-500 hover:bg-accent-400 text-white shadow-lg hover:shadow-accent-500/30 focus:ring-accent-500",
    gradient:
      "bg-gradient-to-r from-primary-600 to-accent-500 hover:from-primary-500 hover:to-accent-400 text-white shadow-lg hover:shadow-primary-500/25 focus:ring-primary-500",
    ghost:
      "text-slate-600 dark:text-slate-400 hover:text-primary-600 dark:hover:text-primary-400 hover:bg-slate-100 dark:hover:bg-slate-800 focus:ring-slate-500",
  };

  const sizes = {
    sm: "px-3 py-1.5 text-sm",
    md: "px-6 py-2.5 text-base",
    lg: "px-8 py-3.5 text-lg",
    icon: "p-2",
  };

  const Component = href ? motion.a : motion.button;

  return (
    <Component
      whileHover={{ scale: 1.02 }}
      whileTap={{ scale: 0.98 }}
      className={`${baseStyles} ${variants[variant]} ${sizes[size]} ${className}`}
      onClick={onClick}
      href={href}
      target={target}
      rel={rel}
      type={!href ? type : undefined}
      {...props}
    >
      {children}
    </Component>
  );
}

Button.propTypes = {
  children: PropTypes.node.isRequired,
  variant: PropTypes.oneOf([
    "primary",
    "secondary",
    "accent",
    "gradient",
    "ghost",
  ]),
  size: PropTypes.oneOf(["sm", "md", "lg", "icon"]),
  className: PropTypes.string,
  onClick: PropTypes.func,
  href: PropTypes.string,
  target: PropTypes.string,
  rel: PropTypes.string,
  type: PropTypes.oneOf(["button", "submit", "reset"]),
};
