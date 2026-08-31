import { supabase } from './supabase';

export interface NotificationItem {
  id: string;
  title: string;
  body: string;
  notificationType: string;
  referenceId?: string;
  referenceType?: string;
  read: boolean;
  createdAt: string;
}

export interface ActivityFeedItem {
  id: string;
  eventType: string;
  title: string;
  summary: string;
  referenceCode?: string;
  referenceType?: string;
  actorName?: string;
  timestamp: string;
}

export const notificationsApi = {
  getUserNotifications: async (page = 0, size = 20): Promise<{ content: NotificationItem[]; totalElements: number }> => {
    const { data: { user } } = await supabase.auth.getUser();
    if (!user) return { content: [], totalElements: 0 };

    const from = page * size;
    const to = from + size - 1;

    const { data, count, error } = await supabase
      .from('notifications')
      .select('*', { count: 'exact' })
      .eq('user_id', user.id)
      .order('created_at', { ascending: false })
      .range(from, to);

    if (error || !data) return { content: [], totalElements: 0 };

    const items: NotificationItem[] = data.map((n: any) => ({
      id: n.id,
      title: n.title,
      body: n.message,
      notificationType: n.type || 'SYSTEM',
      referenceId: n.reference_id,
      referenceType: n.reference_type,
      read: n.is_read,
      createdAt: n.created_at,
    }));

    return {
      content: items,
      totalElements: count || items.length,
    };
  },

  getUnreadCount: async (): Promise<number> => {
    const { data: { user } } = await supabase.auth.getUser();
    if (!user) return 0;

    const { count } = await supabase
      .from('notifications')
      .select('*', { count: 'exact', head: true })
      .eq('user_id', user.id)
      .eq('is_read', false);

    return count || 0;
  },

  markAsRead: async (id: string): Promise<void> => {
    await supabase
      .from('notifications')
      .update({ is_read: true })
      .eq('id', id);
  },

  markAllAsRead: async (): Promise<void> => {
    const { data: { user } } = await supabase.auth.getUser();
    if (!user) return;

    await supabase
      .from('notifications')
      .update({ is_read: true })
      .eq('user_id', user.id);
  },

  getActivityFeed: async (limit = 15): Promise<ActivityFeedItem[]> => {
    const { data: timeline, error } = await supabase
      .from('challenge_timeline')
      .select('*, challenges(title, tracking_number), profiles(first_name, last_name, email)')
      .order('created_at', { ascending: false })
      .limit(limit);

    if (error || !timeline) return [];

    return timeline.map((t: any) => ({
      id: t.id,
      eventType: t.status,
      title: t.title,
      summary: t.description || `Status moved to ${t.status}`,
      referenceCode: t.challenges?.tracking_number,
      referenceType: 'CHALLENGE',
      actorName: t.profiles ? `${t.profiles.first_name} ${t.profiles.last_name}`.trim() || t.profiles.email : 'System',
      timestamp: t.created_at,
    }));
  },
};
