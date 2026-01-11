import PropTypes from "prop-types";

export default function Container({ className = "", children }) {
  return (
    <div className={`container px-4 md:px-6 mx-auto ${className}`}>
      {children}
    </div>
  );
}

Container.propTypes = {
  className: PropTypes.string,
  children: PropTypes.node.isRequired,
};
