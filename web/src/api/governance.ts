import { supabase } from './supabase';

export interface WorkItem {
  id: string;
  title: string;
  description?: string;
  itemType: string;
  status: 'TODO' | 'IN_PROGRESS' | 'BLOCKED' | 'COMPLETED' | 'CANCELLED';
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  assignedToUserId?: string;
  assignedToName?: string;
  assignedToEmail?: string;
  challengeId?: string;
  challengeTrackingNumber?: string;
  proposalId?: string;
  proposalTrackingNumber?: string;
  dueDate?: string;
  completedAt?: string;
  resolutionNotes?: string;
  overdue: boolean;
  createdAt: string;
}

export interface ApprovalRequest {
  id: string;
  workflowType: string;
  targetEntityId: string;
  targetReferenceCode?: string;
  requestedByUserId: string;
  requestedByName: string;
  reviewedByUserId?: string;
  reviewedByName?: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'CHANGES_REQUESTED';
  justification?: string;
  reviewComments?: string;
  previousState?: string;
  targetState?: string;
  reviewedAt?: string;
  createdAt: string;
}

export interface RoleQueueSummary {
  userRole: string;
  myActiveTasksCount: number;
  pendingApprovalsCount: number;
  overdueWorkItemsCount: number;
  criticalActionItemsCount: number;
  highPriorityWorkItems: WorkItem[];
  pendingApprovals: ApprovalRequest[];
}

export interface UnifiedLifecycleStage {
  stageKey: string;
  stageLabel: string;
  status: string;
  timestamp?: string;
  actorRole: string;
  summary: string;
}

export interface UnifiedLifecycleItem {
  id: string;
  stage: string;
  action: string;
  fromState?: string;
  toState?: string;
  actorName: string;
  actorRole: string;
  details?: string;
  timestamp: string;
  isOfficialAction: boolean;
}

export interface UnifiedLifecycleTimeline {
  challengeId: string;
  challengeTrackingNumber: string;
  challengeTitle: string;
  currentStatus: string;
  resolutionPath: string;
  domainName?: string;
  assignedDepartment?: string;
  stages: UnifiedLifecycleStage[];
  auditStream: UnifiedLifecycleItem[];
}

