import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { innovationApi } from '../../api/innovation';
import { Proposal, ProjectTeam, ProposalStatus } from '../../types';
import { StatusBadge } from '../../components/common/StatusBadge';
import {
  ArrowLeft,
  Users,
  Award,
  Layers,
  Sparkles,
  ArrowRight,
  CheckCircle2,
  Briefcase,
  UserPlus,
} from 'lucide-react';

export const ProposalDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [proposal, setProposal] = useState<Proposal | null>(null);
  const [teams, setTeams] = useState<ProjectTeam[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [updating, setUpdating] = useState<boolean>(false);
  const [inviteEmail, setInviteEmail] = useState<string>('');
  const [inviteRole, setInviteRole] = useState<string>('STUDENT_RESEARCHER');

  const loadData = async () => {
    if (!id) return;
    setLoading(true);
    try {
      const p = await innovationApi.getProposalById(id);
      setProposal(p);
      if (p.challengeId) {
        const teamList = await innovationApi.getTeamsByChallenge(p.challengeId).catch(() => []);
        setTeams(teamList);
      }
    } catch (err) {
      console.error('Failed to load proposal', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [id]);

  const handleStatusTransition = async (newStatus: ProposalStatus) => {
    if (!id) return;
    setUpdating(true);
    try {
      await innovationApi.updateProposalStatus(id, newStatus, `Transitioned to ${newStatus} during review`);
      await loadData();
    } catch (err) {
      alert('Failed to update proposal status');
    } finally {
      setUpdating(false);
    }
  };

  const handleInviteMember = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!proposal?.teamId || !inviteEmail.trim()) return;
    try {
      await innovationApi.inviteMember(proposal.teamId, inviteEmail.trim(), inviteRole);
      setInviteEmail('');
      await loadData();
      alert('Invitation dispatched to ' + inviteEmail);
    } catch (err) {
      alert('Failed to invite member');
    }
  };

  if (loading || !proposal) {
    return (
      <div className="p-12 text-center text-slate-500">
        <div className="w-8 h-8 border-4 border-amber-500 border-t-transparent rounded-full animate-spin mx-auto mb-3" />
        <p className="text-sm font-semibold">Loading Solution Proposal...</p>
      </div>
    );
  }

  return (
    <div className="space-y-6 max-w-5xl mx-auto">
      {/* Top Bar */}
      <div className="flex items-center justify-between">
        <button
          onClick={() => navigate('/innovation')}
          className="inline-flex items-center gap-2 text-xs font-bold text-slate-600 hover:text-slate-900 transition-colors"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>Back to Innovation Hub</span>
        </button>

        <div className="flex items-center gap-3">
          <Link
            to={`/partnerships?proposalId=${proposal.id}`}
            className="px-3.5 py-1.5 rounded-xl bg-slate-900 text-amber-400 text-xs font-bold hover:bg-slate-800 transition-colors flex items-center gap-1.5 shadow-sm"
          >
            <Briefcase className="w-3.5 h-3.5" />
            <span>Find Industry/CSR Match →</span>
          </Link>
          <StatusBadge status={proposal.status} />
        </div>
      </div>

      {/* Header Banner */}
      <div className="glass-panel p-6 rounded-3xl shadow-sm border border-slate-200/80">
        <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4">
          <div>
            <div className="flex items-center gap-2 mb-2">
              <span className="font-mono text-xs font-extrabold px-2.5 py-1 rounded bg-amber-100 text-amber-900 border border-amber-200">
                {proposal.trackingNumber}
              </span>
              <span className="text-xs font-bold px-2 py-0.5 rounded bg-slate-100 text-slate-700">
                TRL {proposal.trlLevel} / 9
              </span>
            </div>
            <h1 className="text-2xl font-black text-slate-900 tracking-tight">{proposal.title}</h1>
            <p className="text-xs text-slate-500 mt-1">
              Developed by Multidisciplinary Team: <strong className="text-slate-800">{proposal.teamName}</strong>
            </p>
          </div>

          <div className="flex items-center gap-4 bg-slate-50 p-4 rounded-2xl border border-slate-200/70 text-xs">
            <div>
              <span className="text-slate-400 block">Required Budget</span>
              <span className="text-base font-black text-slate-900">₹{(proposal.budgetRequired / 100000).toFixed(2)} L</span>
            </div>
            <div className="h-8 w-px bg-slate-200" />
            <div>
              <span className="text-slate-400 block">Est. Timeline</span>
              <span className="text-base font-black text-slate-900">{proposal.timelineWeeks} Weeks</span>
            </div>
          </div>
        </div>
      </div>

      {/* Main Grid */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="md:col-span-2 space-y-6">
          {/* Executive Summary */}
          <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-200/80">
            <h2 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-2">Executive Summary</h2>
            <p className="text-sm text-slate-700 leading-relaxed whitespace-pre-wrap">{proposal.executiveSummary}</p>
          </div>

          {/* AI Solution Intelligence Blueprint */}
          <div className="bg-gradient-to-br from-slate-900 via-indigo-950 to-slate-900 text-white p-6 rounded-2xl shadow-md border border-slate-800 space-y-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2 text-amber-400 text-xs font-bold uppercase tracking-wider">
                <Sparkles className="w-4 h-4" />
                <span>AI Solution Intelligence & R&D Blueprint</span>
              </div>
              <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-purple-500/20 text-purple-300 border border-purple-500/30">
                AI-Generated Advisory Roadmap
              </span>
            </div>

            <div className="space-y-3 text-xs">
              <div className="bg-slate-800/60 p-3 rounded-xl border border-slate-700/50">
                <span className="text-amber-400 font-bold block mb-1">Recommended Solution Approaches:</span>
                <ul className="list-disc list-inside space-y-1 text-slate-300">
                  <li>Decentralized gravity-fed ceramic membrane filtration with chemical-free adsorbent media</li>
                  <li>IoT solar-powered inline water quality sensor telemetry (pH, TDS, Arsenic/Fluoride)</li>
                  <li>Community backwash and cartridge regeneration protocol</li>
                </ul>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div className="bg-slate-800/60 p-3 rounded-xl border border-slate-700/50">
                  <span className="text-amber-400 font-bold block mb-1">Required Technologies:</span>
                  <p className="text-slate-300">Nanocomposite Ceramic Membranes, ESP32 IoT Telemetry, Activated Alumina</p>
                </div>
                <div className="bg-slate-800/60 p-3 rounded-xl border border-slate-700/50">
                  <span className="text-amber-400 font-bold block mb-1">Suggested Disciplines:</span>
                  <p className="text-slate-300">Materials Science, Civil/Environmental Eng, IoT Embedded Systems</p>
                </div>
              </div>

              <div className="bg-slate-800/60 p-3 rounded-xl border border-slate-700/50 flex justify-between items-center">
                <div>
                  <span className="text-amber-400 font-bold block">Expected Societal Impact:</span>
                  <span className="text-slate-300">100% WHO potable water compliance for 2,000+ villagers.</span>
                </div>
                <span className="font-mono text-xs font-black px-2.5 py-1 rounded bg-amber-500 text-slate-950">
                  Suggested Start: TRL 3
                </span>
              </div>
            </div>
          </div>

          {/* Technical Approach */}
          <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-200/80">
            <h2 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-2">
              Technical Methodology & Architecture
            </h2>
            <p className="text-sm text-slate-700 leading-relaxed whitespace-pre-wrap">{proposal.technicalApproach}</p>
          </div>

          {/* Multidisciplinary Team Members */}
          <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-200/80">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-xs font-bold uppercase tracking-wider text-slate-400">
                Team Composition & Mentors
              </h2>
            </div>

            <form onSubmit={handleInviteMember} className="flex gap-2 mb-4">
              <input
                type="email"
                value={inviteEmail}
                onChange={(e) => setInviteEmail(e.target.value)}
                placeholder="Invite collaborator by email (e.g. mentor@iitbhu.ac.in)..."
                className="flex-1 px-3 py-2 text-xs rounded-xl border border-slate-200 focus:outline-none focus:border-amber-500"
              />
              <select
                value={inviteRole}
                onChange={(e) => setInviteRole(e.target.value)}
                className="px-3 py-2 text-xs rounded-xl border border-slate-200 font-medium"
              >
                <option value="FACULTY_MENTOR">Faculty Mentor</option>
                <option value="STUDENT_RESEARCHER">Student Researcher</option>
                <option value="EXTERNAL_EXPERT">External Expert</option>
              </select>
              <button
                type="submit"
                className="px-4 py-2 bg-slate-900 text-white rounded-xl text-xs font-bold hover:bg-amber-500 hover:text-slate-950 transition-colors"
              >
                Invite
              </button>
            </form>

            <div className="space-y-2">
              <div className="p-3 rounded-xl bg-slate-50 border border-slate-100 flex items-center justify-between text-xs">
                <div>
                  <p className="font-bold text-slate-900">Dr. S. Iyer (Lead Faculty)</p>
                  <p className="text-[11px] text-slate-500">Materials Science & Nanotechnology • IIT BHU</p>
                </div>
                <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-purple-100 text-purple-800">
                  FACULTY MENTOR
                </span>
              </div>

              <div className="p-3 rounded-xl bg-slate-50 border border-slate-100 flex items-center justify-between text-xs">
                <div>
                  <p className="font-bold text-slate-900">Rahul Verma (Project Lead)</p>
                  <p className="text-[11px] text-slate-500">Environmental Engineering • IIT BHU</p>
                </div>
                <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-blue-100 text-blue-800">
                  STUDENT LEAD
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* Sidebar Status & Lifecycle Actions */}
        <div className="space-y-6">
          <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-200/80 space-y-4">
            <h2 className="text-xs font-bold uppercase tracking-wider text-slate-400">TRL Progression & Stage</h2>

            <div className="space-y-2 text-xs">
              <button
                onClick={() => handleStatusTransition('SHORTLISTED')}
                disabled={updating || proposal.status === 'SHORTLISTED'}
                className="w-full py-2 px-3 rounded-xl border border-violet-200 bg-violet-50 text-violet-700 font-bold hover:bg-violet-100 transition-colors disabled:opacity-50 text-left flex items-center justify-between"
              >
                <span>1. Promote to SHORTLISTED</span>
                <CheckCircle2 className="w-4 h-4" />
              </button>

              <button
                onClick={() => handleStatusTransition('PROTOTYPING')}
                disabled={updating || proposal.status === 'PROTOTYPING'}
                className="w-full py-2 px-3 rounded-xl border border-orange-200 bg-orange-50 text-orange-700 font-bold hover:bg-orange-100 transition-colors disabled:opacity-50 text-left flex items-center justify-between"
              >
                <span>2. Advance to PROTOTYPING</span>
                <CheckCircle2 className="w-4 h-4" />
              </button>

              <button
                onClick={() => handleStatusTransition('PILOT_READY')}
                disabled={updating || proposal.status === 'PILOT_READY'}
                className="w-full py-2 px-3 rounded-xl border border-emerald-200 bg-emerald-50 text-emerald-700 font-bold hover:bg-emerald-100 transition-colors disabled:opacity-50 text-left flex items-center justify-between"
              >
                <span>3. Certify PILOT_READY</span>
                <CheckCircle2 className="w-4 h-4" />
              </button>
            </div>
          </div>

          <div className="p-6 rounded-2xl bg-gradient-to-br from-slate-900 to-indigo-950 text-white shadow-md border border-slate-800 text-xs">
            <div className="flex items-center gap-2 text-amber-400 font-bold mb-2">
              <Briefcase className="w-4 h-4" />
              <span>CSR & Industry Deployment</span>
            </div>
            <p className="text-slate-300 leading-relaxed mb-4">
              Matched with Industry & MSME partners for CSR grant disbursement, pilot testing, and real-world deployment.
            </p>
            <Link
              to={`/partnerships?proposalId=${proposal.id}`}
              className="w-full py-2.5 px-4 rounded-xl bg-amber-500 text-slate-950 font-bold hover:bg-amber-400 transition-all flex items-center justify-center gap-2"
            >
              <span>View Match Scores</span>
              <ArrowRight className="w-4 h-4" />
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};
