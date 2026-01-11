import { useTranslation } from "react-i18next";
import { motion } from "framer-motion";
import { FiExternalLink, FiGithub } from "react-icons/fi";
import Button from "../../components/ui/Button";

export default function ProjectCard({ project, index }) {
  const { t } = useTranslation();

  // Generate a deterministic gradient based on project ID for consistency
  const gradients = {
    chedoparti: "from-violet-600 to-indigo-600",
    fitlife: "from-emerald-500 to-teal-500",
    "n8n-flows": "from-orange-500 to-red-500",
    default: "from-slate-700 to-slate-900"
  };
  
  const bgGradient = gradients[project.id] || gradients.default;
  // Dynamic import handling or direct path usage would vary, assuming public or imported assets
  // For this setup, we'll try to use the image prop directly if it maps to a real URL or import in the parent
  // But since JSON has paths, we might need a dynamic import map or move images to public
  
  return (
    <motion.article
      initial={{ opacity: 0, y: 20 }}
      whileInView={{ opacity: 1, y: 0 }}
      viewport={{ once: true }}
      transition={{ delay: index * 0.1 }}
      className="flex flex-col bg-white dark:bg-slate-900 rounded-2xl overflow-hidden border border-slate-200 dark:border-slate-800 shadow-lg hover:shadow-xl transition-shadow duration-300 h-full group"
    >
      {/* Visual Header (Image or Gradient) */}
      <div className={`h-48 relative overflow-hidden bg-gradient-to-br ${bgGradient}`}>
        {project.image && (
             <img 
               src={project.image} 
               alt={project.title} 
               className="absolute inset-0 w-full h-full object-cover transition-transform duration-500 group-hover:scale-110"
             />
        )}
        <div className={`absolute inset-0 bg-black/10 group-hover:bg-black/0 transition-colors ${project.image ? 'dark:bg-black/20' : ''}`} />
        <div className="absolute bottom-4 left-4">
          {project.tag && (
            <span className="px-3 py-1 text-xs font-bold uppercase tracking-wider text-white bg-black/30 backdrop-blur-md rounded-full border border-white/20">
              {project.tag}
            </span>
          )}
        </div>
      </div>

      <div className="p-6 flex flex-col flex-grow">
        <h3 className="text-xl font-bold font-heading text-slate-900 dark:text-white mb-2">
          {project.title}
        </h3>
        
        <p className="text-slate-600 dark:text-slate-400 text-sm leading-relaxed mb-6 flex-grow">
          {project.description}
        </p>

        <div className="space-y-6">
          <div className="flex flex-wrap gap-2">
            {project.tech?.map((tech) => (
              <span
                key={tech}
                className="px-2 py-1 text-xs font-medium text-slate-600 dark:text-slate-300 bg-slate-100 dark:bg-slate-800 rounded-md border border-slate-200 dark:border-slate-700"
              >
                {tech}
              </span>
            ))}
          </div>

          <div className="flex gap-3 pt-4 border-t border-slate-100 dark:border-slate-800">
            {project.demo && project.demo !== "#" && (
              <Button 
                href={project.demo} 
                variant="primary" 
                size="sm" 
                target="_blank" 
                rel="noreferrer"
                className="flex-1 gap-2"
              >
                <FiExternalLink /> {t("project_card.demo")}
              </Button>
            )}
            {project.repo && (
               <Button 
               href={project.repo} 
               variant="secondary" 
               size="sm" 
               target="_blank" 
               rel="noreferrer"
               className="flex-1 gap-2"
             >
               <FiGithub /> {t("project_card.code")}
             </Button>
            )}
          </div>
        </div>
      </div>
    </motion.article>
  );
}
