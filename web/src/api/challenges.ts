import { supabase } from './supabase';
import { Challenge, Domain, TimelineEvent, ChallengeAttachment, ChallengeStatus } from '../types';

export interface ChallengeFilterParams {
  domain?: string;
  status?: string;
  severity?: string;
  urgency?: string;
  search?: string;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'ASC' | 'DESC';
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

const mapDbChallengeToUi = (db: any): Challenge => {
  const attachments: ChallengeAttachment[] = (db.challenge_media || []).map((m: any) => ({
    id: m.id,
    fileUrl: m.media_url,
    fileName: m.file_name || 'Media attachment',
    mediaType: m.media_type || 'IMAGE',
  }));

  const timeline: TimelineEvent[] = (db.challenge_timeline || []).map((t: any) => ({
    id: t.id,
    fromStatus: t.metadata?.old_status || 'SUBMITTED',
    toStatus: t.status,
    action: t.title,
    comments: t.description,
    actorEmail: t.profiles?.email,
    actorRole: 'OFFICIAL',
    createdAt: t.created_at,
  }));

  return {
    id: db.id,
    trackingNumber: db.tracking_number,
    title: db.title,
    description: db.description,
    domainId: db.domain_id,
    domainCode: db.domains?.code || 'WATER_SANITATION',
    domainName: db.domains?.name || 'Water & Sanitation',
    status: (db.status || 'SUBMITTED') as ChallengeStatus,
    severity: db.severity || 'MEDIUM',
    urgency: db.urgency || 'MEDIUM',
    priorityScore: db.severity === 'CRITICAL' ? 95 : db.severity === 'HIGH' ? 80 : 50,
    aiCategoryConfidence: 0.94,
    duplicateConfidence: 0.05,
    isDuplicateFlagged: false,
    aiReasoning: db.ai_summary || undefined,
    departmentCode: db.organizations?.code,
    departmentName: db.organizations?.name,
    latitude: db.latitude || 28.6139,
    longitude: db.longitude || 77.2090,
    addressText: db.address || db.location,
    district: db.district || 'Central',
    state: db.state || 'Delhi',
    citizenName: db.profiles ? `${db.profiles.first_name} ${db.profiles.last_name}`.trim() : 'Citizen',
    citizenEmail: db.profiles?.email,
    endorsementsCount: db.endorsement_count || 0,
    attachments,
    timeline,
    createdAt: db.created_at,
    updatedAt: db.updated_at,
  };
};

export const challengesApi = {
  getChallenges: async (params?: ChallengeFilterParams): Promise<PageResponse<Challenge>> => {
    let query = supabase
      .from('challenges')
      .select('*, domains(*), organizations(*), profiles!submitter_id(*), challenge_media(*), challenge_timeline(*, profiles(email, first_name, last_name))', { count: 'exact' });

    if (params?.domain) {
      query = query.eq('domain_id', params.domain);
    }
    if (params?.status) {
      query = query.eq('status', params.status);
    }
    if (params?.severity) {
      query = query.eq('severity', params.severity);
    }
    if (params?.urgency) {
      query = query.eq('urgency', params.urgency);
    }
    if (params?.search) {
      query = query.or(`title.ilike.%${params.search}%,description.ilike.%${params.search}%,tracking_number.ilike.%${params.search}%`);
    }

    const page = params?.page || 0;
    const size = params?.size || 20;
    const from = page * size;
    const to = from + size - 1;

    query = query.order('created_at', { ascending: params?.sortDirection === 'ASC' }).range(from, to);

    const { data, count, error } = await query;
    if (error) throw error;

    const items = (data || []).map(mapDbChallengeToUi);
    const total = count || items.length;

    return {
      content: items,
      totalElements: total,
      totalPages: Math.ceil(total / size) || 1,
      size,
      number: page,
    };
  },

  getChallengeById: async (id: string): Promise<Challenge> => {
    const { data, error } = await supabase
      .from('challenges')
      .select('*, domains(*), organizations(*), profiles!submitter_id(*), challenge_media(*), challenge_timeline(*, profiles(email, first_name, last_name))')
      .eq('id', id)
      .single();

    if (error || !data) throw error || new Error('Challenge not found');
    return mapDbChallengeToUi(data);
  },

  getChallengeTimeline: async (id: string): Promise<TimelineEvent[]> => {
    const { data, error } = await supabase
      .from('challenge_timeline')
      .select('*, profiles(email, first_name, last_name)')
      .eq('challenge_id', id)
      .order('created_at', { ascending: true });

    if (error) throw error;

    return (data || []).map((t: any) => ({
      id: t.id,
      fromStatus: t.metadata?.old_status || 'SUBMITTED',
      toStatus: t.status,
      action: t.title,
      comments: t.description,
      actorEmail: t.profiles?.email,
      actorRole: 'OFFICIAL',
      createdAt: t.created_at,
    }));
  },

  getDomains: async (): Promise<Domain[]> => {
    const { data, error } = await supabase
      .from('domains')
      .select('*')
      .eq('is_active', true)
      .order('name', { ascending: true });

    if (error) throw error;

    return (data || []).map((d: any) => ({
      id: d.id,
      code: d.code,
      name: d.name,
      description: d.description || '',
      active: d.is_active,
    }));
  },

  createChallenge: async (challengeData: {
    title: string;
    description: string;
    domainId: string;
    location: string;
    latitude?: number;
    longitude?: number;
    address?: string;
    district?: string;
    state?: string;
    severity?: string;
    urgency?: string;
    affectedPopulation?: string;
    problemCategory?: string;
    mediaUrls?: string[];
  }): Promise<Challenge> => {
    const { data, error } = await supabase.rpc('create_challenge', {
      p_title: challengeData.title,
      p_description: challengeData.description,
      p_domain_id: challengeData.domainId,
      p_location: challengeData.location,
      p_latitude: challengeData.latitude,
      p_longitude: challengeData.longitude,
      p_address: challengeData.address,
      p_district: challengeData.district,
      p_state: challengeData.state,
      p_severity: challengeData.severity || 'MEDIUM',
      p_urgency: challengeData.urgency || 'MEDIUM',
      p_affected_population: challengeData.affectedPopulation,
      p_problem_category: challengeData.problemCategory,
      p_media_urls: challengeData.mediaUrls || [],
    });

    if (error) throw error;
    return challengesApi.getChallengeById(data.id);
  },

  triageChallenge: async (id: string, decision: string, comments: string): Promise<Challenge> => {
    const nextStatus = decision === 'ESCALATE' ? 'IN_COLLABORATION' : decision === 'REJECT' ? 'REJECTED' : 'UNDER_REVIEW';
    const { error } = await supabase.rpc('change_challenge_status', {
      p_challenge_id: id,
      p_new_status: nextStatus,
      p_title: `Triage Action: ${decision}`,
      p_description: comments,
    });

    if (error) throw error;
    return challengesApi.getChallengeById(id);
  },

  resolveDepartmental: async (id: string, summary: string, _evidenceUrls: string[] = []): Promise<Challenge> => {
    const { error } = await supabase.rpc('change_challenge_status', {
      p_challenge_id: id,
      p_new_status: 'RESOLVED',
      p_title: 'Resolved by Department',
      p_description: summary,
    });

    if (error) throw error;
    return challengesApi.getChallengeById(id);
  },

  escalateToInnovation: async (id: string, reason: string, _targetDomain?: string): Promise<Challenge> => {
    const { error } = await supabase.rpc('change_challenge_status', {
      p_challenge_id: id,
      p_new_status: 'IN_COLLABORATION',
      p_title: 'Escalated to Innovation & Collaboration Ecosystem',
      p_description: reason,
    });

    if (error) throw error;
    return challengesApi.getChallengeById(id);
  },

  endorseChallenge: async (id: string): Promise<void> => {
    const { error } = await supabase.rpc('endorse_challenge', { p_challenge_id: id });
    if (error) throw error;
  },
};
