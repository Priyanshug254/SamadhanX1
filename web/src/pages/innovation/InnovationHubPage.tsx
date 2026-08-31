import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { innovationApi } from '../../api/innovation';
import { challengesApi } from '../../api/challenges';
import { Proposal, Challenge, ProjectTeam } from '../../types';
import { StatusBadge } from '../../components/common/StatusBadge';
import {
  Lightbulb,
  Users,
  Layers,
  Sparkles,
  Plus,
  ArrowRight,
  TrendingUp,
  FileCheck,
} from 'lucide-react';

export const InnovationHubPage: React.FC = () => {
  const [proposals, setProposals] = useState<Proposal[]>([]);
  const [innovationChallenges, setInnovationChallenges] = useState<Challenge[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [activeTab, setActiveTab] = useState<'proposals' | 'challenges'>('proposals');

  // New Proposal Form Modal State
  const [showModal, setShowModal] = useState<boolean>(false);
  const [selectedChallengeId, setSelectedChallengeId] = useState<string>('');
  const [teamName, setTeamName] = useState<string>('');
  const [proposalTitle, setProposalTitle] = useState<string>('');
  const [summary, setSummary] = useState<string>('');
  const [approach, setApproach] = useState<string>('');
  const [trlLevel, setTrlLevel] = useState<number>(3);
  const [budget, setBudget] = useState<number>(500000);
  const [weeks, setWeeks] = useState<number>(12);
  const [submitting, setSubmitting] = useState<boolean>(false);

  const loadData = async () => {
    setLoading(true);
    try {
      const [proposalsData, challengesData] = await Promise.all([
        innovationApi.getProposals({ size: 50 }).catch(() => ({ content: [] })),
        challengesApi.getChallenges({ size: 50 }),
      ]);
      const activeChallenges = (challengesData.content || []).filter(
        (c) => ['IN_COLLABORATION', 'INNOVATION_REQUIRED', 'INNOVATION_CHALLENGE_ACTIVE', 'UNDER_REVIEW', 'SOLUTION_PROPOSED', 'PROTOTYPE'].includes(c.status)
      );
      setProposals(proposalsData.content || []);
      setInnovationChallenges(activeChallenges.length > 0 ? activeChallenges : challengesData.content || []);
    } catch (err) {
      console.error('Failed to load innovation hub data', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleCreateProposal = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedChallengeId || !teamName || !proposalTitle) return;
    setSubmitting(true);
    try {
      // 1. Create team
      let teamId: string | undefined;
      try {
        const team = await innovationApi.createTeam(teamName, selectedChallengeId);
        teamId = team.id;
      } catch (e) {
        console.warn('Team creation fallback:', e);
      }

      // 2. Create proposal
      await innovationApi.createProposal({
        challengeId: selectedChallengeId,
        teamId: teamId || '',
        title: proposalTitle,
        executiveSummary: summary,
        technicalApproach: approach,
        trlLevel: Number(trlLevel),
        budgetRequired: Number(budget),
        timelineWeeks: Number(weeks),
      });

      setShowModal(false);
      await loadData();
    } catch (err: any) {
      console.error('Proposal submission error:', err);
      alert(`Submission Notice: ${err?.message || 'Failed to submit innovation proposal'}`);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-slate-900 tracking-tight flex items-center gap-2">
            <Lightbulb className="w-6 h-6 text-amber-500" />
            <span>University & Research Innovation Hub</span>
          </h1>
          <p className="text-sm text-slate-500">
            Multidisciplinary faculty-student team formation, solution development, and TRL technology maturation
          </p>
        </div>

        <button
          onClick={() => setShowModal(true)}
          className="px-4 py-2.5 rounded-xl bg-gradient-to-r from-amber-500 to-amber-600 text-slate-950 text-xs font-bold hover:from-amber-400 hover:to-amber-500 transition-all flex items-center gap-2 shadow-md shadow-amber-500/20"
        >
          <Plus className="w-4 h-4" />
          <span>Submit Solution Proposal</span>
        </button>
      </div>

      {/* Navigation Tabs */}
      <div className="flex border-b border-slate-200 gap-6 text-sm font-bold text-slate-500">
        <button
          onClick={() => setActiveTab('proposals')}
          className={`pb-3 transition-colors ${
            activeTab === 'proposals' ? 'border-b-2 border-amber-500 text-slate-900' : 'hover:text-slate-900'
          }`}
        >
          Solution Proposals ({proposals.length})
        </button>
        <button
          onClick={() => setActiveTab('challenges')}
          className={`pb-3 transition-colors ${
            activeTab === 'challenges' ? 'border-b-2 border-amber-500 text-slate-900' : 'hover:text-slate-900'
          }`}
        >
          Active Innovation Challenges ({innovationChallenges.length})
        </button>
      </div>

      {/* Tab 1: Proposals List */}
      {activeTab === 'proposals' && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {proposals.length === 0 ? (
            <div className="col-span-full bg-white p-12 text-center rounded-2xl border border-slate-200/80 text-slate-400">
              {loading ? 'Loading innovation proposals...' : 'No solution proposals submitted yet.'}
            </div>
          ) : (
            proposals.map((p) => (
              <div
                key={p.id}
                className="bg-white p-6 rounded-2xl shadow-sm border border-slate-200/80 hover:shadow-md transition-shadow flex flex-col justify-between"
              >
                <div>
                  <div className="flex items-center justify-between gap-2 mb-3">
                    <span className="font-mono text-xs font-extrabold text-amber-600">
                      {p.trackingNumber}
                    </span>
                    <StatusBadge status={p.status} />
                  </div>

                  <h2 className="text-base font-bold text-slate-900 line-clamp-2">{p.title}</h2>
                  <p className="text-xs text-slate-500 mt-2 line-clamp-3 leading-relaxed">
                    {p.executiveSummary}
                  </p>

                  <div className="mt-4 pt-4 border-t border-slate-100 grid grid-cols-2 gap-3 text-xs">
                    <div>
                      <span className="text-slate-400 block">Technology Readiness</span>
                      <span className="font-bold text-slate-800">TRL {p.trlLevel} / 9</span>
                    </div>
                    <div>
                      <span className="text-slate-400 block">Budget Required</span>
                      <span className="font-bold text-slate-800">₹{(p.budgetRequired / 100000).toFixed(1)} Lakhs</span>
                    </div>
                  </div>

                  <div className="mt-3 flex items-center gap-1.5 text-xs text-slate-600">
                    <Users className="w-3.5 h-3.5 text-slate-400" />
                    <span>Team: <strong>{p.teamName}</strong></span>
                  </div>
                </div>

                <div className="mt-6 pt-4 border-t border-slate-100 flex items-center justify-between">
                  <span className="text-[11px] text-slate-400">Timeline: {p.timelineWeeks} Weeks</span>
                  <Link
                    to={`/innovation/proposals/${p.id}`}
                    className="text-xs font-bold text-amber-600 hover:text-amber-700 flex items-center gap-1"
                  >
                    <span>View R&D Details</span>
                    <ArrowRight className="w-3.5 h-3.5" />
                  </Link>
                </div>
              </div>
            ))
          )}
        </div>
      )}

      {/* Tab 2: Innovation Challenges */}
      {activeTab === 'challenges' && (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {innovationChallenges.map((c) => (
            <div
              key={c.id}
              className="bg-white p-6 rounded-2xl shadow-sm border border-slate-200/80 hover:shadow-md transition-shadow"
            >
              <div className="flex items-center justify-between gap-2 mb-2">
                <span className="font-mono text-xs font-bold text-amber-600">{c.trackingNumber}</span>
                <span className="text-xs font-bold px-2 py-0.5 rounded bg-purple-50 text-purple-700 border border-purple-200">
                  Innovation Required
                </span>
              </div>
              <h2 className="text-base font-bold text-slate-900">{c.title}</h2>
              <p className="text-xs text-slate-600 mt-2 line-clamp-3 leading-relaxed">{c.description}</p>
              <div className="mt-4 pt-4 border-t border-slate-100 flex items-center justify-between">
                <span className="text-xs text-slate-500">Domain: {c.domainName || c.domainCode}</span>
                <button
                  onClick={() => {
                    setSelectedChallengeId(c.id);
                    setShowModal(true);
                  }}
                  className="px-3 py-1.5 rounded-lg bg-slate-900 text-white text-xs font-bold hover:bg-amber-500 hover:text-slate-950 transition-colors"
                >
                  Propose Solution →
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Modal: New Proposal */}
      {showModal && (
        <div className="fixed inset-0 bg-slate-950/60 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl p-6 sm:p-8 max-w-xl w-full shadow-2xl border border-slate-200 max-h-[90vh] overflow-y-auto">
            <h2 className="text-lg font-black text-slate-900 tracking-tight mb-4">
              Submit Multidisciplinary Solution Proposal
            </h2>
            <form onSubmit={handleCreateProposal} className="space-y-4 text-xs">
              <div>
                <label className="block font-bold text-slate-700 mb-1">Target Societal Challenge</label>
                <select
                  value={selectedChallengeId}
                  onChange={(e) => setSelectedChallengeId(e.target.value)}
                  required
                  className="w-full p-2.5 rounded-xl border border-slate-200 font-medium"
                >
                  <option value="">Select an Innovation Challenge</option>
                  {innovationChallenges.map((c) => (
                    <option key={c.id} value={c.id}>
                      {c.trackingNumber} - {c.title}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block font-bold text-slate-700 mb-1">Research Team Name</label>
                <input
                  type="text"
                  value={teamName}
                  onChange={(e) => setTeamName(e.target.value)}
                  required
                  placeholder="e.g. JalShuddhi Ceramic Nanomaterial Lab"
                  className="w-full p-2.5 rounded-xl border border-slate-200"
                />
              </div>

              <div>
                <label className="block font-bold text-slate-700 mb-1">Solution Title</label>
                <input
                  type="text"
                  value={proposalTitle}
                  onChange={(e) => setProposalTitle(e.target.value)}
                  required
                  placeholder="e.g. Gravity-Fed Terracotta Nanocomposite Filtration System"
                  className="w-full p-2.5 rounded-xl border border-slate-200"
                />
              </div>

              <div>
                <label className="block font-bold text-slate-700 mb-1">Executive Summary</label>
                <textarea
                  value={summary}
                  onChange={(e) => setSummary(e.target.value)}
                  required
                  rows={3}
                  placeholder="High-level solution overview..."
                  className="w-full p-2.5 rounded-xl border border-slate-200"
                />
              </div>

              <div>
                <label className="block font-bold text-slate-700 mb-1">Technical Approach & Methodology</label>
                <textarea
                  value={approach}
                  onChange={(e) => setApproach(e.target.value)}
                  required
                  rows={3}
                  placeholder="Detailed scientific and engineering methodology..."
                  className="w-full p-2.5 rounded-xl border border-slate-200"
                />
              </div>

              <div className="grid grid-cols-3 gap-3">
                <div>
                  <label className="block font-bold text-slate-700 mb-1">TRL Level (1-9)</label>
                  <input
                    type="number"
                    min={1}
                    max={9}
                    value={trlLevel}
                    onChange={(e) => setTrlLevel(Number(e.target.value))}
                    className="w-full p-2.5 rounded-xl border border-slate-200"
                  />
                </div>
                <div>
                  <label className="block font-bold text-slate-700 mb-1">Budget (₹)</label>
                  <input
                    type="number"
                    step={10000}
                    value={budget}
                    onChange={(e) => setBudget(Number(e.target.value))}
                    className="w-full p-2.5 rounded-xl border border-slate-200"
                  />
                </div>
                <div>
                  <label className="block font-bold text-slate-700 mb-1">Timeline (Weeks)</label>
                  <input
                    type="number"
                    min={1}
                    max={52}
                    value={weeks}
                    onChange={(e) => setWeeks(Number(e.target.value))}
                    className="w-full p-2.5 rounded-xl border border-slate-200"
                  />
                </div>
              </div>

              <div className="flex items-center justify-end gap-3 pt-4 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="px-4 py-2 rounded-xl text-slate-500 font-bold hover:bg-slate-100 transition-colors"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className="px-5 py-2 rounded-xl bg-amber-500 text-slate-950 font-bold hover:bg-amber-400 transition-colors shadow-md shadow-amber-500/20"
                >
                  {submitting ? 'Submitting...' : 'Submit to Review Committee'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
