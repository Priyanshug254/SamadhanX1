import React from 'react';

interface PriorityMeterProps {
  score: number;
  showLabel?: boolean;
  size?: 'sm' | 'md' | 'lg';
}

export const PriorityMeter: React.FC<PriorityMeterProps> = ({ score, showLabel = true, size = 'md' }) => {
  const getLevel = (s: number) => {
    if (s >= 80) return { label: 'CRITICAL', color: 'bg-rose-500', text: 'text-rose-700', bgText: 'bg-rose-50 border-rose-200' };
    if (s >= 60) return { label: 'HIGH', color: 'bg-amber-500', text: 'text-amber-700', bgText: 'bg-amber-50 border-amber-200' };
    if (s >= 40) return { label: 'MEDIUM', color: 'bg-blue-500', text: 'text-blue-700', bgText: 'bg-blue-50 border-blue-200' };
    return { label: 'LOW', color: 'bg-emerald-500', text: 'text-emerald-700', bgText: 'bg-emerald-50 border-emerald-200' };
  };

  const level = getLevel(score);

  const heightClass = size === 'sm' ? 'h-1.5' : size === 'lg' ? 'h-3' : 'h-2';

  return (
    <div className="w-full">
      {showLabel && (
        <div className="flex items-center justify-between mb-1">
          <span className="text-xs font-semibold text-slate-500">Multi-Factor Priority</span>
          <span className={`text-xs font-bold px-1.5 py-0.5 rounded border ${level.bgText} ${level.text}`}>
            {score.toFixed(1)} / 100 • {level.label}
          </span>
        </div>
      )}
      <div className={`w-full bg-slate-100 rounded-full overflow-hidden ${heightClass}`}>
        <div
          className={`${heightClass} rounded-full transition-all duration-500 ${level.color}`}
          style={{ width: `${Math.min(100, Math.max(0, score))}%` }}
        />
      </div>
    </div>
  );
};
