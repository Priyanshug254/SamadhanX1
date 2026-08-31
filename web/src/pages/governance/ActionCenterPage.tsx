import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { governanceApi, WorkItem, ApprovalRequest, RoleQueueSummary } from '../../api/governance';
import { StatCard } from '../../components/common/StatCard';
import {
  CheckSquare,
  Clock,
  AlertTriangle,
  FileCheck2,
  CheckCircle2,
  XCircle,
  ExternalLink,
  RefreshCw,
  Search,
  Filter,
  Layers,
  ArrowRight,
} from 'lucide-react';

export const ActionCenterPage: React.FC = () => {
  const [summary, setSummary] = useState<RoleQueueSummary | null>(null);
  const [myTasks, setMyTasks] = useState<WorkItem[]>([]);
  const [pendingApprovals, setPendingApprovals] = useState<ApprovalRequest[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [activeTab, setActiveTab] = useState<'tasks' | 'approvals' | 'overdue'>('tasks');
  const [search, setSearch] = useState<string>('');

  // Review Modal State
  const [selectedApproval, setSelectedApproval] = useState<ApprovalRequest | null>(null);
  const [reviewComments, setReviewComments] = useState<string>('');
  const [submittingReview, setSubmittingReview] = useState<boolean>(false);

  const loadData = async () => {
    setLoading(true);
    try {
      const [sum, tasks, approvals] = await Promise.all([
        governanceApi.getQueueSummary().catch(() => null),
        governanceApi.getMyTasks().catch(() => []),
        governanceApi.getPendingApprovals().catch(() => []),
      ]);
      setSummary(sum);
      setMyTasks(tasks);
      setPendingApprovals(approvals);
    } catch (err) {
      console.error('Failed to load action center data', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleTaskStatusChange = async (taskId: string, newStatus: WorkItem['status']) => {
    try {
      await governanceApi.updateWorkItem(taskId, newStatus);
      await loadData();
    } catch (err) {
      alert('Failed to update task status');
    }
  };

  const handleApprovalDecision = async (decision: 'APPROVED' | 'REJECTED' | 'CHANGES_REQUESTED') => {
    if (!selectedApproval) return;
    setSubmittingReview(true);
    try {
      await governanceApi.reviewApproval(selectedApproval.id, decision, reviewComments);
      setSelectedApproval(null);
      setReviewComments('');
      await loadData();
    } catch (err) {
      alert('Failed to record approval decision');
    } finally {
      setSubmittingReview(false);
    }
  };

  const filteredTasks = myTasks.filter((t) =>
    t.title.toLowerCase().includes(search.toLowerCase()) ||
    (t.challengeTrackingNumber && t.challengeTrackingNumber.toLowerCase().includes(search.toLowerCase()))
  );

  const overdueTasks = myTasks.filter((t) => t.overdue);

  return (
    <div className="space-y-6">
      {/* Top Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-slate-900 tracking-tight flex items-center gap-2">
            <CheckSquare className="w-6 h-6 text-amber-500" />
            <span>Institutional Governance & Action Center</span>
          </h1>
          <p className="text-sm text-slate-500">
            Role-aware work queues, pending multi-party approvals, task tracking, and cross-departmental accountability
          </p>
        </div>

        <button
          onClick={loadData}
          className="px-3.5 py-2 rounded-xl bg-white border border-slate-200 text-slate-700 text-xs font-bold hover:bg-slate-50 transition-colors flex items-center gap-2 shadow-sm"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
          <span>Refresh Queue</span>
        </button>
      </div>

      {/* KPI Stats */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          title="My Active Tasks"
          value={summary?.myActiveTasksCount || myTasks.length}
          subtitle="Assigned to your profile"
          icon={CheckSquare}
          color="navy"
        />
        <StatCard
          title="Pending Approvals"
          value={summary?.pendingApprovalsCount || pendingApprovals.length}
          subtitle="Awaiting administrative review"
          icon={FileCheck2}
          color="saffron"
        />
        <StatCard
          title="Overdue Work Items"
          value={summary?.overdueWorkItemsCount || overdueTasks.length}
          subtitle="Requires immediate action"
          icon={Clock}
          color="rose"
        />
        <StatCard
          title="Critical Queue Items"
          value={summary?.criticalActionItemsCount || 2}
          subtitle="High priority SLA"
          icon={AlertTriangle}
          color="purple"
        />
      </div>

      {/* Tabs */}
      <div className="flex border-b border-slate-200 gap-6 text-sm font-bold text-slate-500">
        <button
          onClick={() => setActiveTab('tasks')}
          className={`pb-3 transition-colors flex items-center gap-2 ${
            activeTab === 'tasks' ? 'border-b-2 border-amber-500 text-slate-900' : 'hover:text-slate-900'
          }`}
        >
          <span>My Tasks & Assignments</span>
          <span className="text-xs px-2 py-0.5 rounded-full bg-slate-100 text-slate-700">{myTasks.length}</span>
        </button>

        <button
          onClick={() => setActiveTab('approvals')}
          className={`pb-3 transition-colors flex items-center gap-2 ${
            activeTab === 'approvals' ? 'border-b-2 border-amber-500 text-slate-900' : 'hover:text-slate-900'
          }`}
        >
          <span>Pending Approvals</span>
          <span className="text-xs px-2 py-0.5 rounded-full bg-amber-100 text-amber-900">{pendingApprovals.length}</span>
        </button>

        <button
          onClick={() => setActiveTab('overdue')}
          className={`pb-3 transition-colors flex items-center gap-2 ${
            activeTab === 'overdue' ? 'border-b-2 border-amber-500 text-slate-900' : 'hover:text-slate-900'
          }`}
        >
          <span>Overdue Escalations</span>
          <span className="text-xs px-2 py-0.5 rounded-full bg-rose-100 text-rose-900">{overdueTasks.length}</span>
        </button>
      </div>

      {/* Search Bar */}
      <div className="relative">
        <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-3" />
        <input
          type="text"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Search work items by title, tracking reference, or domain..."
          className="w-full pl-10 pr-4 py-2 rounded-xl bg-white border border-slate-200 text-xs placeholder-slate-400 focus:outline-none focus:border-amber-500"
        />
      </div>

      {/* Tab: Tasks */}
      {activeTab === 'tasks' && (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {filteredTasks.length === 0 ? (
            <div className="col-span-full bg-white p-12 text-center rounded-2xl border border-slate-200 text-slate-400 text-xs">
              No tasks currently assigned. You are all caught up!
            </div>
          ) : (
            filteredTasks.map((task) => (
              <div
                key={task.id}
                className="bg-white p-5 rounded-2xl shadow-sm border border-slate-200/80 hover:shadow-md transition-shadow flex flex-col justify-between"
              >
                <div>
                  <div className="flex items-center justify-between gap-2 mb-2">
                    <span className="text-[10px] font-extrabold px-2 py-0.5 rounded bg-slate-100 text-slate-700">
                      {task.itemType}
                    </span>
                    <span
                      className={`text-[10px] font-bold px-2 py-0.5 rounded ${
                        task.priority === 'CRITICAL'
                          ? 'bg-rose-100 text-rose-800'
                          : task.priority === 'HIGH'
                          ? 'bg-amber-100 text-amber-800'
                          : 'bg-slate-100 text-slate-700'
                      }`}
                    >
                      {task.priority} PRIORITY
                    </span>
                  </div>

                  <h3 className="text-sm font-bold text-slate-900">{task.title}</h3>
                  {task.description && <p className="text-xs text-slate-500 mt-1">{task.description}</p>}

                  {task.challengeTrackingNumber && (
                    <div className="mt-3 text-xs text-slate-600 flex items-center gap-1">
                      <span>Related Challenge:</span>
                      <Link
                        to={`/government/challenges/${task.challengeId}`}
                        className="font-mono font-bold text-amber-600 hover:text-amber-700 inline-flex items-center gap-1"
                      >
                        <span>{task.challengeTrackingNumber}</span>
                        <ExternalLink className="w-3 h-3" />
                      </Link>
                    </div>
                  )}
                </div>

                <div className="mt-4 pt-3 border-t border-slate-100 flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <select
                      value={task.status}
                      onChange={(e) => handleTaskStatusChange(task.id, e.target.value as any)}
                      className="px-2.5 py-1 rounded-lg border border-slate-200 text-xs font-bold text-slate-700"
                    >
                      <option value="TODO">TODO</option>
                      <option value="IN_PROGRESS">IN PROGRESS</option>
                      <option value="BLOCKED">BLOCKED</option>
                      <option value="COMPLETED">COMPLETED</option>
                    </select>
                  </div>

                  {task.dueDate && (
                    <span className={`text-[11px] font-medium ${task.overdue ? 'text-rose-600 font-bold' : 'text-slate-400'}`}>
                      Due: {new Date(task.dueDate).toLocaleDateString()}
                    </span>
                  )}
                </div>
              </div>
            ))
          )}
        </div>
      )}

      {/* Tab: Approvals */}
      {activeTab === 'approvals' && (
        <div className="space-y-4">
          {pendingApprovals.length === 0 ? (
            <div className="bg-white p-12 text-center rounded-2xl border border-slate-200 text-slate-400 text-xs">
              No pending approval requests requiring administrative review.
            </div>
          ) : (
            pendingApprovals.map((req) => (
              <div
                key={req.id}
                className="bg-white p-5 rounded-2xl shadow-sm border border-slate-200/80 flex flex-col md:flex-row md:items-center justify-between gap-4"
              >
                <div>
                  <div className="flex items-center gap-2 mb-1">
                    <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-amber-100 text-amber-900 border border-amber-200">
                      {req.workflowType}
                    </span>
                    {req.targetReferenceCode && (
                      <span className="font-mono text-xs font-bold text-slate-800">
                        {req.targetReferenceCode}
                      </span>
                    )}
                  </div>
                  <p className="text-xs text-slate-700 mt-1">
                    Requested by: <strong>{req.requestedByName || 'Official'}</strong> on{' '}
                    {new Date(req.createdAt).toLocaleDateString()}
                  </p>
                  {req.justification && (
                    <p className="text-xs text-slate-500 mt-1 italic bg-slate-50 p-2 rounded-lg">
                      "{req.justification}"
                    </p>
                  )}
                </div>

                <div className="flex items-center gap-2 shrink-0">
                  <button
                    onClick={() => setSelectedApproval(req)}
                    className="px-4 py-2 rounded-xl bg-slate-900 text-white text-xs font-bold hover:bg-amber-500 hover:text-slate-950 transition-colors"
                  >
                    Review & Decide →
                  </button>
                </div>
              </div>
            ))
          )}
        </div>
      )}

      {/* Tab: Overdue */}
      {activeTab === 'overdue' && (
        <div className="space-y-3">
          {overdueTasks.length === 0 ? (
            <div className="bg-white p-12 text-center rounded-2xl border border-slate-200 text-slate-400 text-xs">
              No overdue items! All SLA metrics are currently on track.
            </div>
          ) : (
            overdueTasks.map((item) => (
              <div key={item.id} className="p-4 rounded-2xl bg-rose-50 border border-rose-200 flex items-center justify-between text-xs">
                <div className="flex items-center gap-3">
                  <AlertTriangle className="w-5 h-5 text-rose-600 shrink-0" />
                  <div>
                    <span className="font-bold text-rose-900">{item.title}</span>
                    <p className="text-rose-700 text-[11px]">Due date was {item.dueDate ? new Date(item.dueDate).toLocaleDateString() : 'earlier'}</p>
                  </div>
                </div>
                <button
                  onClick={() => handleTaskStatusChange(item.id, 'COMPLETED')}
                  className="px-3 py-1.5 rounded-lg bg-rose-600 text-white font-bold hover:bg-rose-700 transition-colors"
                >
                  Mark Resolved
                </button>
              </div>
            ))
          )}
        </div>
      )}

      {/* Modal: Review Approval Request */}
      {selectedApproval && (
        <div className="fixed inset-0 bg-slate-950/60 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl p-6 sm:p-8 max-w-lg w-full shadow-2xl border border-slate-200">
            <h3 className="text-lg font-black text-slate-900 tracking-tight mb-2">
              Official Workflow Governance Review
            </h3>
            <p className="text-xs text-slate-500 mb-4">
              Action: <strong>{selectedApproval.workflowType}</strong> ({selectedApproval.targetReferenceCode})
            </p>

            <div className="space-y-3 mb-6 text-xs">
              <label className="block font-bold text-slate-700">Official Decision Comments / Audit Rationale</label>
              <textarea
                value={reviewComments}
                onChange={(e) => setReviewComments(e.target.value)}
                placeholder="State administrative justification for approval, rejection, or required modifications..."
                rows={4}
                className="w-full p-3 rounded-xl border border-slate-200 text-xs focus:outline-none focus:border-amber-500"
              />
            </div>

            <div className="flex items-center justify-between pt-4 border-t border-slate-100">
              <button
                type="button"
                onClick={() => setSelectedApproval(null)}
                className="px-4 py-2 text-slate-500 text-xs font-bold hover:bg-slate-100 rounded-xl"
              >
                Cancel
              </button>

              <div className="flex items-center gap-2">
                <button
                  type="button"
                  disabled={submittingReview}
                  onClick={() => handleApprovalDecision('REJECTED')}
                  className="px-3.5 py-2 rounded-xl bg-rose-600 text-white text-xs font-bold hover:bg-rose-700 transition-colors disabled:opacity-50"
                >
                  Reject
                </button>
                <button
                  type="button"
                  disabled={submittingReview}
                  onClick={() => handleApprovalDecision('APPROVED')}
                  className="px-4 py-2 rounded-xl bg-emerald-600 text-white text-xs font-bold hover:bg-emerald-700 transition-colors disabled:opacity-50"
                >
                  Approve Sign-Off
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
