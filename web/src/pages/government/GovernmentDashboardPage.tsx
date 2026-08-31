import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { challengesApi } from '../../api/challenges';
import { analyticsApi } from '../../api/analytics';
import { Challenge, Domain, DashboardMetrics } from '../../types';
import { StatusBadge } from '../../components/common/StatusBadge';
import { PriorityMeter } from '../../components/common/PriorityMeter';
import { StatCard } from '../../components/common/StatCard';
import {
  Inbox,
  AlertTriangle,
  CheckCircle2,
  Sparkles,
  Search,
  Filter,
  Eye,
  ArrowUpRight,
  RefreshCw,
  Building2,
} from 'lucide-react';

export const GovernmentDashboardPage: React.FC = () => {
  const [challenges, setChallenges] = useState<Challenge[]>([]);
  const [domains, setDomains] = useState<Domain[]>([]);
  const [metrics, setMetrics] = useState<DashboardMetrics | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [search, setSearch] = useState<string>('');
  const [selectedDomain, setSelectedDomain] = useState<string>('');
  const [selectedStatus, setSelectedStatus] = useState<string>('');
  const [selectedSeverity, setSelectedSeverity] = useState<string>('');

  const loadData = async () => {
    setLoading(true);
    try {
      const [challengesData, domainsData, metricsData] = await Promise.all([
        challengesApi.getChallenges({
          search: search || undefined,
          domain: selectedDomain || undefined,
          status: selectedStatus || undefined,
          severity: selectedSeverity || undefined,
          size: 50,
        }),
        challengesApi.getDomains(),
        analyticsApi.getSummary(),
      ]);

      setChallenges(challengesData.content || []);
      setDomains(domainsData || []);
      setMetrics(metricsData);
    } catch (err) {
      console.error('Failed to load dashboard data', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [selectedDomain, selectedStatus, selectedSeverity]);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    loadData();
  };

  return (
    <div className="space-y-6">
      {/* Top Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-slate-900 tracking-tight">Government Command Center</h1>
          <p className="text-sm text-slate-500">
            Real-time citizen challenge crowdsourcing, AI triage, and inter-departmental resolution engine
          </p>
        </div>
        <div className="flex items-center gap-3">
          <button
            onClick={loadData}
            className="px-3.5 py-2 rounded-xl bg-white border border-slate-200 text-slate-700 text-xs font-bold hover:bg-slate-50 transition-colors flex items-center gap-2 shadow-sm"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
            <span>Refresh</span>
          </button>
          <Link
            to="/map"
            className="px-4 py-2 rounded-xl bg-slate-900 text-amber-400 text-xs font-bold hover:bg-slate-800 transition-colors flex items-center gap-2 shadow-sm"
          >
            <span>Live GIS Map</span>
            <ArrowUpRight className="w-3.5 h-3.5" />
          </Link>
        </div>
      </div>

      {/* Metric Cards Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          title="Total Challenges"
          value={metrics?.totalChallenges ?? challenges.length}
          subtitle="Citizen crowdsourced reports"
          icon={Inbox}
          color="navy"
        />
        <StatCard
          title="Pending Triage"
          value={metrics?.pendingTriage ?? challenges.filter((c) => c.status === 'SUBMITTED' || c.status === 'UNDER_DEPARTMENT_TRIAGE').length}
          subtitle="Awaiting official review"
          icon={AlertTriangle}
          color="saffron"
        />
        <StatCard
          title="Department Resolved"
          value={metrics?.resolvedDepartmental ?? challenges.filter((c) => c.status === 'RESOLVED_DEPARTMENTAL').length}
          subtitle="Fixed by standard departments"
          icon={CheckCircle2}
          color="emerald"
        />
        <StatCard
          title="Innovation Required"
          value={metrics?.innovationRequired ?? challenges.filter((c) => c.status === 'INNOVATION_REQUIRED' || c.status === 'INNOVATION_CHALLENGE_ACTIVE').length}
          subtitle="Escalated to Universities"
          icon={Sparkles}
          color="purple"
        />
      </div>

      {/* Filter and Search Bar */}
      <div className="glass-panel p-4 rounded-2xl shadow-sm border border-slate-200/80 flex flex-col md:flex-row gap-3">
        <form onSubmit={handleSearchSubmit} className="flex-1 relative">
          <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-3" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search by Tracking ID (SMX-2026-...), Title, or Location..."
            className="w-full pl-10 pr-4 py-2 rounded-xl bg-white border border-slate-200 text-sm placeholder-slate-400 focus:outline-none focus:border-amber-500 focus:ring-1 focus:ring-amber-500"
          />
        </form>

        <div className="flex flex-wrap items-center gap-2">
          <select
            value={selectedDomain}
            onChange={(e) => setSelectedDomain(e.target.value)}
            className="px-3 py-2 rounded-xl bg-white border border-slate-200 text-xs font-semibold text-slate-700 focus:outline-none focus:border-amber-500"
          >
            <option value="">All Domains</option>
            {domains.map((d) => (
              <option key={d.code} value={d.code}>
                {d.name}
              </option>
            ))}
          </select>

          <select
            value={selectedSeverity}
            onChange={(e) => setSelectedSeverity(e.target.value)}
            className="px-3 py-2 rounded-xl bg-white border border-slate-200 text-xs font-semibold text-slate-700 focus:outline-none focus:border-amber-500"
          >
            <option value="">All Severities</option>
            <option value="CRITICAL">Critical</option>
            <option value="HIGH">High</option>
            <option value="MEDIUM">Medium</option>
            <option value="LOW">Low</option>
          </select>

          <select
            value={selectedStatus}
            onChange={(e) => setSelectedStatus(e.target.value)}
            className="px-3 py-2 rounded-xl bg-white border border-slate-200 text-xs font-semibold text-slate-700 focus:outline-none focus:border-amber-500"
          >
            <option value="">All Statuses</option>
            <option value="SUBMITTED">Submitted</option>
            <option value="ASSIGNED_TO_DEPARTMENT">Assigned to Dept</option>
            <option value="UNDER_DEPARTMENT_TRIAGE">Under Triage</option>
            <option value="IN_PROGRESS_DEPARTMENTAL">Dept In Progress</option>
            <option value="RESOLVED_DEPARTMENTAL">Resolved (Standard)</option>
            <option value="INNOVATION_REQUIRED">Innovation Escalated</option>
          </select>
        </div>
      </div>

      {/* Challenges Table */}
      <div className="bg-white rounded-2xl shadow-sm border border-slate-200/80 overflow-hidden">
        <div className="p-4 border-b border-slate-100 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Filter className="w-4 h-4 text-amber-500" />
            <span className="text-xs font-bold uppercase tracking-wider text-slate-700">
              Active Challenge Queue ({challenges.length})
            </span>
          </div>
          <span className="text-xs text-slate-500">Sorted by Multi-Factor Priority</span>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm text-slate-700">
            <thead className="bg-slate-50 text-[11px] font-bold uppercase tracking-wider text-slate-500 border-b border-slate-200/80">
              <tr>
                <th className="px-5 py-3">Tracking ID & Title</th>
                <th className="px-4 py-3">Domain</th>
                <th className="px-4 py-3 w-48">AI Priority Score</th>
                <th className="px-4 py-3">AI Flags</th>
                <th className="px-4 py-3">Assigned Dept</th>
                <th className="px-4 py-3">Status</th>
                <th className="px-4 py-3 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 font-medium">
              {challenges.length === 0 ? (
                <tr>
                  <td colSpan={7} className="px-5 py-12 text-center text-slate-400">
                    {loading ? 'Loading challenge registry...' : 'No challenges match current filter criteria.'}
                  </td>
                </tr>
              ) : (
                challenges.map((c) => (
                  <tr key={c.id} className="hover:bg-slate-50/80 transition-colors">
                    <td className="px-5 py-3.5">
                      <div className="flex flex-col">
                        <span className="font-mono text-xs font-bold text-amber-600">
                          {c.trackingNumber}
                        </span>
                        <span className="text-sm font-bold text-slate-900 line-clamp-1 mt-0.5">
                          {c.title}
                        </span>
                        <span className="text-[11px] text-slate-500 truncate max-w-xs mt-0.5">
                          📍 {c.addressText || `${c.district || 'District'}, ${c.state || 'India'}`}
                        </span>
                      </div>
                    </td>

                    <td className="px-4 py-3.5">
                      <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-semibold bg-slate-100 text-slate-800">
                        {c.domainName || c.domainCode}
                      </span>
                    </td>

                    <td className="px-4 py-3.5">
                      <PriorityMeter score={c.priorityScore || 50} size="sm" />
                    </td>

                    <td className="px-4 py-3.5">
                      <div className="flex flex-col gap-1">
                        {c.aiCategoryConfidence && (
                          <span className="text-[11px] text-slate-600">
                            Confidence: <strong>{(c.aiCategoryConfidence * 100).toFixed(0)}%</strong>
                          </span>
                        )}
                        {c.isDuplicateFlagged && (
                          <span className="inline-flex items-center gap-1 text-[10px] font-bold text-amber-700 bg-amber-50 px-1.5 py-0.5 rounded border border-amber-200">
                            Duplicate Flagged
                          </span>
                        )}
                      </div>
                    </td>

                    <td className="px-4 py-3.5">
                      <div className="flex items-center gap-1.5 text-xs text-slate-700">
                        <Building2 className="w-3.5 h-3.5 text-slate-400" />
                        <span>{c.departmentName || c.departmentCode || 'Pending Routing'}</span>
                      </div>
                    </td>

                    <td className="px-4 py-3.5">
                      <StatusBadge status={c.status} />
                    </td>

                    <td className="px-4 py-3.5 text-right">
                      <Link
                        to={`/government/challenges/${c.id}`}
                        className="inline-flex items-center gap-1 px-3 py-1.5 rounded-lg bg-slate-900 text-white text-xs font-bold hover:bg-amber-500 hover:text-slate-950 transition-colors shadow-sm"
                      >
                        <Eye className="w-3.5 h-3.5" />
                        <span>Triage</span>
                      </Link>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
