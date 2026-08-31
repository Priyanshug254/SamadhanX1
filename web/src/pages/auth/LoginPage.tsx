import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { ShieldCheck, Lock, Mail, ArrowRight, User as UserIcon, Phone, UserPlus } from 'lucide-react';
import { Role } from '../../types';

export const LoginPage: React.FC = () => {
  const [isRegisterMode, setIsRegisterMode] = useState(false);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [phone, setPhone] = useState('');
  const [role, setRole] = useState<Role>('CITIZEN');
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const { login, register } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setSuccessMessage(null);
    setLoading(true);

    try {
      if (isRegisterMode) {
        await register(email, password, firstName, lastName, phone, role);
        setSuccessMessage('Registration successful! Signing you in...');
        await login(email, password);
      } else {
        await login(email, password);
      }
      navigate('/government');
    } catch (err: any) {
      setError(err.message || 'Authentication failed. Please verify your credentials.');
    } finally {
      setLoading(false);
    }
  };

  const setDemoUser = (demoEmail: string, demoPass: string) => {
    setIsRegisterMode(false);
    setEmail(demoEmail);
    setPassword(demoPass);
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-950 via-slate-900 to-slate-950 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        {/* Brand Header */}
        <div className="text-center mb-8">
          <div className="inline-flex w-14 h-14 rounded-2xl bg-gradient-to-tr from-amber-500 to-amber-600 items-center justify-center shadow-xl shadow-amber-500/20 mb-4 border border-amber-400/30">
            <span className="text-slate-950 font-black text-2xl tracking-tighter">SX</span>
          </div>
          <h1 className="text-2xl font-black text-white tracking-tight">
            Samadhan<span className="text-amber-500">X</span> Command Portal
          </h1>
          <p className="text-xs text-slate-400 mt-1.5 max-w-xs mx-auto">
            Societal Challenge Crowdsourcing & Collaborative Problem-Solving Ecosystem
          </p>
        </div>

        {/* Auth Card */}
        <div className="glass-panel-dark rounded-3xl p-8 shadow-2xl border border-slate-800">
          <div className="flex items-center justify-between mb-6">
            <div className="flex items-center gap-2 text-xs font-bold uppercase tracking-wider text-amber-400">
              <ShieldCheck className="w-4 h-4" />
              <span>{isRegisterMode ? 'Ecosystem Registration' : 'Secure Official Access'}</span>
            </div>

            <button
              type="button"
              onClick={() => {
                setIsRegisterMode(!isRegisterMode);
                setError(null);
                setSuccessMessage(null);
              }}
              className="text-xs text-amber-400 hover:text-amber-300 font-semibold underline underline-offset-2"
            >
              {isRegisterMode ? 'Sign In Instead' : 'Register New Account'}
            </button>
          </div>

          {error && (
            <div className="mb-5 p-3.5 rounded-xl bg-rose-500/10 border border-rose-500/30 text-rose-300 text-xs font-medium">
              {error}
            </div>
          )}

          {successMessage && (
            <div className="mb-5 p-3.5 rounded-xl bg-emerald-500/10 border border-emerald-500/30 text-emerald-300 text-xs font-medium">
              {successMessage}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            {isRegisterMode && (
              <>
                <div className="grid grid-cols-2 gap-2">
                  <div>
                    <label className="block text-xs font-bold text-slate-300 mb-1.5">First Name</label>
                    <div className="relative">
                      <UserIcon className="w-4 h-4 text-slate-400 absolute left-3.5 top-3" />
                      <input
                        type="text"
                        value={firstName}
                        onChange={(e) => setFirstName(e.target.value)}
                        required
                        placeholder="John"
                        className="w-full pl-10 pr-3 py-2 rounded-xl bg-slate-900/90 border border-slate-700 text-white placeholder-slate-500 text-xs focus:outline-none focus:border-amber-500"
                      />
                    </div>
                  </div>
                  <div>
                    <label className="block text-xs font-bold text-slate-300 mb-1.5">Last Name</label>
                    <input
                      type="text"
                      value={lastName}
                      onChange={(e) => setLastName(e.target.value)}
                      required
                      placeholder="Doe"
                      className="w-full px-3 py-2 rounded-xl bg-slate-900/90 border border-slate-700 text-white placeholder-slate-500 text-xs focus:outline-none focus:border-amber-500"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-bold text-slate-300 mb-1.5">Phone Number</label>
                  <div className="relative">
                    <Phone className="w-4 h-4 text-slate-400 absolute left-3.5 top-3" />
                    <input
                      type="tel"
                      value={phone}
                      onChange={(e) => setPhone(e.target.value)}
                      placeholder="+91 98765 43210"
                      className="w-full pl-10 pr-3 py-2 rounded-xl bg-slate-900/90 border border-slate-700 text-white placeholder-slate-500 text-xs focus:outline-none focus:border-amber-500"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-bold text-slate-300 mb-1.5">Ecosystem Role</label>
                  <select
                    value={role}
                    onChange={(e) => setRole(e.target.value as Role)}
                    className="w-full px-3 py-2 rounded-xl bg-slate-900/90 border border-slate-700 text-white text-xs focus:outline-none focus:border-amber-500"
                  >
                    <option value="CITIZEN">Citizen</option>
                    <option value="STUDENT">Student / Researcher</option>
                    <option value="FACULTY">Faculty / Academic Lead</option>
                    <option value="STARTUP">Startup</option>
                    <option value="MSME">MSME Enterprise</option>
                    <option value="INDUSTRY">Industry Enterprise</option>
                    <option value="CSR">CSR / Funding Partner</option>
                    <option value="RESEARCH_LAB">Research Lab</option>
                    <option value="INNOVATION_HUB">Innovation Hub / Incubator</option>
                    <option value="COMMUNITY_ORGANIZATION">Community Organization (NGO)</option>
                  </select>
                </div>
              </>
            )}

            <div>
              <label className="block text-xs font-bold text-slate-300 mb-1.5">Email Address</label>
              <div className="relative">
                <Mail className="w-4 h-4 text-slate-400 absolute left-3.5 top-3" />
                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                  placeholder="name@organization.org"
                  className="w-full pl-10 pr-4 py-2.5 rounded-xl bg-slate-900/90 border border-slate-700 text-white placeholder-slate-500 text-sm focus:outline-none focus:border-amber-500 focus:ring-1 focus:ring-amber-500"
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-300 mb-1.5">Password</label>
              <div className="relative">
                <Lock className="w-4 h-4 text-slate-400 absolute left-3.5 top-3" />
                <input
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                  placeholder="••••••••••••"
                  className="w-full pl-10 pr-4 py-2.5 rounded-xl bg-slate-900/90 border border-slate-700 text-white placeholder-slate-500 text-sm focus:outline-none focus:border-amber-500 focus:ring-1 focus:ring-amber-500"
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full py-3 px-4 rounded-xl bg-gradient-to-r from-amber-500 to-amber-600 text-slate-950 font-bold text-sm shadow-lg shadow-amber-500/25 hover:from-amber-400 hover:to-amber-500 transition-all flex items-center justify-center gap-2 mt-2"
            >
              {loading ? (
                <span>Processing...</span>
              ) : isRegisterMode ? (
                <>
                  <UserPlus className="w-4 h-4" />
                  <span>Create Account</span>
                </>
              ) : (
                <>
                  <span>Sign In to Command Center</span>
                  <ArrowRight className="w-4 h-4" />
                </>
              )}
            </button>
          </form>

          {/* Quick Demo Persona Shortcuts */}
          <div className="mt-8 pt-6 border-t border-slate-800">
            <p className="text-[11px] font-bold uppercase tracking-wider text-slate-400 mb-3 text-center">
              1-Click Demo Accounts
            </p>
            <div className="grid grid-cols-2 gap-2">
              <button
                type="button"
                onClick={() => setDemoUser('admin@samadhanx.gov.in', 'Admin@123456')}
                className="p-2 rounded-lg bg-slate-800/80 hover:bg-slate-800 text-left border border-slate-700/60 text-xs transition-colors"
              >
                <p className="font-bold text-amber-400">Govt Admin</p>
                <p className="text-[10px] text-slate-400 truncate">admin@samadhanx.gov.in</p>
              </button>

              <button
                type="button"
                onClick={() => setDemoUser('official@samadhanx.gov.in', 'Official@123456')}
                className="p-2 rounded-lg bg-slate-800/80 hover:bg-slate-800 text-left border border-slate-700/60 text-xs transition-colors"
              >
                <p className="font-bold text-cyan-400">Dept Official</p>
                <p className="text-[10px] text-slate-400 truncate">official@samadhanx.gov.in</p>
              </button>

              <button
                type="button"
                onClick={() => setDemoUser('faculty@samadhanx.gov.in', 'Faculty@123456')}
                className="p-2 rounded-lg bg-slate-800/80 hover:bg-slate-800 text-left border border-slate-700/60 text-xs transition-colors"
              >
                <p className="font-bold text-purple-400">University Lead</p>
                <p className="text-[10px] text-slate-400 truncate">faculty@samadhanx.gov.in</p>
              </button>

              <button
                type="button"
                onClick={() => setDemoUser('industry@samadhanx.gov.in', 'Industry@123456')}
                className="p-2 rounded-lg bg-slate-800/80 hover:bg-slate-800 text-left border border-slate-700/60 text-xs transition-colors"
              >
                <p className="font-bold text-emerald-400">Industry / CSR</p>
                <p className="text-[10px] text-slate-400 truncate">industry@samadhanx.gov.in</p>
              </button>
            </div>
          </div>
        </div>

        <div className="text-center mt-6 text-[11px] text-slate-500">
          SamadhanX National GovTech Infrastructure • SIH Problem Statement 26043
        </div>
      </div>
    </div>
  );
};
