import { supabase } from './supabase';
import { PageResponse } from './challenges';
import { ProjectTeam, Proposal, ProposalStatus } from '../types';

export const innovationApi = {
  getProposals: async (params?: { page?: number; size?: number; status?: ProposalStatus; challengeId?: string }): Promise<PageResponse<Proposal>> => {
    let query = supabase
      .from('solution_proposals')
      .select('*, challenges(title), teams(name), profiles!proposer_id(first_name, last_name, email)', { count: 'exact' });

    if (params?.status) {
      query = query.eq('status', params.status);
    }
    if (params?.challengeId) {
      query = query.eq('challenge_id', params.challengeId);
    }

    const page = params?.page || 0;
    const size = params?.size || 20;
    const from = page * size;
    const to = from + size - 1;

    query = query.order('created_at', { ascending: false }).range(from, to);

    const { data, count, error } = await query;
    if (error) return { content: [], totalElements: 0, totalPages: 0, size, number: page };

    const items: Proposal[] = (data || []).map((p: any) => ({
      id: p.id,
      trackingNumber: `PROP-${p.id.substring(0, 8).toUpperCase()}`,
      challengeId: p.challenge_id,
      challengeTitle: p.challenges?.title,
      teamId: p.team_id || p.id,
      teamName: p.teams?.name || 'Innovation Solution Team',
      title: p.title,
      executiveSummary: p.problem_understanding,
      technicalApproach: p.proposed_solution,
      trlLevel: p.score ? Math.min(9, Math.max(1, Math.round(p.score))) : 4,
      budgetRequired: Number(p.estimated_budget) || 150000,
      timelineWeeks: p.timeline_weeks || 12,
      status: (p.status || 'SUBMITTED') as ProposalStatus,
      averageEvaluationScore: p.score ? Number(p.score) : undefined,
      createdAt: p.created_at,
    }));

    const total = count || items.length;

    return {
      content: items,
      totalElements: total,
      totalPages: Math.ceil(total / size) || 1,
      size,
      number: page,
    };
  },

  getProposalById: async (id: string): Promise<Proposal> => {
    const { data: p, error } = await supabase
      .from('solution_proposals')
      .select('*, challenges(title), teams(name), profiles!proposer_id(first_name, last_name, email)')
      .eq('id', id)
      .single();

    if (error || !p) throw error || new Error('Proposal not found');

    return {
      id: p.id,
      trackingNumber: `PROP-${p.id.substring(0, 8).toUpperCase()}`,
      challengeId: p.challenge_id,
      challengeTitle: p.challenges?.title,
      teamId: p.team_id || p.id,
      teamName: p.teams?.name || 'Innovation Solution Team',
      title: p.title,
      executiveSummary: p.problem_understanding,
      technicalApproach: p.proposed_solution,
      trlLevel: p.score ? Math.min(9, Math.max(1, Math.round(p.score))) : 4,
      budgetRequired: Number(p.estimated_budget) || 150000,
      timelineWeeks: p.timeline_weeks || 12,
      status: (p.status || 'SUBMITTED') as ProposalStatus,
      averageEvaluationScore: p.score ? Number(p.score) : undefined,
      createdAt: p.created_at,
    };
  },

  createProposal: async (data: {
    challengeId: string;
    teamId: string;
    title: string;
    executiveSummary: string;
    technicalApproach: string;
    trlLevel: number;
    budgetRequired: number;
    timelineWeeks: number;
  }): Promise<Proposal> => {
    const { data: { user } } = await supabase.auth.getUser();
    if (!user) throw new Error('User must be logged in');

    const { data: inserted, error } = await supabase
      .from('solution_proposals')
      .insert({
        challenge_id: data.challengeId,
        team_id: data.teamId || null,
        proposer_id: user.id,
        title: data.title,
        problem_understanding: data.executiveSummary,
        proposed_solution: data.technicalApproach,
        estimated_budget: data.budgetRequired,
        timeline_weeks: data.timelineWeeks,
        status: 'SUBMITTED',
        score: data.trlLevel,
      })
      .select()
      .single();

    if (error) throw error;

    return innovationApi.getProposalById(inserted.id);
  },

  updateProposalStatus: async (id: string, status: ProposalStatus, comments?: string): Promise<Proposal> => {
    const { error } = await supabase
      .from('solution_proposals')
      .update({
        status,
        reviewer_feedback: comments,
        updated_at: new Date().toISOString(),
      })
      .eq('id', id);

    if (error) throw error;
    return innovationApi.getProposalById(id);
  },

  getTeamsByChallenge: async (challengeId: string): Promise<ProjectTeam[]> => {
    const { data: teams, error } = await supabase
      .from('teams')
      .select('*, profiles!leader_id(first_name, last_name, email), team_members(*, profiles(first_name, last_name, email))')
      .eq('challenge_id', challengeId);

    if (error || !teams) return [];

    return teams.map((t: any) => ({
      id: t.id,
      name: t.name,
      challengeId: t.challenge_id,
      leadUserId: t.leader_id,
      leadName: t.profiles ? `${t.profiles.first_name} ${t.profiles.last_name}`.trim() : 'Team Lead',
      members: (t.team_members || []).map((m: any) => ({
        id: m.id,
        userId: m.user_id,
        fullName: m.profiles ? `${m.profiles.first_name} ${m.profiles.last_name}`.trim() : 'Member',
        email: m.profiles?.email || '',
        roleInTeam: m.role || 'STUDENT_RESEARCHER',
      })),
      createdAt: t.created_at,
    }));
  },

  createTeam: async (name: string, challengeId: string): Promise<ProjectTeam> => {
    const { data: { user } } = await supabase.auth.getUser();
    if (!user) throw new Error('User must be logged in');

    const { data: team, error } = await supabase
      .from('teams')
      .insert({
        name,
        challenge_id: challengeId,
        leader_id: user.id,
      })
      .select()
      .single();

    if (error) throw error;

    // Add leader to team_members
    await supabase.from('team_members').insert({
      team_id: team.id,
      user_id: user.id,
      role: 'LEADER',
    });

    return {
      id: team.id,
      name: team.name,
      challengeId: team.challenge_id,
      leadUserId: user.id,
      leadName: 'Lead',
      members: [],
      createdAt: team.created_at,
    };
  },

  inviteMember: async (teamId: string, _email: string, roleInTeam: string): Promise<void> => {
    const { data: { user } } = await supabase.auth.getUser();
    if (!user) throw new Error('User must be logged in');

    await supabase.from('team_members').insert({
      team_id: teamId,
      user_id: user.id,
      role: roleInTeam,
    });
  },
};
