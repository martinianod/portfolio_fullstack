// Este archivo contiene los iconos SVG para cada tecnología
// Puedes expandirlo con más iconos según lo necesites

export const skillIcons = {
  Java: (
    <svg
      viewBox="0 0 32 32"
      width="24"
      height="24"
      fill="currentColor"
      className="inline-block mr-2 text-orange-600"
    >
      <path d="M16 30c-6.627 0-12-5.373-12-12S9.373 6 16 6s12 5.373 12 12-5.373 12-12 12zm0-28C7.163 2 0 9.163 0 18s7.163 16 16 16 16-7.163 16-16S24.837 2 16 2zm2.5 22.5c-1.5.5-3.5.5-5 0-.5-.2-.7-.7-.5-1.2.2-.5.7-.7 1.2-.5 1.2.4 2.8.4 4 0 .5-.2 1 .1 1.2.5.2.5-.1 1-.5 1.2zm-2.5-3c-2.2 0-4-1.8-4-4 0-.6.4-1 1-1s1 .4 1 1c0 1.1.9 2 2 2s2-.9 2-2c0-.6.4-1 1-1s1 .4 1 1c0 2.2-1.8 4-4 4z" />
    </svg>
  ),
  "Spring Boot": (
    <svg
      viewBox="0 0 32 32"
      width="24"
      height="24"
      fill="currentColor"
      className="inline-block mr-2 text-green-600"
    >
      <circle
        cx="16"
        cy="16"
        r="14"
        stroke="currentColor"
        strokeWidth="2"
        fill="none"
      />
      <path
        d="M16 10v12M10 16h12"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="round"
      />
    </svg>
  ),
  React: (
    <svg
      viewBox="0 0 32 32"
      width="24"
      height="24"
      fill="currentColor"
      className="inline-block mr-2 text-sky-500"
    >
      <circle cx="16" cy="16" r="2.5" fill="currentColor" />
      <ellipse
        cx="16"
        cy="16"
        rx="11"
        ry="4.5"
        stroke="currentColor"
        strokeWidth="2"
        fill="none"
      />
      <ellipse
        cx="16"
        cy="16"
        rx="4.5"
        ry="11"
        stroke="currentColor"
        strokeWidth="2"
        fill="none"
        transform="rotate(60 16 16)"
      />
      <ellipse
        cx="16"
        cy="16"
        rx="4.5"
        ry="11"
        stroke="currentColor"
        strokeWidth="2"
        fill="none"
        transform="rotate(120 16 16)"
      />
    </svg>
  ),
  TypeScript: (
    <svg
      viewBox="0 0 32 32"
      width="24"
      height="24"
      fill="currentColor"
      className="inline-block mr-2 text-blue-600"
    >
      <rect x="4" y="4" width="24" height="24" rx="4" fill="#3178c6" />
      <text x="16" y="22" textAnchor="middle" fontSize="12" fill="#fff">
        TS
      </text>
    </svg>
  ),
  PostgreSQL: (
    <svg
      viewBox="0 0 32 32"
      width="24"
      height="24"
      fill="currentColor"
      className="inline-block mr-2 text-blue-800"
    >
      <ellipse cx="16" cy="16" rx="12" ry="8" fill="#336791" />
      <text x="16" y="20" textAnchor="middle" fontSize="10" fill="#fff">
        PG
      </text>
    </svg>
  ),
  Docker: (
    <svg
      viewBox="0 0 32 32"
      width="24"
      height="24"
      fill="currentColor"
      className="inline-block mr-2 text-blue-400"
    >
      <rect x="8" y="16" width="16" height="6" rx="2" fill="#2496ed" />
      <rect x="12" y="12" width="8" height="4" rx="1" fill="#2496ed" />
    </svg>
  ),
  Kubernetes: (
    <svg
      viewBox="0 0 32 32"
      width="24"
      height="24"
      fill="currentColor"
      className="inline-block mr-2 text-blue-500"
    >
      <circle cx="16" cy="16" r="12" fill="#326ce5" />
      <text x="16" y="21" textAnchor="middle" fontSize="10" fill="#fff">
        K8s
      </text>
    </svg>
  ),
  Jenkins: (
    <svg
      viewBox="0 0 32 32"
      width="24"
      height="24"
      fill="currentColor"
      className="inline-block mr-2 text-red-500"
    >
      <circle cx="16" cy="16" r="12" fill="#d24939" />
      <text x="16" y="21" textAnchor="middle" fontSize="10" fill="#fff">
        J
      </text>
    </svg>
  ),
  AWS: (
    <svg
      viewBox="0 0 32 32"
      width="24"
      height="24"
      fill="currentColor"
      className="inline-block mr-2 text-orange-500"
    >
      <rect x="8" y="12" width="16" height="8" rx="2" fill="#ff9900" />
      <text x="16" y="20" textAnchor="middle" fontSize="10" fill="#fff">
        AWS
      </text>
    </svg>
  ),
  Redis: (
    <svg
      viewBox="0 0 32 32"
      width="24"
      height="24"
      fill="currentColor"
      className="inline-block mr-2 text-red-600"
    >
      <ellipse cx="16" cy="16" rx="12" ry="7" fill="#d82c20" />
      <text x="16" y="20" textAnchor="middle" fontSize="10" fill="#fff">
        Redis
      </text>
    </svg>
  ),
  Nginx: (
    <svg
      viewBox="0 0 32 32"
      width="24"
      height="24"
      fill="currentColor"
      className="inline-block mr-2 text-green-600"
    >
      <rect x="8" y="12" width="16" height="8" rx="2" fill="#009639" />
      <text x="16" y="20" textAnchor="middle" fontSize="10" fill="#fff">
        Nginx
      </text>
    </svg>
  ),
  gRPC: (
    <svg
      viewBox="0 0 32 32"
      width="24"
      height="24"
      fill="currentColor"
      className="inline-block mr-2 text-teal-500"
    >
      <rect x="8" y="12" width="16" height="8" rx="2" fill="#00c5a9" />
      <text x="16" y="20" textAnchor="middle" fontSize="10" fill="#fff">
        gRPC
      </text>
    </svg>
  ),
  REST: (
    <svg
      viewBox="0 0 32 32"
      width="24"
      height="24"
      fill="currentColor"
      className="inline-block mr-2 text-gray-700"
    >
      <rect x="8" y="12" width="16" height="8" rx="2" fill="#6b7280" />
      <text x="16" y="20" textAnchor="middle" fontSize="10" fill="#fff">
        REST
      </text>
    </svg>
  ),
};
