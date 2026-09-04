import { createContext, useContext, useState } from 'react';
import api from '../api/axios.js';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem('testly_user');
    return stored ? JSON.parse(stored) : null;
  });

  function persist(authResponse) {
    localStorage.setItem('testly_token', authResponse.token);
    const userInfo = {
      id: authResponse.userId,
      name: authResponse.name,
      email: authResponse.email,
      role: authResponse.role,
    };
    localStorage.setItem('testly_user', JSON.stringify(userInfo));
    setUser(userInfo);
    return userInfo;
  }

  async function login(email, password) {
    const res = await api.post('/auth/login', { email, password });
    return persist(res.data);
  }

  async function register(name, email, password, role) {
    const res = await api.post('/auth/register', { name, email, password, role });
    return persist(res.data);
  }

  function logout() {
    localStorage.removeItem('testly_token');
    localStorage.removeItem('testly_user');
    setUser(null);
  }

  return (
    <AuthContext.Provider value={{ user, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
