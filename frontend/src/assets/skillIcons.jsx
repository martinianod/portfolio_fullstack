// Este archivo contiene los iconos SVG para cada tecnología
// Puedes expandirlo con más iconos según lo necesites

import {
  DiJava,
  DiReact,
  DiDocker,
  DiAws,
  DiPostgresql,
  DiRedis,
  DiNginx,
  DiJenkins,
} from "react-icons/di";
import { SiSpring, SiTypescript, SiKubernetes } from "react-icons/si";
import { MdApi } from "react-icons/md";
// ...existing code...
export const skillIcons = {
  Java: <DiJava className="inline-block mr-2" size={24} />,
  "Spring Boot": <SiSpring className="inline-block mr-2" size={24} />,
  React: <DiReact className="inline-block mr-2" size={24} />,
  TypeScript: <SiTypescript className="inline-block mr-2" size={24} />,
  PostgreSQL: <DiPostgresql className="inline-block mr-2" size={24} />,
  Docker: <DiDocker className="inline-block mr-2" size={24} />,
  Kubernetes: <SiKubernetes className="inline-block mr-2" size={24} />,
  Jenkins: <DiJenkins className="inline-block mr-2" size={24} />,
  AWS: <DiAws className="inline-block mr-2" size={24} />,
  Redis: <DiRedis className="inline-block mr-2" size={24} />,
  Nginx: <DiNginx className="inline-block mr-2" size={24} />,
  gRPC: <MdApi className="inline-block mr-2" size={24} />,
  REST: (
    <span
      className="inline-block mr-2"
      style={{ fontWeight: 600, fontSize: 20 }}
    >
      REST
    </span>
  ),
};
// ...eliminado código antiguo, solo Devicon export...
