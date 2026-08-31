export type Role =
  | 'CITIZEN'
  | 'COMMUNITY_ORGANIZATION'
  | 'GOVERNMENT_OFFICIAL'
  | 'GOVERNMENT_ADMIN'
  | 'UNIVERSITY_ADMIN'
  | 'FACULTY'
  | 'STUDENT'
  | 'INDUSTRY'
  | 'STARTUP'
  | 'MSME'
  | 'CSR'
  | 'RESEARCH_LAB'
  | 'INNOVATION_HUB'
  | 'SUPER_ADMIN';

export type ChallengeStatus =
  | 'SUBMITTED'
  | 'AI_CATEGORIZED'
  | 'FLAGGED_DUPLICATE'
  | 'ASSIGNED_TO_DEPARTMENT'
  | 'UNDER_DEPARTMENT_TRIAGE'
  | 'IN_PROGRESS_DEPARTMENTAL'
  | 'RESOLVED_DEPARTMENTAL'
  | 'INNOVATION_REQUIRED'
  | 'INNOVATION_CHALLENGE_ACTIVE'
  | 'SOLUTIONS_SHORTLISTED'
  | 'PROTOTYPE_DEPLOYED'
  | 'RESOLVED_INNOVATION'
  | 'REJECTED';

export type ProposalStatus =
  | 'DRAFT'
  | 'SUBMITTED'
  | 'UNDER_REVIEW'
  | 'SHORTLISTED'
  | 'PROTOTYPING'
  | 'PILOT_READY'
  | 'REJECTED'
  | 'AWARDED';

export interface User {
  id: string;
  email: string;
  fullName: string;
  phoneNumber?: string;
  role: Role;
  organizationId?: string;
  organizationName?: string;
  active: boolean;
  avatarUrl?: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresInMs: number;
  user: User;
}

export interface Domain {
  id: string;
  code: string;
  name: string;
  description: string;
  active: boolean;
}

export interface ChallengeAttachment {
  id: string;
  fileUrl: string;
  fileName: string;
  mediaType: 'IMAGE' | 'VIDEO' | 'AUDIO' | 'DOCUMENT';
  fileSizeBytes?: number;
}

export interface TimelineEvent {
  id: string;
  fromStatus: string;
  toStatus: string;
  action: string;
  comments?: string;
  actorEmail?: string;
  actorRole?: string;
  createdAt: string;
}

export interface Challenge {
  id: string;
  trackingNumber: string;
  title: string;
  description: string;
  domainId: string;
  domainCode: string;
  domainName: string;
  status: ChallengeStatus;
  severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  urgency: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  priorityScore: number;
  aiCategoryConfidence?: number;
  duplicateConfidence?: number;
  isDuplicateFlagged: boolean;
  duplicateOfChallengeId?: string;
  aiReasoning?: string;
  aiPriorityReasoning?: string;
  aiDuplicateExplanation?: string;
  aiSolutionRecommendation?: string;
  aiModelProvider?: string;
  departmentCode?: string;
  departmentName?: string;
  latitude: number;
  longitude: number;
  addressText?: string;
  district?: string;
  state?: string;
  pinCode?: string;
  citizenName?: string;
  citizenEmail?: string;
  endorsementsCount: number;
  attachments?: ChallengeAttachment[];
  timeline?: TimelineEvent[];
  createdAt: string;
  updatedAt: string;
}

export interface AiSolutionRecommendation {
  problemSummary: string;
  proposedSolutionApproaches: string[];
  requiredTechnologies: string[];
  suggestedDisciplines: string[];
  implementationRisks: string[];
  expectedImpact: string;
  suggestedTRLStartingPoint: number;
  modelProvider?: string;
  fallbackUsed?: boolean;
}

export interface TeamMember {
  id: string;
  userId: string;
  fullName: string;
  email: string;
  roleInTeam: 'LEAD' | 'FACULTY_MENTOR' | 'STUDENT_RESEARCHER' | 'EXTERNAL_EXPERT';
  discipline?: string;
  institutionName?: string;
}

export interface ProjectTeam {
  id: string;
  name: string;
  challengeId: string;
  leadUserId: string;
  leadName?: string;
  members: TeamMember[];
  createdAt: string;
}

export interface Proposal {
  id: string;
  trackingNumber: string;
  challengeId: string;
  challengeTitle?: string;
  teamId: string;
  teamName: string;
  title: string;
  executiveSummary: string;
  technicalApproach: string;
  trlLevel: number;
  budgetRequired: number;
  timelineWeeks: number;
  status: ProposalStatus;
  averageEvaluationScore?: number;
  createdAt: string;
}

export interface PartnerMatchingFactor {
  factor: string;
  scoreBonus: number;
  description: string;
}

export interface PartnerMatch {
  partnerOrganizationId: string;
  partnerName: string;
  organizationType: string;
  matchScore: number;
  fitQuality: 'EXCELLENT' | 'HIGH' | 'MODERATE' | 'LOW';
  matchingFactors: PartnerMatchingFactor[];
  missingCapabilities: string[];
  mentorshipAvailable: boolean;
  fundingAvailable: boolean;
  pilotTestingAvailable: boolean;
}

export interface PilotDeployment {
  id: string;
  proposalId: string;
  proposalTitle: string;
  location: string;
  status: 'PREPARATION' | 'ACTIVE_DEPLOYMENT' | 'MONITORING' | 'VALIDATED' | 'COMPLETED';
  startDate: string;
  beneficiariesCount: number;
  waterSavedLiters?: number;
  energySavedKwh?: number;
  wasteDivertedKg?: number;
  pilotNotes?: string;
}

export interface DashboardMetrics {
  totalChallenges: number;
  pendingTriage: number;
  highPriority: number;
  resolvedDepartmental: number;
  innovationRequired: number;
  activeProposals: number;
  activePilots: number;
  totalCsrFundsAllocated: number;
}
