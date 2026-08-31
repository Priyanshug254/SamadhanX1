import { supabase } from './supabase';
import { User, Role } from '../types';

export const authApi = {
  login: async (email: string, password: string): Promise<string> => {
    const { data, error } = await supabase.auth.signInWithPassword({ email, password });
    if (error) throw error;
    if (!data.session) throw new Error('Supabase did not return an active session.');
    return data.session.access_token;
  },

  register: async (
    email: string,
    password: string,
    firstName: string,
    lastName: string,
    phone?: string,
    role: Role = 'CITIZEN'
  ): Promise<void> => {
    const { error } = await supabase.auth.signUp({
      email,
      password,
      options: {
        data: {
          first_name: firstName,
          last_name: lastName,
          phone_number: phone,
          role,
        },
      },
    });
    if (error) throw error;
  },

  getCurrentUser: async (): Promise<User> => {
    const { data: { user }, error } = await supabase.auth.getUser();
    if (error || !user) throw error || new Error('No authenticated user found');

    // Fetch profile and roles from Supabase
    const { data: profile } = await supabase
      .from('profiles')
      .select('*, organizations(id, name)')
      .eq('id', user.id)
      .single();

    const { data: userRoles } = await supabase
      .from('user_roles')
      .select('role_name')
      .eq('user_id', user.id);

    const primaryRole: Role = (userRoles && userRoles.length > 0
      ? userRoles[0].role_name
      : (user.user_metadata?.role || 'CITIZEN')) as Role;

    const firstName = profile?.first_name || user.user_metadata?.first_name || '';
    const lastName = profile?.last_name || user.user_metadata?.last_name || '';
    const fullName = `${firstName} ${lastName}`.trim() || user.email?.split('@')[0] || 'User';

    return {
      id: user.id,
      email: user.email || '',
      fullName,
      phoneNumber: profile?.phone_number || user.user_metadata?.phone_number,
      role: primaryRole,
      organizationId: profile?.organization_id,
      organizationName: profile?.organizations?.name,
      active: profile?.is_active ?? true,
      avatarUrl: profile?.avatar_url,
    };
  },

  logout: () => supabase.auth.signOut(),
  getSession: () => supabase.auth.getSession(),
};
