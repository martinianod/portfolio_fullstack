import PropTypes from "prop-types";

export default function Section({
  id,
  className = "",
  children,
  background = "transparent",
}) {
  const backgrounds = {
    transparent: "",
    mobile: "bg-surface-50 dark:bg-surface-900/50",
    alternate: "bg-slate-50 dark:bg-slate-900/50",
  };

  return (
    <section
      id={id}
      className={`py-16 md:py-24 relative overflow-hidden ${backgrounds[background]} ${className}`}
    >
      {children}
    </section>
  );
}

Section.propTypes = {
  id: PropTypes.string,
  className: PropTypes.string,
  children: PropTypes.node.isRequired,
  background: PropTypes.oneOf(["transparent", "subtle", "alternate"]),
};
