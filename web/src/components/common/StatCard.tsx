import React from 'react';
import { LucideIcon } from 'lucide-react';

interface StatCardProps {
  title: string;
  value: string | number;
  subtitle?: string;
  icon: LucideIcon;
  color: 'navy' | 'saffron' | 'emerald' | 'rose' | 'purple';
  trend?: string;
}

export const StatCard: React.FC<StatCardProps> = ({ title, value, subtitle, icon: Icon, color, trend }) => {
  const colorStyles = {
    navy: { iconBg: 'bg-slate-900/10 text-slate-900', border: 'hover:border-slate-900/40' },
    saffron: { iconBg: 'bg-amber-500/10 text-amber-600', border: 'hover:border-amber-500/40' },
    emerald: { iconBg: 'bg-emerald-500/10 text-emerald-600', border: 'hover:border-emerald-500/40' },
    rose: { iconBg: 'bg-rose-500/10 text-rose-600', border: 'hover:border-rose-500/40' },
    purple: { iconBg: 'bg-purple-500/10 text-purple-600', border: 'hover:border-purple-500/40' },
  };

  const style = colorStyles[color];

  return (
    <div className={`glass-panel p-5 rounded-2xl shadow-sm border border-slate-200/80 transition-all duration-300 ${style.border}`}>
      <div className="flex items-center justify-between">
        <div>
          <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">{title}</p>
          <p className="text-2xl font-black text-slate-900 mt-1">{value}</p>
          {subtitle && <p className="text-xs text-slate-500 mt-1">{subtitle}</p>}
          {trend && <span className="inline-block text-[11px] font-semibold text-emerald-600 mt-1">{trend}</span>}
        </div>
        <div className={`p-3.5 rounded-xl ${style.iconBg}`}>
          <Icon className="w-6 h-6" />
        </div>
      </div>
    </div>
  );
};
