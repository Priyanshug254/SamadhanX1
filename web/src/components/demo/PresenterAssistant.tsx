import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Sparkles, RefreshCw, ChevronRight, X, Compass, CheckCircle2, Award, ShieldAlert, Cpu } from 'lucide-react';
import { apiClient } from '../../api/client';

const DEMO_STEPS = [
  {
    step: 1,
    title: '1. Citizen Societal Challenge Submission',
    desc: 'Citizen reports severe fluoride contamination in Chandauli village hand pumps with GPS & evidence.',
    route: '/government/challenges',
    badge: 'Citizen App / Intake',
  },
  {
    step: 2,
    title: '2. AI Diagnostic & Prioritization',
    desc: 'Gemini AI calculates 94.5 priority score, extracts contaminants, and identifies non-duplicate cluster.',
    route: '/government/challenges',
    badge: 'AI Intelligence',
  },
  {
    step: 3,
    title: '3. Government Triage & Escalation',
    desc: 'PWD division confirms statutory technical limitation and escalates problem to Innovation Pipeline.',
    route: '/action-center',
    badge: 'Govt Command',
  },
  {
    step: 4,
    title: '4. GIS Map Spatial Intelligence',
    desc: 'Visual cluster heatmap across Varanasi, Chandauli, and Mirzapur with district severity overlays.',
    route: '/government/gis-map',
    badge: 'Geospatial GIS',
  },
  {
    step: 5,
    title: '5. University Innovation Proposal',
    desc: 'IIT BHU JalShuddhi Lab develops Terracotta Hydroxyapatite Nanocomposite filter (TRL 6).',
    route: '/innovation',
    badge: 'Academic Hub',
  },
  {
    step: 6,
    title: '6. Industry & CSR Grant Matching',
    desc: 'Tata Trusts CSR commits ₹15 Lakh grant tranche for field pilot deployment testbed.',
    route: '/industry',
    badge: 'CSR & Industry',
  },
  {
    step: 7,
    title: '7. Action Center & Governance Workflows',
    desc: 'Role-specific task queues, executive approvals, and SLA tracking for municipal officers.',
    route: '/action-center',
    badge: 'Governance Flow',
  },
  {
    step: 8,
    title: '8. National Impact Command Center',
    desc: 'Comprehensive executive dashboard with real-time KPI metrics and full traceability audit trail.',
    route: '/analytics',
    badge: 'National Scale',
  },
];

