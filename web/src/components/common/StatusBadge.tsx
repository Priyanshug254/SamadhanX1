import React from 'react';
import { ChallengeStatus, ProposalStatus } from '../../types';

interface StatusBadgeProps {
  status: ChallengeStatus | ProposalStatus | string;
  className?: string;
}

export const StatusBadge: React.FC<StatusBadgeProps> = ({ status, className = '' }) => {
  const getStatusConfig = (s: string) => {
    switch (s) {
      case 'SUBMITTED':
        return { label: 'Submitted', bg: 'bg-blue-50', text: 'text-blue-700', border: 'border-blue-200', dot: 'bg-blue-500' };
      case 'AI_CATEGORIZED':
        return { label: 'AI Analyzed', bg: 'bg-indigo-50', text: 'text-indigo-700', border: 'border-indigo-200', dot: 'bg-indigo-500' };
      case 'FLAGGED_DUPLICATE':
        return { label: 'Duplicate Flag', bg: 'bg-amber-50', text: 'text-amber-700', border: 'border-amber-200', dot: 'bg-amber-500' };
      case 'ASSIGNED_TO_DEPARTMENT':
      case 'UNDER_DEPARTMENT_TRIAGE':
        return { label: 'Under Triage', bg: 'bg-cyan-50', text: 'text-cyan-700', border: 'border-cyan-200', dot: 'bg-cyan-500' };
      case 'IN_PROGRESS_DEPARTMENTAL':
        return { label: 'Dept In Progress', bg: 'bg-teal-50', text: 'text-teal-700', border: 'border-teal-200', dot: 'bg-teal-500' };
      case 'RESOLVED_DEPARTMENTAL':
      case 'RESOLVED_INNOVATION':
      case 'AWARDED':
      case 'VALIDATED':
      case 'COMPLETED':
        return { label: 'Resolved', bg: 'bg-emerald-50', text: 'text-emerald-700', border: 'border-emerald-200', dot: 'bg-emerald-500' };
      case 'INNOVATION_REQUIRED':
      case 'INNOVATION_CHALLENGE_ACTIVE':
        return { label: 'Innovation Required', bg: 'bg-purple-50', text: 'text-purple-700', border: 'border-purple-200', dot: 'bg-purple-500' };
      case 'SHORTLISTED':
        return { label: 'Shortlisted', bg: 'bg-violet-50', text: 'text-violet-700', border: 'border-violet-200', dot: 'bg-violet-500' };
      case 'PROTOTYPING':
      case 'PROTOTYPE_DEPLOYED':
        return { label: 'Prototyping', bg: 'bg-orange-50', text: 'text-orange-700', border: 'border-orange-200', dot: 'bg-orange-500' };
      case 'PILOT_READY':
        return { label: 'Pilot Ready', bg: 'bg-emerald-50', text: 'text-emerald-700', border: 'border-emerald-200', dot: 'bg-emerald-500' };
      case 'REJECTED':
        return { label: 'Rejected', bg: 'bg-rose-50', text: 'text-rose-700', border: 'border-rose-200', dot: 'bg-rose-500' };
      default:
        return { label: s.replace(/_/g, ' '), bg: 'bg-slate-50', text: 'text-slate-700', border: 'border-slate-200', dot: 'bg-slate-400' };
    }
  };

  const config = getStatusConfig(status);

  return (
    <span
      className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold border ${config.bg} ${config.text} ${config.border} ${className}`}
    >
      <span className={`w-1.5 h-1.5 rounded-full ${config.dot}`} />
      {config.label}
    </span>
  );
};
