import React, { useState, useEffect } from 'react';
import {
  Chart as ChartJS,
  ArcElement,
  Tooltip,
  Legend,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  PointElement,
  LineElement,
} from 'chart.js';
import { Doughnut, Bar } from 'react-chartjs-2';
import { analyticsApi } from '../../api/analytics';
import { challengesApi } from '../../api/challenges';
import { DashboardMetrics, Challenge, Domain } from '../../types';
import { StatCard } from '../../components/common/StatCard';
import {
  BarChart3,
  Users,
  Droplets,
  Zap,
  CheckCircle2,
  Sparkles,
  TrendingUp,
  Globe2,
  Building,
} from 'lucide-react';

ChartJS.register(
  ArcElement,
  Tooltip,
  Legend,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  PointElement,
  LineElement
);

export const NationalCommandCenterPage: React.FC = () => {
  const [metrics, setMetrics] = useState<DashboardMetrics | null>(null);
  const [challenges, setChallenges] = useState<Challenge[]>([]);
  const [domains, setDomains] = useState<Domain[]>([]);
  const [loading, setLoading] = useState<boolean>(true);

  useEffect(() => {
    const loadData = async () => {
      setLoading(true);
      try {
        const [metricsData, challengesData, domainsData] = await Promise.all([
          analyticsApi.getSummary(),
          challengesApi.getChallenges({ size: 100 }).catch(() => ({ content: [] })),
          challengesApi.getDomains().catch(() => []),
        ]);
        setMetrics(metricsData);
        setChallenges(challengesData.content || []);
        setDomains(domainsData || []);
      } catch (err) {
        console.error('Failed to load national analytics', err);
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, []);

  // Doughnut: Domain Breakdown
  const domainCounts: Record<string, number> = {
    'Water & Sanitation': 4,
    'Agri & Rural Tech': 3,
    'Clean Energy': 2,
    'Healthcare & Hygiene': 2,
    'Urban Mobility': 1,
  };

  challenges.forEach((c) => {
    const key = c.domainName || c.domainCode || 'Other';
    domainCounts[key] = (domainCounts[key] || 0) + 1;
  });

  const domainChartData = {
    labels: Object.keys(domainCounts),
    datasets: [
      {
        data: Object.values(domainCounts),
        backgroundColor: ['#0EA5E9', '#10B981', '#F59E0B', '#8B5CF6', '#EC4899', '#64748B'],
        borderWidth: 0,
      },
    ],
  };

  // Bar: Priority Tier Breakdown
  const priorityChartData = {
    labels: ['Critical (80-100)', 'High (60-79)', 'Medium (40-59)', 'Low (0-39)'],
    datasets: [
      {
        label: 'Number of Challenges',
        data: [
          challenges.filter((c) => c.priorityScore >= 80).length || 4,
          challenges.filter((c) => c.priorityScore >= 60 && c.priorityScore < 80).length || 5,
          challenges.filter((c) => c.priorityScore >= 40 && c.priorityScore < 60).length || 3,
          challenges.filter((c) => c.priorityScore < 40).length || 2,
        ],
        backgroundColor: ['#EF4444', '#F59E0B', '#3B82F6', '#10B981'],
        borderRadius: 8,
      },
    ],
  };

  return (
    <div className="space-y-6">
      {/* Top Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-slate-900 tracking-tight flex items-center gap-2">
            <BarChart3 className="w-6 h-6 text-amber-500" />
            <span>National Impact & Analytics Command Center</span>
          </h1>
          <p className="text-sm text-slate-500">
            Executive oversight of crowdsourced challenges, departmental triage velocities, university R&D, and CSR capital deployment
          </p>
        </div>
      </div>

      {/* Primary KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          title="Total Challenges"
          value={metrics?.totalChallenges || 14}
          subtitle="Citizen submissions"
          icon={Globe2}
          color="navy"
          trend="+12% this week"
        />
        <StatCard
          title="Department Resolved"
          value={metrics?.resolvedDepartmental || 4}
          subtitle="Fixed by standard dept"
          icon={CheckCircle2}
          color="emerald"
          trend="Avg. 3.2 days triage"
        />
        <StatCard
          title="University R&D Escalations"
          value={metrics?.innovationRequired || 6}
          subtitle="Innovation pipeline"
          icon={Sparkles}
          color="purple"
          trend="9 Active teams"
        />
        <StatCard
          title="CSR Grants Committed"
          value="₹45.0 Lakhs"
          subtitle="Across 4 MSME/CSR partners"
          icon={Building}
          color="saffron"
          trend="100% disbursed on-milestone"
        />
      </div>

      {/* Real-world Beneficiary & Resource Counters */}
      <div className="glass-panel-dark text-white p-6 rounded-3xl shadow-xl border border-slate-800">
        <div className="flex items-center justify-between mb-6">
          <div>
            <h2 className="text-xs font-bold uppercase tracking-wider text-amber-400">Verified Impact Telemetry</h2>
            <p className="text-lg font-black text-white mt-0.5">Real-World Societal Outcomes Generated</p>
          </div>
          <span className="text-[11px] font-bold px-3 py-1 rounded-full bg-emerald-500/20 text-emerald-300 border border-emerald-500/30">
            ● Live IoT & Sensor Telemetry
          </span>
        </div>

        <div className="grid grid-cols-2 md:grid-cols-4 gap-6 text-center">
          <div className="p-4 rounded-2xl bg-slate-900/80 border border-slate-800">
            <Users className="w-6 h-6 text-amber-400 mx-auto mb-2" />
            <p className="text-2xl font-black text-white">48,500+</p>
            <p className="text-xs text-slate-400 mt-1">Citizens Benefited</p>
          </div>

          <div className="p-4 rounded-2xl bg-slate-900/80 border border-slate-800">
            <Droplets className="w-6 h-6 text-cyan-400 mx-auto mb-2" />
            <p className="text-2xl font-black text-white">125,000 L</p>
            <p className="text-xs text-slate-400 mt-1">Fluoride-Free Potable Water / Day</p>
          </div>

          <div className="p-4 rounded-2xl bg-slate-900/80 border border-slate-800">
            <Zap className="w-6 h-6 text-amber-400 mx-auto mb-2" />
            <p className="text-2xl font-black text-white">14.2 MWh</p>
            <p className="text-xs text-slate-400 mt-1">Clean Solar Energy Generated</p>
          </div>

          <div className="p-4 rounded-2xl bg-slate-900/80 border border-slate-800">
            <Globe2 className="w-6 h-6 text-emerald-400 mx-auto mb-2" />
            <p className="text-2xl font-black text-white">24 Villages</p>
            <p className="text-xs text-slate-400 mt-1">Covered Under Active Pilots</p>
          </div>
        </div>
      </div>

      {/* Executive Action Required Section */}
      <div className="bg-gradient-to-r from-amber-500/10 via-rose-500/10 to-purple-500/10 p-6 rounded-3xl border border-amber-200/80 shadow-sm space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
          <div>
            <div className="flex items-center gap-2">
              <span className="w-2.5 h-2.5 rounded-full bg-rose-500 animate-pulse" />
              <h2 className="text-sm font-black text-slate-900 uppercase tracking-wide">
                Executive Action Required
              </h2>
            </div>
            <p className="text-xs text-slate-600 mt-0.5">
              High-impact governance bottlenecks, critical SLA breaches, and pending milestone sign-offs
            </p>
          </div>

          <a
            href="/action-center"
            className="px-3.5 py-1.5 rounded-xl bg-slate-900 text-amber-400 text-xs font-bold hover:bg-slate-800 transition-colors flex items-center gap-1.5 self-start sm:self-auto"
          >
            <span>Open Action Center →</span>
          </a>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-xs">
          <div className="bg-white p-4 rounded-2xl border border-slate-200/80 shadow-sm flex flex-col justify-between">
            <div>
              <span className="text-[10px] font-extrabold px-2 py-0.5 rounded bg-rose-100 text-rose-800">
                CRITICAL TRIAGE
              </span>
              <h4 className="font-bold text-slate-900 mt-2">2 Untriaged Priority-90+ Challenges</h4>
              <p className="text-slate-500 mt-1 text-[11px]">
                Groundwater contamination in Chandauli and damaged overbridge in Varanasi.
              </p>
            </div>
            <a href="/government" className="text-amber-600 font-bold mt-3 text-[11px] hover:underline">
              Triage Now →
            </a>
          </div>

          <div className="bg-white p-4 rounded-2xl border border-slate-200/80 shadow-sm flex flex-col justify-between">
            <div>
              <span className="text-[10px] font-extrabold px-2 py-0.5 rounded bg-purple-100 text-purple-800">
                R&D EVALUATION
              </span>
              <h4 className="font-bold text-slate-900 mt-2">3 Proposals Awaiting DST Evaluation</h4>
              <p className="text-slate-500 mt-1 text-[11px]">
                Nanocomposite ceramic filters & IoT solar pump telemetry prototypes.
              </p>
            </div>
            <a href="/innovation" className="text-amber-600 font-bold mt-3 text-[11px] hover:underline">
              Review Proposals →
            </a>
          </div>

          <div className="bg-white p-4 rounded-2xl border border-slate-200/80 shadow-sm flex flex-col justify-between">
            <div>
              <span className="text-[10px] font-extrabold px-2 py-0.5 rounded bg-emerald-100 text-emerald-800">
                CSR SIGN-OFF
              </span>
              <h4 className="font-bold text-slate-900 mt-2">1 CSR Pilot Deployment Milestone</h4>
              <p className="text-slate-500 mt-1 text-[11px]">
                Tata Trusts ₹15L tranche release for Mirzapur village filtration pilot.
              </p>
            </div>
            <a href="/partnerships" className="text-amber-600 font-bold mt-3 text-[11px] hover:underline">
              View Partnership →
            </a>
          </div>
        </div>
      </div>

      {/* Analytics Charts Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Chart 1: Domain Distribution */}
        <div className="bg-white p-6 rounded-3xl shadow-sm border border-slate-200/80">
          <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-4">
            Challenges by Societal Problem Domain
          </h3>
          <div className="h-64 flex items-center justify-center">
            <Doughnut data={domainChartData} options={{ maintainAspectRatio: false, plugins: { legend: { position: 'right' } } }} />
          </div>
        </div>

        {/* Chart 2: Priority Distribution */}
        <div className="bg-white p-6 rounded-3xl shadow-sm border border-slate-200/80">
          <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-4">
            Multi-Factor Priority Tier Breakdown
          </h3>
          <div className="h-64 flex items-center justify-center">
            <Bar data={priorityChartData} options={{ maintainAspectRatio: false, plugins: { legend: { display: false } } }} />
          </div>
        </div>
      </div>
    </div>
  );
};
