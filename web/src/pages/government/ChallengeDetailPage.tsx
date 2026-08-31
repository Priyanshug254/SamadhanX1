import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { challengesApi } from '../../api/challenges';
import { Challenge, TimelineEvent } from '../../types';
import { StatusBadge } from '../../components/common/StatusBadge';
import { PriorityMeter } from '../../components/common/PriorityMeter';
import {
  ArrowLeft,
  MapPin,
  Clock,
  Sparkles,
  Building2,
  CheckCircle2,
  Layers,
  FileCheck,
  User,
  ThumbsUp,
} from 'lucide-react';

export const ChallengeDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [challenge, setChallenge] = useState<Challenge | null>(null);
  const [timeline, setTimeline] = useState<TimelineEvent[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [actionLoading, setActionLoading] = useState<boolean>(false);
  const [resolutionSummary, setResolutionSummary] = useState<string>('');
  const [escalateReason, setEscalateReason] = useState<string>('');
  const [activeTab, setActiveTab] = useState<'details' | 'actions' | 'timeline'>('details');

  const loadChallenge = async () => {
    if (!id) return;
    setLoading(true);
    try {
      const [challengeData, timelineData] = await Promise.all([
        challengesApi.getChallengeById(id),
        challengesApi.getChallengeTimeline(id).catch(() => []),
      ]);
      setChallenge(challengeData);
      setTimeline(timelineData);
    } catch (err) {
      console.error('Failed to load challenge detail', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadChallenge();
  }, [id]);

  const handleResolveDepartmental = async () => {
    if (!id || !resolutionSummary.trim()) return;
    setActionLoading(true);
    try {
      await challengesApi.resolveDepartmental(id, resolutionSummary);
      await loadChallenge();
      setActiveTab('details');
    } catch (err) {
      alert('Failed to record resolution.');
    } finally {
      setActionLoading(false);
    }
  };

  const handleEscalateInnovation = async () => {
    if (!id || !escalateReason.trim()) return;
    setActionLoading(true);
    try {
      await challengesApi.escalateToInnovation(id, escalateReason, challenge?.domainCode);
      await loadChallenge();
      setActiveTab('details');
    } catch (err) {
      alert('Failed to escalate to innovation.');
    } finally {
      setActionLoading(false);
    }
  };

  if (loading || !challenge) {
    return (
      <div className="p-12 text-center text-slate-500">
        <div className="w-8 h-8 border-4 border-amber-500 border-t-transparent rounded-full animate-spin mx-auto mb-3" />
        <p className="text-sm font-semibold">Loading Challenge Dossier...</p>
      </div>
    );
  }

  return (
    <div className="space-y-6 max-w-5xl mx-auto">
      {/* Top Breadcrumb & Actions */}
      <div className="flex items-center justify-between">
        <button
          onClick={() => navigate('/government')}
          className="inline-flex items-center gap-2 text-xs font-bold text-slate-600 hover:text-slate-900 transition-colors"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>Back to Command Queue</span>
        </button>
        <div className="flex items-center gap-2">
          <StatusBadge status={challenge.status} />
        </div>
      </div>

      {/* Header Card */}
      <div className="glass-panel p-6 rounded-3xl shadow-sm border border-slate-200/80">
        <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4">
          <div>
            <div className="flex items-center gap-2 mb-2">
              <span className="font-mono text-xs font-extrabold px-2.5 py-1 rounded bg-amber-100/80 text-amber-900 border border-amber-200">
                {challenge.trackingNumber}
              </span>
              <span className="text-xs font-bold px-2 py-0.5 rounded bg-slate-100 text-slate-700">
                {challenge.domainName || challenge.domainCode}
              </span>
            </div>
            <h1 className="text-2xl font-black text-slate-900 tracking-tight">{challenge.title}</h1>
            <p className="text-xs text-slate-500 flex items-center gap-2 mt-1">
              <span>Reported: {new Date(challenge.createdAt).toLocaleDateString()}</span>
              <span>•</span>
              <span className="flex items-center gap-1">
                <ThumbsUp className="w-3 h-3 text-amber-500" />
                <strong>{challenge.endorsementsCount} Community Endorsements</strong>
              </span>
            </p>
          </div>

          <div className="w-full lg:w-72 bg-slate-50 p-4 rounded-2xl border border-slate-200/70">
            <PriorityMeter score={challenge.priorityScore || 50} />
          </div>
        </div>
      </div>

      {/* Tabs */}
      <div className="flex border-b border-slate-200 gap-6 text-sm font-bold text-slate-500">
        <button
          onClick={() => setActiveTab('details')}
          className={`pb-3 transition-colors ${
            activeTab === 'details' ? 'border-b-2 border-amber-500 text-slate-900' : 'hover:text-slate-900'
          }`}
        >
          Challenge Dossier & Evidence
        </button>
        <button
          onClick={() => setActiveTab('actions')}
          className={`pb-3 transition-colors ${
            activeTab === 'actions' ? 'border-b-2 border-amber-500 text-slate-900' : 'hover:text-slate-900'
          }`}
        >
          Government Triage & Actions
        </button>
        <button
          onClick={() => setActiveTab('timeline')}
          className={`pb-3 transition-colors ${
            activeTab === 'timeline' ? 'border-b-2 border-amber-500 text-slate-900' : 'hover:text-slate-900'
          }`}
        >
          Audit Timeline ({timeline.length})
        </button>
      </div>

      {/* Tab: Details */}
      {activeTab === 'details' && (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="md:col-span-2 space-y-6">
            {/* Problem Statement */}
            <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-200/80">
              <h2 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-2">Description & Impact</h2>
              <p className="text-sm text-slate-700 leading-relaxed whitespace-pre-wrap">{challenge.description}</p>
            </div>

            {/* AI Diagnostics */}
            {/* AI Diagnostics & Explainability */}
            <div className="bg-gradient-to-br from-slate-900 to-indigo-950 text-white p-6 rounded-2xl shadow-md border border-slate-800 space-y-4">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2 text-amber-400 text-xs font-bold uppercase tracking-wider">
                  <Sparkles className="w-4 h-4" />
                  <span>AI Intelligence & Multi-Factor Assessment</span>
                </div>
                <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-slate-800 text-slate-300 border border-slate-700">
                  {challenge.aiModelProvider || 'Google Gemini LLM / Safety Fallback'}
                </span>
              </div>

              <div className="grid grid-cols-2 sm:grid-cols-3 gap-4 text-xs pt-1">
                <div>
                  <span className="text-slate-400 block">AI Domain Confidence</span>
                  <span className="text-base font-bold text-white">
                    {challenge.aiCategoryConfidence ? `${(challenge.aiCategoryConfidence * 100).toFixed(0)}%` : '92%'}
                  </span>
                </div>
                <div>
                  <span className="text-slate-400 block">Duplicate Intelligence</span>
                  <span className="text-base font-bold text-white">
                    {challenge.isDuplicateFlagged ? 'Potential Duplicate' : 'Unique (Original)'}
                  </span>
                </div>
                <div>
                  <span className="text-slate-400 block">Target Routing Pipeline</span>
                  <span className="text-base font-bold text-amber-400">
                    {challenge.status.includes('INNOVATION') ? 'University R&D Hub' : 'Standard Department'}
                  </span>
                </div>
              </div>

              {/* AI Explainability & Reasoning */}
              <div className="space-y-2 pt-3 border-t border-slate-800 text-xs">
                {challenge.aiReasoning && (
                  <div className="p-2.5 rounded-xl bg-slate-800/60 border border-slate-700/50">
                    <span className="text-amber-400 font-bold block mb-0.5">AI Categorization Reasoning:</span>
                    <p className="text-slate-300 leading-relaxed">{challenge.aiReasoning}</p>
                  </div>
                )}

                {challenge.aiPriorityReasoning && (
                  <div className="p-2.5 rounded-xl bg-slate-800/60 border border-slate-700/50">
                    <span className="text-amber-400 font-bold block mb-0.5">Priority Intelligence Factor:</span>
                    <p className="text-slate-300 leading-relaxed">{challenge.aiPriorityReasoning}</p>
                  </div>
                )}

                {challenge.aiDuplicateExplanation && (
                  <div className="p-2.5 rounded-xl bg-slate-800/60 border border-slate-700/50">
                    <span className="text-amber-400 font-bold block mb-0.5">Duplicate Analysis:</span>
                    <p className="text-slate-300 leading-relaxed">{challenge.aiDuplicateExplanation}</p>
                  </div>
                )}

                <p className="text-[10px] text-slate-400 italic pt-1">
                  * Note: AI outputs are algorithmic recommendations to assist triage and do not constitute a final statutory determination.
                </p>
              </div>
            </div>

            {/* Evidence & Attachments */}
            <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-200/80">
              <h2 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-3">
                Multimedia Evidence & Attachments ({challenge.attachments?.length || 0})
              </h2>
              {(!challenge.attachments || challenge.attachments.length === 0) ? (
                <p className="text-xs text-slate-400 italic">No media attachments provided.</p>
              ) : (
                <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
                  {challenge.attachments.map((att) => (
                    <div key={att.id} className="p-3 rounded-xl bg-slate-50 border border-slate-200 flex flex-col justify-between">
                      <div>
                        <span className="text-[10px] font-bold px-1.5 py-0.5 rounded bg-slate-200 text-slate-700">
                          {att.mediaType}
                        </span>
                        <p className="text-xs font-medium text-slate-800 mt-2 truncate">{att.fileName}</p>
                      </div>
                      <a
                        href={att.fileUrl}
                        target="_blank"
                        rel="noreferrer"
                        className="mt-3 text-[11px] font-bold text-amber-600 hover:text-amber-700"
                      >
                        View Attachment →
                      </a>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>

          {/* Sidebar Info */}
          <div className="space-y-6">
            <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-200/80 space-y-4 text-xs">
              <h2 className="font-bold uppercase tracking-wider text-slate-400">Routing & Location</h2>
              <div>
                <span className="text-slate-400 block">Location Coordinates</span>
                <span className="font-mono font-bold text-slate-800 flex items-center gap-1 mt-0.5">
                  <MapPin className="w-3.5 h-3.5 text-amber-500" />
                  {challenge.latitude.toFixed(5)}, {challenge.longitude.toFixed(5)}
                </span>
                <p className="text-slate-500 mt-1">{challenge.addressText || `${challenge.district}, ${challenge.state}`}</p>
              </div>

              <div>
                <span className="text-slate-400 block">Responsible Department</span>
                <span className="font-bold text-slate-800 flex items-center gap-1 mt-0.5">
                  <Building2 className="w-3.5 h-3.5 text-slate-400" />
                  {challenge.departmentName || challenge.departmentCode || 'General Municipal Administration'}
                </span>
              </div>

              <div>
                <span className="text-slate-400 block">Submitting Citizen</span>
                <span className="font-bold text-slate-800 flex items-center gap-1 mt-0.5">
                  <User className="w-3.5 h-3.5 text-slate-400" />
                  {challenge.citizenName || 'Verified Citizen'}
                </span>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Tab: Actions */}
      {activeTab === 'actions' && (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {/* Action 1: Standard Resolution */}
          <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-200/80 flex flex-col justify-between">
            <div>
              <div className="flex items-center gap-2 text-emerald-600 font-bold text-sm mb-2">
                <CheckCircle2 className="w-5 h-5" />
                <span>Normal Departmental Resolution</span>
              </div>
              <p className="text-xs text-slate-500 mb-4">
                Use this when standard departmental tools, maintenance crews, or existing budget can resolve the issue directly.
              </p>
              <textarea
                value={resolutionSummary}
                onChange={(e) => setResolutionSummary(e.target.value)}
                placeholder="Provide official resolution summary, maintenance log, or action report..."
                rows={4}
                className="w-full p-3 rounded-xl border border-slate-200 text-xs focus:outline-none focus:border-emerald-500 focus:ring-1 focus:ring-emerald-500"
              />
            </div>
            <button
              onClick={handleResolveDepartmental}
              disabled={actionLoading || !resolutionSummary.trim()}
              className="mt-4 w-full py-2.5 px-4 rounded-xl bg-emerald-600 text-white font-bold text-xs hover:bg-emerald-700 transition-colors disabled:opacity-50"
            >
              {actionLoading ? 'Recording Resolution...' : 'Mark as RESOLVED_DEPARTMENTAL'}
            </button>
          </div>

          {/* Action 2: Escalate to Innovation */}
          <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-200/80 flex flex-col justify-between">
            <div>
              <div className="flex items-center gap-2 text-purple-600 font-bold text-sm mb-2">
                <Layers className="w-5 h-5" />
                <span>Escalate to University Innovation Pipeline</span>
              </div>
              <p className="text-xs text-slate-500 mb-4">
                Use this when the societal challenge lacks a viable standard solution and requires R&D by multidisciplinary university researchers and CSR funding.
              </p>
              <textarea
                value={escalateReason}
                onChange={(e) => setEscalateReason(e.target.value)}
                placeholder="Explain why standard departmental approaches are insufficient and what technological innovation is needed..."
                rows={4}
                className="w-full p-3 rounded-xl border border-slate-200 text-xs focus:outline-none focus:border-purple-500 focus:ring-1 focus:ring-purple-500"
              />
            </div>
            <button
              onClick={handleEscalateInnovation}
              disabled={actionLoading || !escalateReason.trim()}
              className="mt-4 w-full py-2.5 px-4 rounded-xl bg-purple-600 text-white font-bold text-xs hover:bg-purple-700 transition-colors disabled:opacity-50"
            >
              {actionLoading ? 'Escalating...' : 'Escalate to INNOVATION_REQUIRED'}
            </button>
          </div>
        </div>
      )}

      {/* Tab: Timeline */}
      {activeTab === 'timeline' && (
        <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-200/80">
          <h2 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-6">Complete Audit Lifecycle Trail</h2>
          {timeline.length === 0 ? (
            <p className="text-xs text-slate-400 italic">No timeline events recorded.</p>
          ) : (
            <div className="relative border-l-2 border-slate-200 ml-4 space-y-6">
              {timeline.map((evt) => (
                <div key={evt.id} className="relative pl-6">
                  <div className="absolute -left-1.5 top-1.5 w-3 h-3 rounded-full bg-amber-500 border-2 border-white shadow-sm" />
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-bold text-slate-900">{evt.action || evt.toStatus}</span>
                    <span className="text-[11px] text-slate-400">{new Date(evt.createdAt).toLocaleString()}</span>
                  </div>
                  {evt.comments && (
                    <p className="text-xs text-slate-600 mt-1 bg-slate-50 p-2.5 rounded-lg border border-slate-100">
                      {evt.comments}
                    </p>
                  )}
                  {evt.actorEmail && (
                    <span className="text-[10px] text-slate-400 mt-1 block">
                      Actor: {evt.actorEmail} ({evt.actorRole || 'SYSTEM'})
                    </span>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
};
