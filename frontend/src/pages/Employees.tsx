import React, { useState, useEffect, useCallback, useRef } from 'react';
import { Users, UserPlus, Search, Trash2, CheckCircle2, XCircle, UserCheck, UserX } from 'lucide-react';
import api from '../lib/api';
import Pagination from '../components/Pagination';
import ModalPortal from '../components/ModalPortal';
import SortableHeader from '../components/SortableHeader';
import type { SortDir } from '../components/SortableHeader';
import { useGlobalShortcuts } from '../hooks/useKeyboardShortcuts';

interface Employee {
  id: number;
  username: string;
  email: string;
  fullName: string;
  roles: string[];
  isActive: boolean;
  createdAt: string;
}

export default function Employees() {
  const [employees, setEmployees] = useState<Employee[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [error, setError] = useState('');

  // Pagination
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const pageSize = 10;

  // Form State
  const [formData, setFormData] = useState({
    username: '',
    fullName: '',
    email: '',
    password: '',
    role: 'ROLE_EMPLOYEE'
  });

  // Sorting — default newest first
  const [sortField, setSortField] = useState('createdAt');
  const [sortDir, setSortDir] = useState<SortDir>('desc');
  const searchRef = useRef<HTMLInputElement>(null);

  const handleSort = (field: string, dir: SortDir) => {
    setSortField(field);
    setSortDir(dir);
    setPage(0);
  };

  // Keyboard shortcuts
  useGlobalShortcuts({
    onSearch: () => searchRef.current?.focus(),
    onCreate: () => setIsModalOpen(true),
  });

  const fetchEmployees = useCallback(async () => {
    try {
      setLoading(true);
      setError('');
      const sortParam = sortDir ? `&sort=${sortField},${sortDir}` : '';
      const res = await api.get(`/users?role=ROLE_EMPLOYEE&search=${encodeURIComponent(searchTerm)}&page=${page}&size=${pageSize}${sortParam}`);
      setEmployees(res.data.content || []);
      setTotalPages(res.data.totalPages || 0);
      setTotalElements(res.data.totalElements || 0);
    } catch (err) {
      setError('Failed to load employees.');
    } finally {
      setLoading(false);
    }
  }, [searchTerm, page, sortField, sortDir]);

  useEffect(() => {
    const timer = setTimeout(fetchEmployees, 200);
    return () => clearTimeout(timer);
  }, [fetchEmployees]);

  const handleFilterChange = (setter: any, val: any) => {
    setter(val);
    setPage(0);
  };

  const handleCreateEmployee = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setError('');
      await api.post('/auth/register', {
        ...formData,
      });
      setIsModalOpen(false);
      fetchEmployees();
      setFormData({ username: '', fullName: '', email: '', password: '', role: 'ROLE_EMPLOYEE' });
    } catch (err: any) {
      setError(err.response?.data || 'Failed to create employee.');
    }
  };

  const handleToggleStatus = async (emp: Employee) => {
    try {
      await api.put(`/users/${emp.id}`, {
        ...emp,
        isActive: !emp.isActive
      });
      fetchEmployees();
    } catch {
      alert('Failed to toggle employee status.');
    }
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm('Are you sure you want to delete this employee? This action cannot be undone.')) return;
    try {
      await api.delete(`/users/${id}`);
      fetchEmployees();
    } catch {
      alert('Failed to delete employee.');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold text-gray-900 tracking-tight">Manage Employees</h1>
          <p className="text-gray-500 mt-1 text-sm">View and manage your dealership staff and access levels.</p>
        </div>
        <button 
          onClick={() => setIsModalOpen(true)}
          className="flex items-center px-5 py-2.5 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-all font-semibold shadow-lg shadow-blue-200"
        >
          <UserPlus className="h-5 w-5 mr-2" />
          Add New Employee
        </button>
      </div>

      {/* Search and Filters */}
      <div className="bg-white p-4 rounded-xl shadow-sm border border-gray-100 flex flex-col md:flex-row gap-4 items-center">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-gray-400" />
          <input
            type="text"
            placeholder="Search by name, email, or username..."
            className="w-full pl-10 pr-4 py-2 border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none transition-all text-sm"
            value={searchTerm}
            ref={searchRef}
            onChange={(e) => handleFilterChange(setSearchTerm, e.target.value)}
          />
        </div>
        <div className="text-xs text-gray-400 font-bold bg-gray-50 px-3 py-1 rounded-full border border-gray-100">
           {totalElements} Staff Members
        </div>
      </div>

      {error && (
        <div className="bg-red-50 border-l-4 border-red-500 p-4 rounded-r-lg">
          <p className="text-sm text-red-700 font-medium">{error}</p>
        </div>
      )}

      {/* Employee List */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-x-auto text-black transition-all">
        {loading ? (
          <div className="p-12 text-center text-gray-500 flex flex-col items-center">
            <div className="animate-spin h-8 w-8 border-4 border-blue-500 border-t-transparent rounded-full mb-4"></div>
            <p className="font-medium">Syncing employee registry...</p>
          </div>
        ) : employees.length === 0 ? (
          <div className="p-16 text-center text-gray-500">
            <div className="bg-gray-50 w-16 h-16 rounded-full flex items-center justify-center mx-auto mb-4">
              <Users className="h-8 w-8 opacity-20" />
            </div>
            <p className="font-bold text-gray-900">No employees found</p>
            <p className="text-sm text-gray-400">Try adjusting your search or add a new staff member.</p>
          </div>
        ) : (
          <>
          <div className="overflow-x-auto">
            <table className="w-full text-left">
              <thead>
                <tr className="bg-gray-50/50 border-b border-gray-100">
                  <SortableHeader label="Employee" field="fullName" currentSort={sortField} currentDir={sortDir} onSort={handleSort} />
                  <SortableHeader label="Status" field="isActive" currentSort={sortField} currentDir={sortDir} onSort={handleSort} />
                  <SortableHeader label="Onboarded" field="createdAt" currentSort={sortField} currentDir={sortDir} onSort={handleSort} />
                  <th className="px-5 py-3 text-right text-xs font-semibold text-gray-500 uppercase tracking-wider">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {employees.map((emp) => (
                  <tr key={emp.id} className="hover:bg-gray-50/50 transition-colors group">
                    <td className="px-6 py-4">
                      <div className="flex items-center">
                        <div className={`h-10 w-10 rounded-full ${emp.isActive ? 'bg-blue-600' : 'bg-gray-300'} flex items-center justify-center text-white font-bold mr-4 shadow-sm`}>
                          {emp.fullName?.charAt(0) || emp.username.charAt(0)}
                        </div>
                        <div>
                          <div className="font-bold text-gray-900 leading-tight">{emp.fullName}</div>
                          <div className="text-xs text-gray-400 font-medium">@{emp.username} · {emp.email}</div>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      {emp.isActive ? (
                        <span className="inline-flex items-center px-3 py-1 rounded-full text-[10px] font-extrabold bg-green-100 text-green-700 border border-green-200">
                          <CheckCircle2 className="h-3 w-3 mr-1.5" /> ACTIVE
                        </span>
                      ) : (
                        <span className="inline-flex items-center px-3 py-1 rounded-full text-[10px] font-extrabold bg-red-50 text-red-600 border border-red-100">
                          <XCircle className="h-3 w-3 mr-1.5" /> DEACTIVATED
                        </span>
                      )}
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-500 font-medium">
                      {new Date(emp.createdAt).toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' })}
                    </td>
                    <td className="px-6 py-4">
                      <div className="flex items-center justify-end space-x-2">
                        <button 
                          onClick={() => handleToggleStatus(emp)}
                          className={`p-2 rounded-lg transition-all ${emp.isActive ? 'bg-red-50 text-red-600 hover:bg-red-100' : 'bg-green-50 text-green-600 hover:bg-green-100'}`}
                          title={emp.isActive ? 'Deactivate Employee' : 'Activate Employee'}
                        >
                          {emp.isActive ? <UserX className="h-4 w-4" /> : <UserCheck className="h-4 w-4" />}
                        </button>
                        <button 
                          onClick={() => handleDelete(emp.id)}
                          className="p-2 bg-gray-50 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-all"
                          title="Purge Record"
                        >
                          <Trash2 className="h-4 w-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Pagination 
            currentPage={page}
            totalPages={totalPages}
            totalElements={totalElements}
            onPageChange={setPage}
            pageSize={pageSize}
          />
          </>
        )}
      </div>

      {/* Add Employee Modal */}
      {isModalOpen && (
        <ModalPortal onClose={() => setIsModalOpen(false)}>
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md flex flex-col max-h-[90vh]" onClick={e => e.stopPropagation()}>
            <div className="px-6 py-5 bg-gray-50 border-b border-gray-100 flex justify-between items-center text-black flex-shrink-0">
              <div>
                <h2 className="text-xl font-bold text-gray-900">Add Staff Member</h2>
                <p className="text-xs text-gray-400 mt-0.5">Create a new dealership employee account.</p>
              </div>
              <button onClick={() => setIsModalOpen(false)} className="text-gray-400 hover:text-gray-600 p-2 hover:bg-gray-200 rounded-full transition-all">
                <XCircle className="h-6 w-6" />
              </button>
            </div>
            
            <form onSubmit={handleCreateEmployee} className="p-6 space-y-4 text-black overflow-y-auto flex-1">
              <div>
                <label className="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">Display Name *</label>
                <input
                  required
                  type="text"
                  className="w-full px-4 py-2.5 bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-blue-500 focus:bg-white outline-none transition-all"
                  placeholder="e.g. Sergio Busquets"
                  value={formData.fullName}
                  onChange={(e) => setFormData({...formData, fullName: e.target.value})}
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">Username *</label>
                  <input
                    required
                    type="text"
                    className="w-full px-4 py-2.5 bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-blue-500 focus:bg-white outline-none transition-all"
                    placeholder="sergio5"
                    value={formData.username}
                    onChange={(e) => setFormData({...formData, username: e.target.value})}
                  />
                </div>
                <div>
                  <label className="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">Password *</label>
                  <input
                    required
                    type="password"
                    className="w-full px-4 py-2.5 bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-blue-500 focus:bg-white outline-none transition-all"
                    placeholder="••••••••"
                    value={formData.password}
                    onChange={(e) => setFormData({...formData, password: e.target.value})}
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">Work Email *</label>
                <input
                  required
                  type="email"
                  className="w-full px-4 py-2.5 bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-blue-500 focus:bg-white outline-none transition-all"
                  placeholder="staff@dealership.com"
                  value={formData.email}
                  onChange={(e) => setFormData({...formData, email: e.target.value})}
                />
              </div>

              <div className="pt-4 flex gap-3">
                <button
                  type="button"
                  onClick={() => setIsModalOpen(false)}
                  className="flex-1 px-4 py-3 border border-gray-200 text-gray-600 rounded-xl font-bold hover:bg-gray-50 transition-colors"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="flex-1 px-4 py-3 bg-blue-600 text-white rounded-xl font-bold hover:bg-blue-700 shadow-lg shadow-blue-200 transition-all active:scale-95"
                >
                  Register Staff
                </button>
              </div>
            </form>
          </div>
        </ModalPortal>
      )}
    </div>
  );
}