export const governanceApi = {
  getMyTasks: async (): Promise<WorkItem[]> => {
    const { data: challenges, error } = await supabase
      .from('challenges')
      .select('id, tracking_number, title, description, severity, status, created_at')
      .in('status', ['SUBMITTED', 'UNDER_REVIEW', 'ROUTED', 'IN_COLLABORATION'])
      .order('created_at', { ascending: false })
      .limit(20);

    if (error) return [];

    return (challenges || []).map((c: any) => ({
      id: c.id,
      title: `Review Challenge: ${c.title}`,
      description: c.description,
      itemType: 'CHALLENGE_TRIAGE',
      status: c.status === 'UNDER_REVIEW' ? 'IN_PROGRESS' : 'TODO',
      priority: c.severity || 'MEDIUM',
      challengeId: c.id,
      challengeTrackingNumber: c.tracking_number,
      dueDate: new Date(Date.now() + 86400000 * 3).toISOString(),
      overdue: false,
      createdAt: c.created_at,
    }));
  },

  getQueueSummary: async (): Promise<RoleQueueSummary> => {
    const { data: { user } } = await supabase.auth.getUser();
    const tasks = await governanceApi.getMyTasks();
    const approvals = await governanceApi.getPendingApprovals();

    return {
      userRole: user?.user_metadata?.role || 'GOVERNMENT_OFFICIAL',
      myActiveTasksCount: tasks.length,
      pendingApprovalsCount: approvals.length,
      overdueWorkItemsCount: 0,
      criticalActionItemsCount: tasks.filter(t => t.priority === 'CRITICAL').length,
      highPriorityWorkItems: tasks.filter(t => t.priority === 'HIGH' || t.priority === 'CRITICAL'),
      pendingApprovals: approvals,
    };
  },

  updateWorkItem: async (
    id: string,
    status: WorkItem['status'],
    resolutionNotes?: string
  ): Promise<WorkItem> => {
    if (status === 'COMPLETED') {
      await supabase.rpc('change_challenge_status', {
        p_challenge_id: id,
        p_new_status: 'IN_COLLABORATION',
        p_title: 'Work item completed by department',
        p_description: resolutionNotes || 'Assigned to active collaboration',
      });
    }

    return {
      id,
      title: 'Updated Work Item',
      itemType: 'CHALLENGE_TRIAGE',
      status,
      priority: 'MEDIUM',
      resolutionNotes,
      overdue: false,
      createdAt: new Date().toISOString(),
    };
  },

  getPendingApprovals: async (): Promise<ApprovalRequest[]> => {
    const { data: orgs, error } = await supabase
      .from('organizations')
      .select('id, name, type, registration_number, created_at, created_by, profiles!created_by(email, first_name, last_name)')
      .eq('verification_status', 'PENDING_VERIFICATION')
      .order('created_at', { ascending: false });

    if (error || !orgs) return [];

    return orgs.map((org: any) => ({
      id: org.id,
      workflowType: 'ORGANIZATION_VERIFICATION',
      targetEntityId: org.id,
      targetReferenceCode: org.registration_number || org.name,
      requestedByUserId: org.created_by || '',
      requestedByName: org.profiles ? `${org.profiles.first_name} ${org.profiles.last_name}`.trim() : 'Org Representative',
      status: 'PENDING',
      justification: `Organization registration for ${org.name} (${org.type})`,
      createdAt: org.created_at,
    }));
  },

  reviewApproval: async (
    id: string,
    decision: 'APPROVED' | 'REJECTED' | 'CHANGES_REQUESTED',
    reviewComments?: string
  ): Promise<ApprovalRequest> => {
    const targetStatus = decision === 'APPROVED' ? 'VERIFIED' : 'REJECTED';
    await supabase.rpc('verify_organization', {
      p_org_id: id,
      p_decision: targetStatus,
      p_reason: reviewComments || 'Reviewed and updated by administrator',
    });

    return {
      id,
      workflowType: 'ORGANIZATION_VERIFICATION',
      targetEntityId: id,
      requestedByUserId: '',
      requestedByName: 'Organization Representative',
      status: decision === 'APPROVED' ? 'APPROVED' : 'REJECTED',
      reviewComments,
      reviewedAt: new Date().toISOString(),
      createdAt: new Date().toISOString(),
    };
  },

  getUnifiedLifecycle: async (challengeId: string): Promise<UnifiedLifecycleTimeline> => {
    const { data: challenge } = await supabase
      .from('challenges')
      .select('*, domains(name), organizations(name), challenge_timeline(*, profiles(email, first_name, last_name))')
      .eq('id', challengeId)
      .single();

    if (!challenge) throw new Error('Challenge not found');

    const stages: UnifiedLifecycleStage[] = [
      { stageKey: 'REPORTED', stageLabel: 'Citizen Report', status: 'COMPLETED', timestamp: challenge.created_at, actorRole: 'CITIZEN', summary: 'Challenge submitted & tracking number generated' },
      { stageKey: 'TRIAGE', stageLabel: 'Department Triage', status: challenge.status !== 'SUBMITTED' ? 'COMPLETED' : 'IN_PROGRESS', actorRole: 'GOVERNMENT', summary: 'Department evaluation and verification' },
      { stageKey: 'COLLABORATION', stageLabel: 'Innovation & Co-Design', status: ['IN_COLLABORATION', 'SOLUTION_PROPOSED', 'UNDER_EVALUATION', 'PROTOTYPE', 'PILOT', 'IMPLEMENTATION', 'RESOLVED'].includes(challenge.status) ? 'COMPLETED' : 'PENDING', actorRole: 'ECOSYSTEM', summary: 'University and Startup solution teams formed' },
      { stageKey: 'PILOT', stageLabel: 'Pilot Deployment', status: ['PILOT', 'IMPLEMENTATION', 'RESOLVED'].includes(challenge.status) ? 'COMPLETED' : 'PENDING', actorRole: 'PARTNERS', summary: 'Field pilot and impact metrics tracking' },
      { stageKey: 'RESOLVED', stageLabel: 'Impact & Resolution', status: ['RESOLVED', 'CLOSED'].includes(challenge.status) ? 'COMPLETED' : 'PENDING', actorRole: 'GOVERNMENT', summary: 'Societal challenge resolved with verified impact' },
    ];

    const auditStream: UnifiedLifecycleItem[] = (challenge.challenge_timeline || []).map((t: any) => ({
      id: t.id,
      stage: t.status,
      action: t.title,
      fromState: t.metadata?.old_status,
      toState: t.status,
      actorName: t.profiles ? `${t.profiles.first_name} ${t.profiles.last_name}`.trim() || t.profiles.email : 'System',
      actorRole: 'OFFICIAL',
      details: t.description,
      timestamp: t.created_at,
      isOfficialAction: true,
    }));

    return {
      challengeId: challenge.id,
      challengeTrackingNumber: challenge.tracking_number,
      challengeTitle: challenge.title,
      currentStatus: challenge.status,
      resolutionPath: 'COLLABORATIVE_INNOVATION',
      domainName: challenge.domains?.name,
      assignedDepartment: challenge.organizations?.name,
      stages,
      auditStream,
    };
  },
};
