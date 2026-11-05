import React, { createContext, useContext, useEffect, useState, useCallback } from 'react';
import type { AdminUser } from '../../types';
import { adminAuthApi, setOnUnauthorizedCallback, tokenStorage } from '../../services/api';

interface AuthContextType {
  user: AdminUser | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (username: string, pass: string) => Promise<void>;
  logout: () => Promise<void>;
  refreshMe: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<AdminUser | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  const logout = useCallback(async () => {
    try {
      if (tokenStorage.get()) {
        await adminAuthApi.logout();
      }
    } catch {
      // Ignore network errors on logout
    } finally {
      tokenStorage.clear();
      setUser(null);
    }
  }, []);

  const refreshMe = useCallback(async () => {
    const token = tokenStorage.get();
    if (!token) {
      setUser(null);
      setIsLoading(false);
      return;
    }

    try {
      const me = await adminAuthApi.getMe();
      setUser(me);
    } catch {
      tokenStorage.clear();
      setUser(null);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    setOnUnauthorizedCallback(() => {
      setUser(null);
    });
    refreshMe();
  }, [refreshMe]);

  const login = async (username: string, pass: string) => {
    setIsLoading(true);
    try {
      const res = await adminAuthApi.login(username, pass);
      setUser(res.user);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: !!user,
        isLoading,
        login,
        logout,
        refreshMe,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
