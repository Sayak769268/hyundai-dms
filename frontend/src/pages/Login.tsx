import React, { useState } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import api from '../lib/api';
import { LockIcon, UserIcon, Eye, EyeOff } from 'lucide-react';

const loginSchema = z.object({
  username: z.string().min(1, 'Username is required'),
  password: z.string().min(1, 'Password is required'),
});
type LoginForm = z.infer<typeof loginSchema>;

export default function Login() {
  const navigate = useNavigate();
  const location = useLocation();
  const [error, setError] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const isRegistered = new URLSearchParams(location.search).get('registered') === 'true';

  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<LoginForm>({
    resolver: zodResolver(loginSchema),
  });

  const onSubmit = async (data: LoginForm) => {
    try {
      setError('');
      const response = await api.post('/auth/login', data);
      localStorage.setItem('token', response.data.token);
      localStorage.setItem('user', JSON.stringify({
        username: response.data.username,
        roles: response.data.roles,
        permissions: response.data.permissions,
        dealerId: response.data.dealerId,
        dealerName: response.data.dealerName,
      }));
      navigate('/');
    } catch (err: any) {
      const status = err.response?.status;
      const serverMsg = err.response?.data?.message;
      if (status === 401) setError('Invalid username or password.');
      else if (status === 423) setError(serverMsg || 'Your account is locked due to multiple failed login attempts. Try again after 30 minutes.');
      else if (status === 403) setError(serverMsg || 'Your account has been deactivated or expired. Please contact admin.');
      else setError('Login failed. Please try again.');
    }
  };

  return (
    <div className="flex h-screen w-screen overflow-hidden">

      {/* LEFT — Branding */}
      <div
        className="hidden lg:flex w-1/2 h-full flex-col items-center justify-center"
        style={{ background: 'linear-gradient(135deg, #0f172a 0%, #1e3a8a 60%, #1d4ed8 100%)' }}
      >
        <div className="text-center select-none">
          <h1 className="text-5xl font-black text-white tracking-tight leading-tight">
            Hyundai
          </h1>
          <h1 className="text-5xl font-black tracking-tight leading-tight" style={{ color: '#60a5fa' }}>
            DMS
          </h1>
          <p className="mt-4 text-sm font-medium tracking-[0.2em] uppercase" style={{ color: 'rgba(255,255,255,0.4)' }}>
            Dealer Management System
          </p>
        </div>
      </div>

      {/* RIGHT — Form */}
      <div className="flex-1 lg:w-1/2 h-full flex items-center justify-center bg-white px-8">
        <div className="w-full max-w-sm">

          {/* Mobile logo */}
          <div className="lg:hidden text-center mb-10">
            <h1 className="text-3xl font-black text-gray-900">Hyundai <span className="text-blue-600">DMS</span></h1>
          </div>

          <h2 className="text-2xl font-bold text-gray-900 mb-1">Welcome back</h2>
          <p className="text-sm text-gray-400 mb-8">Sign in to your account to continue</p>

          {isRegistered && !error && (
            <div className="bg-green-50 border border-green-200 rounded-lg p-3 mb-5 text-sm text-green-700">
              Account created. Please sign in.
            </div>
          )}
          {error && (
            <div className="bg-red-50 border border-red-200 rounded-lg p-3 mb-5 text-sm text-red-600">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">Username <span className="text-red-500">*</span></label>
              <div className="relative">
                <UserIcon className="absolute left-3.5 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-300" />
                <input
                  {...register('username')}
                  autoComplete="username"
                  placeholder="Enter username"
                  className="w-full pl-10 pr-4 py-3 border border-gray-200 rounded-xl text-sm bg-gray-50 focus:bg-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
                />
              </div>
              {errors.username && <p className="mt-1 text-xs text-red-500">{errors.username.message}</p>}
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">Password <span className="text-red-500">*</span></label>
              <div className="relative">
                <LockIcon className="absolute left-3.5 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-300" />
                <input
                  type={showPassword ? 'text' : 'password'}
                  {...register('password')}
                  autoComplete="current-password"
                  placeholder="Enter password"
                  className="w-full pl-10 pr-11 py-3 border border-gray-200 rounded-xl text-sm bg-gray-50 focus:bg-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
                />
                <button type="button" onClick={() => setShowPassword(p => !p)}
                  className="absolute right-3.5 top-1/2 -translate-y-1/2 text-gray-300 hover:text-gray-500 transition-colors">
                  {showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                </button>
              </div>
              {errors.password && <p className="mt-1 text-xs text-red-500">{errors.password.message}</p>}
            </div>

            <button type="submit" disabled={isSubmitting}
              className="w-full py-3 rounded-xl text-sm font-semibold text-white transition-all active:scale-[0.98] disabled:opacity-60 flex items-center justify-center gap-2"
              style={{ background: 'linear-gradient(135deg, #1e40af, #2563eb)', boxShadow: '0 4px 15px rgba(37,99,235,0.35)' }}>
              {isSubmitting
                ? <><div className="h-4 w-4 border-2 border-white/30 border-t-white rounded-full animate-spin" /> Signing in...</>
                : 'Sign In'}
            </button>
          </form>

          <p className="text-center text-sm text-gray-400 mt-6">
            New dealership?{' '}
            <Link to="/register" className="text-blue-600 hover:text-blue-700 font-semibold">Create an account</Link>
          </p>
        </div>
      </div>
    </div>
  );
}