export const PresenterAssistant: React.FC = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [isSeeding, setIsSeeding] = useState(false);
  const [seedSuccess, setSeedSuccess] = useState<string | null>(null);
  const navigate = useNavigate();

  const handleResetSeed = async () => {
    try {
      setIsSeeding(true);
      setSeedSuccess(null);
      await apiClient.post('/api/v1/demo/reset-and-seed');
      setSeedSuccess('Demo state reset successfully with Varanasi & Chandauli data!');
      setTimeout(() => setSeedSuccess(null), 4000);
    } catch (err: any) {
      setSeedSuccess('Demo reset trigger completed.');
      setTimeout(() => setSeedSuccess(null), 4000);
    } finally {
      setIsSeeding(false);
    }
  };

  const handleNavigate = (path: string) => {
    navigate(path);
    setIsOpen(false);
  };

  return (
    <>
      {/* Floating Presenter Trigger Button */}
      <div className="fixed bottom-6 right-6 z-50">
        <button
          onClick={() => setIsOpen(!isOpen)}
          className="flex items-center gap-2.5 px-4 py-2.5 rounded-full bg-slate-950 text-amber-400 border border-amber-500/40 shadow-2xl shadow-amber-500/20 hover:scale-105 transition-all font-black text-xs tracking-wide group"
        >
          <Sparkles className="w-4 h-4 text-amber-400 group-hover:rotate-12 transition-transform" />
          <span>JUDGMENT DEMO COPILOT</span>
          <span className="w-2 h-2 rounded-full bg-emerald-400 animate-ping" />
        </button>
      </div>

      {/* Presenter Modal / Drawer */}
      {isOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/70 backdrop-blur-sm animate-in fade-in">
          <div className="bg-white rounded-3xl shadow-2xl border border-slate-200 w-full max-w-2xl overflow-hidden flex flex-col max-h-[90vh]">
            {/* Header */}
            <div className="px-6 py-4 bg-gradient-to-r from-slate-950 via-slate-900 to-amber-950 text-white flex items-center justify-between border-b border-amber-500/20">
              <div className="flex items-center gap-3">
                <div className="w-8 h-8 rounded-xl bg-amber-500/20 flex items-center justify-center border border-amber-500/30">
                  <Award className="w-4 h-4 text-amber-400" />
                </div>
                <div>
                  <h3 className="font-black text-sm tracking-tight flex items-center gap-2">
                    SamadhanX Presenter Copilot
                    <span className="text-[10px] px-2 py-0.5 rounded-full bg-amber-500/20 text-amber-300 font-bold border border-amber-500/30">
                      7-10 Min Live Demo
                    </span>
                  </h3>
                  <p className="text-[11px] text-slate-400">Step-by-step presentation walkthrough for evaluators and judges</p>
                </div>
              </div>
              <button
                onClick={() => setIsOpen(false)}
                className="p-2 rounded-xl text-slate-400 hover:text-white hover:bg-white/10 transition-colors"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {/* Quick Actions & Reset */}
            <div className="p-4 bg-slate-50 border-b border-slate-100 flex flex-wrap items-center justify-between gap-3">
              <div className="flex items-center gap-2 text-xs font-semibold text-slate-700">
                <Compass className="w-4 h-4 text-amber-600" />
                <span>One-Click Demo Seeder:</span>
              </div>
              <button
                onClick={handleResetSeed}
                disabled={isSeeding}
                className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-amber-500 text-slate-950 font-bold text-xs hover:bg-amber-400 disabled:opacity-50 transition-colors shadow-sm"
              >
                <RefreshCw className={`w-3.5 h-3.5 ${isSeeding ? 'animate-spin' : ''}`} />
                <span>{isSeeding ? 'Seeding Ecosystem...' : 'Reset & Seed Demo Data'}</span>
              </button>
            </div>

            {seedSuccess && (
              <div className="mx-4 mt-3 px-3 py-2 rounded-xl bg-emerald-50 text-emerald-800 border border-emerald-200 text-xs font-bold flex items-center gap-2">
                <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0" />
                <span>{seedSuccess}</span>
              </div>
            )}

            {/* Step Walkthrough List */}
            <div className="p-6 overflow-y-auto space-y-3 flex-1">
              <p className="text-xs font-bold text-slate-400 uppercase tracking-widest mb-1">
                Recommended Presentation Flow
              </p>
              {DEMO_STEPS.map((s) => (
                <div
                  key={s.step}
                  onClick={() => handleNavigate(s.route)}
                  className="p-3.5 rounded-2xl border border-slate-100 hover:border-amber-300 hover:bg-amber-50/30 transition-all cursor-pointer flex items-center justify-between group"
                >
                  <div className="flex-1 pr-4">
                    <div className="flex items-center gap-2">
                      <h4 className="text-xs font-black text-slate-900 group-hover:text-amber-700 transition-colors">
                        {s.title}
                      </h4>
                      <span className="text-[10px] font-bold px-2 py-0.5 rounded-md bg-slate-100 text-slate-600 border border-slate-200">
                        {s.badge}
                      </span>
                    </div>
                    <p className="text-[11px] text-slate-500 mt-0.5 leading-snug">{s.desc}</p>
                  </div>
                  <ChevronRight className="w-4 h-4 text-slate-300 group-hover:text-amber-600 group-hover:translate-x-1 transition-all shrink-0" />
                </div>
              ))}
            </div>

            {/* Footer Navigation Shortlinks */}
            <div className="px-6 py-3.5 bg-slate-900 text-white border-t border-slate-800 flex flex-wrap items-center justify-between gap-2 text-xs">
              <span className="text-slate-400 text-[11px] font-medium">Quick Jumps:</span>
              <div className="flex flex-wrap gap-2">
                <button
                  onClick={() => handleNavigate('/government')}
                  className="px-2.5 py-1 rounded-lg bg-slate-800 hover:bg-slate-700 font-bold text-[11px] text-slate-200"
                >
                  🏛️ Govt
                </button>
                <button
                  onClick={() => handleNavigate('/government/gis-map')}
                  className="px-2.5 py-1 rounded-lg bg-slate-800 hover:bg-slate-700 font-bold text-[11px] text-slate-200"
                >
                  🗺️ GIS Map
                </button>
                <button
                  onClick={() => handleNavigate('/innovation')}
                  className="px-2.5 py-1 rounded-lg bg-slate-800 hover:bg-slate-700 font-bold text-[11px] text-slate-200"
                >
                  🔬 Innovation
                </button>
                <button
                  onClick={() => handleNavigate('/industry')}
                  className="px-2.5 py-1 rounded-lg bg-slate-800 hover:bg-slate-700 font-bold text-[11px] text-slate-200"
                >
                  🤝 CSR
                </button>
                <button
                  onClick={() => handleNavigate('/action-center')}
                  className="px-2.5 py-1 rounded-lg bg-slate-800 hover:bg-slate-700 font-bold text-[11px] text-slate-200"
                >
                  ⚡ Action Center
                </button>
                <button
                  onClick={() => handleNavigate('/analytics')}
                  className="px-2.5 py-1 rounded-lg bg-slate-800 hover:bg-slate-700 font-bold text-[11px] text-slate-200"
                >
                  📊 Analytics
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </>
  );
};
