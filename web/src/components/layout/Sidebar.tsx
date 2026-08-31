import React from 'react';
import { NavLink } from 'react-router-dom';
import {
  CheckSquare,
  LayoutDashboard,
  MapPin,
  Lightbulb,
  Briefcase,
  BarChart3,
  FileText,
} from 'lucide-react';

export const Sidebar: React.FC = () => {
  const navItems = [
    {
      label: 'Institutional Action Center',
      path: '/action-center',
      icon: CheckSquare,
      badge: 'Tasks',
    },
    {
      label: 'Government Command Center',
      path: '/government',
      icon: LayoutDashboard,
      badge: 'Triage',
    },
    {
      label: 'Live GIS Geo-Map',
      path: '/map',
      icon: MapPin,
      badge: 'Live',
    },
    {
      label: 'University Innovation Hub',
      path: '/innovation',
      icon: Lightbulb,
      badge: 'TRL R&D',
    },
    {
      label: 'Industry, MSME & CSR',
      path: '/partnerships',
      icon: Briefcase,
      badge: 'Matching',
    },
    {
      label: 'National Impact Analytics',
      path: '/analytics',
      icon: BarChart3,
      badge: 'Overview',
    },
  ];

  return (
    <aside className="w-64 bg-slate-900 text-white min-h-[calc(100vh-61px)] flex flex-col justify-between p-4 border-r border-slate-800">
      <div className="space-y-1">
        <div className="px-3 py-2 text-[11px] font-bold uppercase tracking-wider text-slate-400">
          Core Portals
        </div>
        {navItems.map((item) => {
          const Icon = item.icon;
          return (
            <NavLink
              key={item.path}
              to={item.path}
              className={({ isActive }) =>
                `flex items-center justify-between px-3.5 py-2.5 rounded-xl text-sm font-medium transition-all ${
                  isActive
                    ? 'bg-amber-500 text-slate-950 font-bold shadow-md shadow-amber-500/20'
                    : 'text-slate-300 hover:text-white hover:bg-slate-800/80'
                }`
              }
            >
              <div className="flex items-center gap-3">
                <Icon className="w-4 h-4" />
                <span>{item.label}</span>
              </div>
              {item.badge && (
                <span className="text-[10px] font-bold px-1.5 py-0.5 rounded bg-slate-800/90 text-slate-300 border border-slate-700/50">
                  {item.badge}
                </span>
              )}
            </NavLink>
          );
        })}
      </div>

      {/* GovTech Watermark / Demo Badge */}
      <div className="p-3.5 rounded-xl bg-slate-800/60 border border-slate-700/50 text-xs text-slate-400">
        <div className="flex items-center gap-2 text-amber-400 font-bold mb-1">
          <FileText className="w-3.5 h-3.5" />
          <span>SIH PS 26043 Ecosystem</span>
        </div>
        <p className="text-[11px] leading-relaxed text-slate-400">
          Integrated Crowdsourcing, Multidisciplinary R&D & CSR Deployment.
        </p>
      </div>
    </aside>
  );
};
