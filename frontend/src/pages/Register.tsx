import { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import api from '../lib/api';
import { LockIcon, UserIcon, MailIcon, ShieldAlert } from 'lucide-react';

const registerSchema = z.object({
  fullName: z.string().min(2, 'Full Name is required'),
  email: z.string().email('Invalid email address'),
  username: z.string().min(3, 'Username must be at least 3 characters'),
  password: z.string().min(6, 'Password must be at least 6 characters'),
  role: z.string().min(1, 'Role selection is required'),
});

type RegisterForm = z.infer<typeof registerSchema>;

export default function Register() {
  const navigate = useNavigate();
  const [error, setError] = useState('');
  
  const { register, handleSubmit, formState: { errors, isValid, isSubmitting } } = useForm<RegisterForm>({
    resolver: zodResolver(registerSchema),
    defaultValues: { role: 'ROLE_EMPLOYEE' },
    mode: 'onTouched'
  });

  const [usernameError, setUsernameError] = useState('');
  const [emailError, setEmailError] = useState('');

  const checkUsername = async (e: React.FocusEvent<HTMLInputElement>) => {
    const val = e.target.value;
    if (val.length >= 3) {
      try {
        const res = await api.get(`/auth/check-username?username=${val}`);
        if (res.data.exists) setUsernameError('Username is already taken');
        else setUsernameError('');
      } catch (err) {}
    }
  };

  const checkEmail = async (e: React.FocusEvent<HTMLInputElement>) => {
    const val = e.target.value;
    if (val.includes('@')) {
      try {
        const res = await api.get(`/auth/check-email?email=${val}`);
        if (res.data.exists) setEmailError('Email is already in use');
        else setEmailError('');
      } catch (err) {}
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
    <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8 bg-white p-10 rounded-xl shadow-lg border border-gray-100">
        <div>
          <h2 className="mt-2 text-center text-3xl font-extrabold text-gray-900 border-b pb-4">
            Hyundai DMS
          </h2>
          <p className="mt-4 text-center text-sm text-gray-600">
            Create a new system account
          </p>
        </div>

        {error && (
          <div className="bg-red-50 border-l-4 border-red-500 p-4 mb-4">
            <p className="text-sm text-red-700">{error}</p>
          </div>
        )}

        <form className="mt-8 space-y-6" onSubmit={handleSubmit(onSubmit)}>
          <div className="space-y-4">
            
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Full Name</label>
              <div className="relative rounded-md shadow-sm">
                <input
                  {...register('fullName')}
                  className="focus:ring-blue-500 focus:border-blue-500 block w-full px-3 sm:text-sm border-gray-300 rounded-md py-3 bg-gray-50 border"
                  placeholder="John Doe"
                />
              </div>
              {errors.fullName && <p className="mt-1 text-xs text-red-500">{errors.fullName.message}</p>}
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Email Address</label>
              <div className="relative rounded-md shadow-sm">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <MailIcon className="h-5 w-5 text-gray-400" />
                </div>
                <input
                  {...emailField}
                  onBlur={(e) => {
                    emailField.onBlur(e);
                    checkEmail(e);
                  }}
                  className={`focus:ring-blue-500 focus:border-blue-500 block w-full pl-10 sm:text-sm border-gray-300 rounded-md py-3 bg-gray-50 border ${emailError ? 'border-red-500 ring-red-500' : ''}`}
                  placeholder="john@dealership.com"
                />
              </div>
              {errors.email && <p className="mt-1 text-xs text-red-500">{errors.email.message}</p>}
              {!errors.email && emailError && <p className="mt-1 text-xs text-red-500">{emailError}</p>}
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Username</label>
              <div className="relative rounded-md shadow-sm">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <UserIcon className="h-5 w-5 text-gray-400" />
                </div>
                <input
                  {...usernameField}
                  onBlur={(e) => {
                    usernameField.onBlur(e);
                    checkUsername(e);
                  }}
                  className={`focus:ring-blue-500 focus:border-blue-500 block w-full pl-10 sm:text-sm border-gray-300 rounded-md py-3 bg-gray-50 border ${usernameError ? 'border-red-500 ring-red-500' : ''}`}
                  placeholder="johndoe"
                />
              </div>
              {errors.username && <p className="mt-1 text-xs text-red-500">{errors.username.message}</p>}
              {!errors.username && usernameError && <p className="mt-1 text-xs text-red-500">{usernameError}</p>}
            </div>

            <div className="hidden">
              <input type="hidden" {...register('role')} value="ROLE_DEALER" />
            </div>



            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Password</label>
              <div className="relative rounded-md shadow-sm">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <LockIcon className="h-5 w-5 text-gray-400" />
                </div>
                <input
                  type="password"
                  {...register('password')}
                  className="focus:ring-blue-500 focus:border-blue-500 block w-full pl-10 sm:text-sm border-gray-300 rounded-md py-3 bg-gray-50 border"
                  placeholder="••••••••"
                />
              </div>
              {errors.password && <p className="mt-1 text-xs text-red-500">{errors.password.message}</p>}
            </div>
          </div>

          <div>
            <button
              type="submit"
              disabled={isSubmitting}
              className="group relative w-full flex justify-center py-3 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-colors disabled:opacity-70"
            >
              {isSubmitting ? 'Creating Account...' : 'Register Account'}
            </button>
          </div>
          
          <div className="text-center mt-4">
            <Link to="/login" className="text-sm font-medium text-blue-600 hover:text-blue-500">
              Already have an account? Sign in
            </Link>
          </div>
        </form>
      </div>
    </div>
  );
}
