import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import api from '../lib/api';
import { LockIcon, UserIcon, MailIcon, Eye, EyeOff, UserCircle } from 'lucide-react';

const registerSchema = z.object({
  fullName: z.string().min(2, 'Full name is required'),
  email: z.string().email('Invalid email address'),
  username: z.string().min(3, 'Username must be at least 3 characters'),
  password: z.string().min(6, 'Password must be at least 6 characters'),
  role: z.string().min(1),
});
type RegisterForm = z.infer<typeof registerSchema>;

function DarkPanel() {
  return (
    <div
      className="hidden lg:flex w-1/2 h-full flex-col items-center justify-center"
      style={{ background: 'linear-gradient(135deg, #0f172a 0%, #1e3a8a 60%, #1d4ed8 100%)' }}
    >
      <div className="text-center select-none">
        <h1 className="text-5xl font-black text-white tracking-tight leading-tight">Hyundai</h1>
        <h1 className="text-5xl font-black tracking-tight leading-tight" style={{ color: '#60a5fa' }}>DMS</h1>
        <p className="mt-4 text-sm font-medium tracking-[0.2em] uppercase" style={{ color: 'rgba(255,255,255,0.4)' }}>
          Dealer Management System
        </p>
      </div>
    </div>
  );
}

export default function Register() {
  const navigate = useNavigate();
  const [error, setError] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [usernameError, setUsernameError] = useState('');
  const [emailError, setEmailError] = useState('');

  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<RegisterForm>({
    resolver: zodResolver(registerSchema),
    defaultValues: { role: 'ROLE_DEALER' },
  });

  const checkUsername = async (e: React.FocusEvent<HTMLInputElement>) => {
    const val = e.target.value;
    if (val.length >= 3) {
      try {
        const res = await api.get(`/auth/check-username?username=${val}`);
        setUsernameError(res.data.exists ? 'Username is already taken' : '');
      } catch {}
    }
  };

  const checkEmail = async (e: React.FocusEvent<HTMLInputElement>) => {
    const val = e.target.value;
    if (val.includes('@')) {
      try {
        const res = await api.get(`/auth/check-email?email=${val}`);
        setEmailError(res.data.exists ? 'Email is already in use' : '');
      } catch {}
    }
  };

  const onSubmit = async (data: RegisterForm) => {
    if (usernameError || emailError) return;
    try {
      await api.post('/auth/register', data);
      navigate('/login?registered=true');
    } catch (err: any) {
      setError(err.response?.data || 'Registration failed. Please try again.');
    }
  };

  const emailField = register('email');
  const usernameField = register('username');

  return (
    <div className="flex h-screen w-screen overflow-hidden">
      <DarkPanel />

      {/* Right — Form */}
      <div className="flex-1 lg:w-1/2 h-full flex items-center justify-center bg-white px-8">
        <div className="w-full max-w-sm">
          {/* Mobile logo */}
          <div className="lg:hidden text-center mb-10">
            <h1 className="text-3xl font-black text-gray-900">Hyundai <span className="text-blue-600">DMS</span></h1>
          </div>

          <h2 className="text-2xl font-bold text-gray-900 mb-1">Create account</h2>
          <p className="text-sm text-gray-400 mb-8">Register your dealership to get started</p>

          {error && (
            <div className="bg-red-50 border border-red-200 rounded-xl p-3 mb-5 text-sm text-red-700 font-medium">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <input type="hidden" {...register('role')} value="ROLE_DEALER" defaultValue="ROLE_DEALER" />

            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-1.5">Full Name</label>
              <div className="relative">
                <UserCircle className="absolute left-3.5 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400" />
                <input {...register('fullName')} placeholder="Your full name"
                  className="w-full pl-10 pr-4 py-3 border border-gray-200 rounded-xl text-sm bg-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition shadow-sm" />
              </div>
              {errors.fullName && <p className="mt-1 text-xs text-red-500">{errors.fullName.message}</p>}
            </div>

            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-1.5">Email Address</label>
              <div className="relative">
                <MailIcon className="absolute left-3.5 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400" />
                <input
                  {...emailField}
                  onBlur={(e) => { emailField.onBlur(e); checkEmail(e); }}
                  placeholder="you@dealership.com"
                  className={`w-full pl-10 pr-4 py-3 border rounded-xl text-sm bg-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition shadow-sm ${emailError ? 'border-red-400' : 'border-gray-200'}`}
                />
              </div>
              {(errors.email || emailError) && <p className="mt-1 text-xs text-red-500">{errors.email?.message || emailError}</p>}
            </div>

            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-1.5">Username</label>
              <div className="relative">
                <UserIcon className="absolute left-3.5 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400" />
                <input
                  {...usernameField}
                  onBlur={(e) => { usernameField.onBlur(e); checkUsername(e); }}
                  placeholder="Choose a username"
                  className={`w-full pl-10 pr-4 py-3 border rounded-xl text-sm bg-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition shadow-sm ${usernameError ? 'border-red-400' : 'border-gray-200'}`}
                />
              </div>
              {(errors.username || usernameError) && <p className="mt-1 text-xs text-red-500">{errors.username?.message || usernameError}</p>}
            </div>

            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-1.5">Password</label>
              <div className="relative">
                <LockIcon className="absolute left-3.5 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400" />
                <input
                  type={showPassword ? 'text' : 'password'}
                  {...register('password')}
                  placeholder="Min. 6 characters"
                  className="w-full pl-10 pr-11 py-3 border border-gray-200 rounded-xl text-sm bg-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition shadow-sm"
                />
                <button type="button" onClick={() => setShowPassword(p => !p)}
                  className="absolute right-3.5 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 transition">
                  {showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                </button>
              </div>
              {errors.password && <p className="mt-1 text-xs text-red-500">{errors.password.message}</p>}
            </div>

            <button type="submit" disabled={isSubmitting}
              className="w-full py-3 rounded-xl text-sm font-semibold text-white transition-all active:scale-[0.98] disabled:opacity-60 flex items-center justify-center gap-2"
              style={{ background: 'linear-gradient(135deg, #1e40af, #2563eb)', boxShadow: '0 4px 15px rgba(37,99,235,0.35)' }}>
              {isSubmitting ? (
                <><div className="h-4 w-4 border-2 border-white/30 border-t-white rounded-full animate-spin" /> Creating account...</>
              ) : 'Create Account'}
            </button>
          </form>

          <p className="text-center text-sm text-gray-500 mt-6">
            Already have an account?{' '}
            <Link to="/login" className="text-blue-600 hover:text-blue-700 font-semibold">Sign in</Link>
          </p>
        </div>
      </div>
    </div>
  );
}
