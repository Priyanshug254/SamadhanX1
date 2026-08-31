import React, { createContext, useContext, useState, useEffect } from 'react';
import { User, Role } from '../types';
import { authApi } from '../api/auth';
import { supabase } from '../api/supabase';

interface AuthContextType {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (email: string, pass: string) => Promise<void>;
  register: (email: string, pass: string, firstName: string, lastName: string, phone?: string, role?: Role) => Promise<void>;
  logout: () => void;
  hasRole: (...roles: Role[]) => boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(() => {
    const saved = localStorage.getItem('samadhanx_user');
    return saved ? JSON.parse(saved) : null;
  });
  const [token, setToken] = useState<string | null>(() => {
    return localStorage.getItem('samadhanx_token');
  });
  const [isLoading, setIsLoading] = useState<boolean>(true);

  useEffect(() => {
    const checkSession = async () => {
      try {
        const { data } = await authApi.getSession();
        const sessionToken = data.session?.access_token;
        if (sessionToken) {
          setToken(sessionToken);
          localStorage.setItem('samadhanx_token', sessionToken);
          const currentUser = await authApi.getCurrentUser();
          setUser(currentUser);
          localStorage.setItem('samadhanx_user', JSON.stringify(currentUser));
        } else {
          setToken(null);
          setUser(null);
          localStorage.removeItem('samadhanx_token');
          localStorage.removeItem('samadhanx_user');
        }
      } catch (err) {
        console.error('Session verification error:', err);
      } finally {
        setIsLoading(false);
      }
    };

    checkSession();

    // Listen to Supabase auth state changes
    const { data: authListener } = supabase.auth.onAuthStateChange(async (event, session) => {
      if (session?.access_token) {
        setToken(session.access_token);
        localStorage.setItem('samadhanx_token', session.access_token);
        try {
          const currentUser = await authApi.getCurrentUser();
          setUser(currentUser);
          localStorage.setItem('samadhanx_user', JSON.stringify(currentUser));
        } catch {
          // Profile may be in process of creation
        }
      } else if (event === 'SIGNED_OUT') {
        setToken(null);
        setUser(null);
        localStorage.removeItem('samadhanx_token');
        localStorage.removeItem('samadhanx_user');
      }
    });

    return () => {
      authListener.subscription.unsubscribe();
    };
  }, []);

  const login = async (email: string, pass: string) => {
    setIsLoading(true);
    try {
      const accessToken = await authApi.login(email, pass);
      setToken(accessToken);
      localStorage.setItem('samadhanx_token', accessToken);
      const currentUser = await authApi.getCurrentUser();
      setUser(currentUser);
      localStorage.setItem('samadhanx_user', JSON.stringify(currentUser));
    } finally {
      setIsLoading(false);
    }
  };

  const register = async (
    email: string,
    pass: string,
    firstName: string,
    lastName: string,
    phone?: string,
    role: Role = 'CITIZEN'
  ) => {
    setIsLoading(true);
    try {
      await authApi.register(email, pass, firstName, lastName, phone, role);
    } finally {
      setIsLoading(false);
    }
  };

  const logout = () => {
    void authApi.logout();
    setToken(null);
    setUser(null);
    localStorage.removeItem('samadhanx_token');
    localStorage.removeItem('samadhanx_user');
  };

  const hasRole = (...roles: Role[]) => {
    if (!user) return false;
    if (user.role === 'SUPER_ADMIN') return true;
    return roles.includes(user.role);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: !!token && !!user,
        isLoading,
        login,
        register,
        logout,
        hasRole,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
