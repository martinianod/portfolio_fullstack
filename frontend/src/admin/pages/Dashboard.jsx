import { useState, useEffect } from 'react';
import DashboardService from '../services/dashboard.service';
import {
  FiUsers,
  FiUserCheck,
  FiBriefcase,
  FiTrendingUp,
  FiAlertCircle,
} from 'react-icons/fi';

export default function Dashboard() {
  const [kpis, setKpis] = useState(null);
  const [leadsByStage, setLeadsByStage] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    try {
      setLoading(true);
      const [kpisData, stagesData] = await Promise.all([
        DashboardService.getKPIs(),
        DashboardService.getLeadsByStage(),
      ]);
      setKpis(kpisData);
      setLeadsByStage(stagesData);
    } catch (err) {
      setError(err.message || 'Failed to load dashboard data');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg p-6 flex items-start gap-3">
        <FiAlertCircle className="text-red-600 dark:text-red-400 flex-shrink-0 mt-0.5" />
        <div>
          <h3 className="text-red-800 dark:text-red-300 font-semibold mb-1">
            Error Loading Dashboard
          </h3>
          <p className="text-red-600 dark:text-red-400 text-sm">{error}</p>
        </div>
      </div>
    );
  }

  const stats = [
    {
      name: 'Total Leads',
      value: kpis?.totalLeads || 0,
      icon: FiUsers,
      color: 'bg-blue-500',
      change: '+12%',
      changeType: 'positive',
    },
    {
      name: 'Active Clients',
      value: kpis?.activeClients || 0,
      icon: FiUserCheck,
      color: 'bg-green-500',
      change: '+8%',
      changeType: 'positive',
    },
    {
      name: 'Active Projects',
      value: kpis?.activeProjects || 0,
      icon: FiBriefcase,
      color: 'bg-purple-500',
      change: '+5%',
      changeType: 'positive',
    },
    {
      name: 'Conversion Rate',
      value: `${kpis?.conversionRate || 0}%`,
      icon: FiTrendingUp,
      color: 'bg-orange-500',
      change: '+3%',
      changeType: 'positive',
    },
  ];

  const stageColors = {
    new: 'bg-blue-500',
    contacted: 'bg-yellow-500',
    qualified: 'bg-purple-500',
    proposal: 'bg-indigo-500',
    negotiation: 'bg-orange-500',
    won: 'bg-green-500',
    lost: 'bg-red-500',
  };

  return (
    <div className="space-y-6">
      {/* Page header */}
      <div>
        <h1 className="text-2xl font-bold text-slate-900 dark:text-white">
          Dashboard
        </h1>
        <p className="text-slate-600 dark:text-slate-400 mt-1">
          Overview of your CRM metrics and activity
        </p>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        {stats.map((stat) => {
          const Icon = stat.icon;
          return (
            <div
              key={stat.name}
              className="bg-white dark:bg-slate-800 rounded-lg p-6 border border-slate-200 dark:border-slate-700 hover:shadow-lg transition-shadow"
            >
              <div className="flex items-center justify-between mb-4">
                <div className={`${stat.color} p-3 rounded-lg`}>
                  <Icon className="text-white" size={24} />
                </div>
                <span
                  className={`text-sm font-medium ${
                    stat.changeType === 'positive'
                      ? 'text-green-600 dark:text-green-400'
                      : 'text-red-600 dark:text-red-400'
                  }`}
                >
                  {stat.change}
                </span>
              </div>
              <p className="text-sm text-slate-600 dark:text-slate-400 mb-1">
                {stat.name}
              </p>
              <p className="text-3xl font-bold text-slate-900 dark:text-white">
                {stat.value}
              </p>
            </div>
          );
        })}
      </div>

      {/* Leads by Stage */}
      <div className="bg-white dark:bg-slate-800 rounded-lg border border-slate-200 dark:border-slate-700">
        <div className="p-6 border-b border-slate-200 dark:border-slate-700">
          <h2 className="text-lg font-semibold text-slate-900 dark:text-white">
            Leads by Stage
          </h2>
          <p className="text-sm text-slate-600 dark:text-slate-400 mt-1">
            Distribution of leads across pipeline stages
          </p>
        </div>
        <div className="p-6">
          {leadsByStage.length > 0 ? (
            <div className="space-y-4">
              {leadsByStage.map((stage) => (
                <div key={stage.stage}>
                  <div className="flex items-center justify-between mb-2">
                    <div className="flex items-center gap-3">
                      <div
                        className={`w-3 h-3 rounded-full ${
                          stageColors[stage.stage] || 'bg-slate-500'
                        }`}
                      />
                      <span className="font-medium text-slate-900 dark:text-white capitalize">
                        {stage.stage}
                      </span>
                    </div>
                    <span className="text-sm font-semibold text-slate-900 dark:text-white">
                      {stage.count}
                    </span>
                  </div>
                  <div className="w-full bg-slate-200 dark:bg-slate-700 rounded-full h-2">
                    <div
                      className={`${
                        stageColors[stage.stage] || 'bg-slate-500'
                      } h-2 rounded-full transition-all duration-500`}
                      style={{
                        width: `${
                          (stage.count / (kpis?.totalLeads || 1)) * 100
                        }%`,
                      }}
                    />
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="text-center py-12">
              <FiUsers className="mx-auto text-slate-400 mb-3" size={48} />
              <p className="text-slate-600 dark:text-slate-400">
                No lead data available
              </p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
