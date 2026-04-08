import React, { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import api from '../lib/api';
import { Plus, Search, X, Eye, Pencil, Archive, Filter } from 'lucide-react';
import Pagination from '../components/Pagination';

const STATUS_COLORS: Record<string, string> = {
  NEW: 'bg-blue-100 text-blue-700',
  INTERESTED: 'bg-yellow-100 text-yellow-700',
  BOOKED: 'bg-green-100 text-green-700',
  LOST: 'bg-red-100 text-red-700',
};

const customerSchema = z.object({
  firstName: z.string().min(1, 'First name is required'),
  lastName: z.string().min(1, 'Last name is required'),
  email: z.string().email('Invalid email address'),
  phone: z.string()
    .min(7, 'Phone must be at least 7 digits')
    .max(15, 'Phone must be at most 15 digits')
    .regex(/^[0-9+\-\s()]+$/, 'Invalid phone number'),
  address: z.string().optional(),
  notes: z.string().optional(),
  status: z.string().min(1),
  assignedEmployeeId: z.string().optional(),
  nextFollowUpDate: z.string().optional(),
  dealerId: z.number().optional(),
});

type CustomerForm = z.infer<typeof customerSchema>;

interface Customer {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  address?: string;
  notes?: string;
  status: string;
  assignedEmployeeId?: number;
  assignedEmployeeName?: string;
  nextFollowUpDate?: string;
  isActive: boolean;
  createdAt?: string;
}

export default function Customers() {
  const navigate = useNavigate();
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [employeeFilter, setEmployeeFilter] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingCustomer, setEditingCustomer] = useState<Customer | null>(null);
  const [confirmArchive, setConfirmArchive] = useState<Customer | null>(null);
  
  // Pagination
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const pageSize = 15;

  const [employees, setEmployees] = useState<{id: number; name: string}[]>([]);

  // Fetch employee list once for the assignment dropdown
  useEffect(() => {
    api.get('/users/employees')
      .then(res => setEmployees(res.data))
      .catch(() => {});
  }, []);

  const { register, handleSubmit, reset, setValue, formState: { errors, isSubmitting } } = useForm<CustomerForm>({
    resolver: zodResolver(customerSchema),
    defaultValues: { status: 'NEW' },
  });

  const fetchCustomers = useCallback(async () => {
    setLoading(true);
    try {
      let url = `/customers?search=${encodeURIComponent(search)}&page=${page}&size=${pageSize}`;
      if (statusFilter) url += `&status=${statusFilter}`;
      if (employeeFilter) url += `&assignedEmployeeId=${employeeFilter}`;
      
      const res = await api.get(url);
      setCustomers(res.data?.content ?? []);
      setTotalPages(res.data?.totalPages ?? 0);
      setTotalElements(res.data?.totalElements ?? 0);
    } catch (err) {
      console.error('Failed to fetch customers', err);
    } finally {
      setLoading(false);
    }
  }, [search, statusFilter, employeeFilter, page]);

  useEffect(() => {
    const timer = setTimeout(fetchCustomers, 200);
    return () => clearTimeout(timer);
  }, [fetchCustomers]);

  const handleFilterChange = (setter: any, val: any) => {
    setter(val);
    setPage(0);
  };

  const openAddModal = () => {
    setEditingCustomer(null);
    reset({ status: 'NEW' });
    setIsModalOpen(true);
  };
// ... (rest of the component until return)

  const openEditModal = (c: Customer) => {
    setEditingCustomer(c);
    setValue('firstName', c.firstName);
    setValue('lastName', c.lastName);
    setValue('email', c.email);
    setValue('phone', c.phone);
    setValue('address', c.address ?? '');
    setValue('notes', c.notes ?? '');
    setValue('status', c.status);
    setValue('nextFollowUpDate', c.nextFollowUpDate ?? '');
    setValue('assignedEmployeeId', c.assignedEmployeeId?.toString() ?? '');
    setIsModalOpen(true);
  };

  const onSubmit = async (data: CustomerForm) => {
    try {
      const payload = {
        ...data,
        assignedEmployeeId: data.assignedEmployeeId ? Number(data.assignedEmployeeId) : null,
        nextFollowUpDate: data.nextFollowUpDate || null,
        dealerId: 1,
      };
      if (editingCustomer) {
        const res = await api.put(`/customers/${editingCustomer.id}`, payload);
        setCustomers(prev => prev.map(c => c.id === editingCustomer.id ? res.data : c));
      } else {
        const res = await api.post('/customers', payload);
        setCustomers(prev => [res.data, ...prev]);
      }
      setIsModalOpen(false);
      reset();
    } catch (err: any) {
      alert(err.response?.data?.message || err.response?.data?.error || err.response?.data || 'Failed to save customer.');
    }
  };

  const handleArchive = async (c: Customer) => {
    try {
      await api.patch(`/customers/${c.id}/archive`);
      setCustomers(prev => prev.filter(x => x.id !== c.id));
      setConfirmArchive(null);
    } catch (err) {
      alert('Failed to archive customer.');
    }
  };

  return (
    <div className="space-y-6 relative">
      {/* Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Customers CRM</h1>
          <p className="text-sm text-gray-500 mt-1">{totalElements} active customers</p>
        </div>
        {!JSON.parse(localStorage.getItem('user') || '{}').roles?.includes('ROLE_ADMIN') && (
          <button
            onClick={openAddModal}
            className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2.5 rounded-lg text-sm font-semibold flex items-center gap-2 transition-colors shadow-sm"
          >
            <Plus className="h-4 w-4" />
            Add Customer
          </button>
        )}
      </div>

      {/* Filters Bar */}
      <div className="bg-white border border-gray-200 rounded-xl p-4 flex flex-wrap gap-3 items-center">
        <div className="relative flex-1 min-w-[200px]">
          <Search className="h-4 w-4 absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
          <input
            type="text"
            placeholder="Search by name, email or phone..."
            value={search}
            onChange={e => handleFilterChange(setSearch, e.target.value)}
            className="pl-9 pr-4 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 w-full"
          />
        </div>
        <div className="flex items-center gap-2">
          <Filter className="h-4 w-4 text-gray-400" />
          <select
            value={statusFilter}
            onChange={e => handleFilterChange(setStatusFilter, e.target.value)}
            className="border border-gray-300 rounded-lg text-sm py-2 px-3 focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="">All Statuses</option>
            <option value="NEW">New</option>
            <option value="INTERESTED">Interested</option>
            <option value="BOOKED">Booked</option>
            <option value="LOST">Lost</option>
          </select>
        </div>
        <select
          value={employeeFilter}
          onChange={e => handleFilterChange(setEmployeeFilter, e.target.value)}
          className="border border-gray-300 rounded-lg text-sm py-2 px-3 focus:outline-none focus:ring-2 focus:ring-blue-500"
        >
          <option value="">All Employees</option>
          {employees.map(emp => (
            <option key={emp.id} value={emp.id}>{emp.name}</option>
          ))}
        </select>
        {(search || statusFilter || employeeFilter) && (
          <button onClick={() => { setSearch(''); setStatusFilter(''); setEmployeeFilter(''); setPage(0); }} className="text-xs text-red-500 hover:text-red-700 flex items-center gap-1 border border-red-200 px-2 py-1.5 rounded-lg">
            <X className="h-3 w-3" /> Clear
          </button>
        )}
      </div>

      {/* Table */}
      <div className="bg-white shadow-sm border border-gray-200 rounded-xl overflow-x-auto">
        {loading ? (
          <div className="p-16 text-center text-gray-400 text-sm">Loading customers...</div>
        ) : customers.length === 0 ? (
          <div className="p-16 text-center text-gray-400 text-sm">No customers found. Add your first one!</div>
        ) : (
          <>
          <table className="min-w-full divide-y divide-gray-100">
            <thead className="bg-gray-50">
              <tr>
                {['Name', 'Contact', 'Status', 'Assigned To', 'Next Follow-up', 'Actions'].map(h => (
                  <th key={h} className="px-5 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-100">
              {customers.map(c => (
                <tr key={c.id} className="hover:bg-gray-50 transition-colors">
                  <td className="px-5 py-3 whitespace-nowrap">
                    <div className="font-semibold text-gray-900 text-sm">{c.firstName} {c.lastName}</div>
                  </td>
                  <td className="px-5 py-3">
                    <div className="text-sm text-gray-700">{c.email}</div>
                    <div className="text-xs text-gray-400">{c.phone}</div>
                  </td>
                  <td className="px-5 py-3">
                    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold ${STATUS_COLORS[c.status] ?? 'bg-gray-100 text-gray-600'}`}>
                      {c.status}
                    </span>
                  </td>
                  <td className="px-5 py-3 text-sm text-gray-600">{c.assignedEmployeeName ?? '—'}</td>
                  <td className="px-5 py-3 text-sm text-gray-600">
                    {c.nextFollowUpDate ? new Date(c.nextFollowUpDate).toLocaleDateString('en-IN') : '—'}
                  </td>
                  <td className="px-5 py-3 whitespace-nowrap">
                    <div className="flex items-center gap-2">
                      <button
                        onClick={() => navigate(`/customers/${c.id}`)}
                        className="p-1.5 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-md transition-colors"
                        title="View"
                      >
                        <Eye className="h-4 w-4" />
                      </button>
                      <button
                        onClick={() => openEditModal(c)}
                        className="p-1.5 text-gray-400 hover:text-amber-600 hover:bg-amber-50 rounded-md transition-colors"
                        title="Edit"
                      >
                        <Pencil className="h-4 w-4" />
                      </button>
                      <button
                        onClick={() => setConfirmArchive(c)}
                        className="p-1.5 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-md transition-colors"
                        title="Archive"
                      >
                        <Archive className="h-4 w-4" />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
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

      {/* Add / Edit Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 bg-black/50 z-50 flex justify-center items-center p-4">
          <div className="bg-white w-full max-w-lg rounded-2xl shadow-2xl flex flex-col max-h-[90vh]">
            <div className="flex justify-between items-center px-6 py-4 border-b flex-shrink-0">
              <h2 className="text-lg font-bold text-gray-800">{editingCustomer ? 'Edit Customer' : 'New Customer'}</h2>
              <button onClick={() => setIsModalOpen(false)} className="text-gray-400 hover:text-gray-700 transition">
                <X className="h-5 w-5" />
              </button>
            </div>
            <form onSubmit={handleSubmit(onSubmit)} className="p-6 space-y-4 overflow-y-auto flex-1">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">First Name *</label>
                  <input {...register('firstName')} className={`input-field ${errors.firstName ? 'border-red-500' : ''}`} placeholder="John" />
                  {errors.firstName && <p className="text-xs text-red-500 mt-1">{errors.firstName.message}</p>}
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Last Name *</label>
                  <input {...register('lastName')} className={`input-field ${errors.lastName ? 'border-red-500' : ''}`} placeholder="Doe" />
                  {errors.lastName && <p className="text-xs text-red-500 mt-1">{errors.lastName.message}</p>}
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Email Address *</label>
                <input type="email" {...register('email')} className={`input-field ${errors.email ? 'border-red-500' : ''}`} placeholder="john@example.com" />
                {errors.email && <p className="text-xs text-red-500 mt-1">{errors.email.message}</p>}
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Phone Number *</label>
                <input {...register('phone')} className={`input-field ${errors.phone ? 'border-red-500' : ''}`} placeholder="+91 9876543210" />
                {errors.phone && <p className="text-xs text-red-500 mt-1">{errors.phone.message}</p>}
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Status</label>
                  <select {...register('status')} className="input-field">
                    <option value="NEW">New</option>
                    <option value="INTERESTED">Interested</option>
                    <option value="BOOKED">Booked</option>
                    <option value="LOST">Lost</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Next Follow-up</label>
                  <input type="date" {...register('nextFollowUpDate')} className="input-field" />
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Assign To Employee</label>
                <select {...register('assignedEmployeeId')} className="input-field">
                  <option value="">— Not assigned —</option>
                  {employees.map(emp => (
                    <option key={emp.id} value={emp.id.toString()}>{emp.name}</option>
                  ))}
                </select>
                {employees.length === 0 && (
                  <p className="text-xs text-gray-400 mt-1">No employees found. Register an employee account first.</p>
                )}
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Address</label>
                <input {...register('address')} className="input-field" placeholder="123 Main St, City" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Notes</label>
                <textarea {...register('notes')} rows={3} className="input-field resize-none" placeholder="Any notes about this customer..." />
              </div>
              <div className="flex justify-end gap-3 pt-2 border-t">
                <button type="button" onClick={() => setIsModalOpen(false)} className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition">
                  Cancel
                </button>
                <button type="submit" disabled={isSubmitting} className="px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700 transition disabled:opacity-50">
                  {isSubmitting ? 'Saving...' : editingCustomer ? 'Save Changes' : 'Add Customer'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Archive Confirmation */}
      {confirmArchive && (
        <div className="fixed inset-0 bg-black/50 z-50 flex justify-center items-center p-4">
          <div className="bg-white rounded-2xl shadow-2xl p-6 max-w-sm w-full text-center">
            <div className="w-12 h-12 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <Archive className="h-6 w-6 text-red-600" />
            </div>
            <h3 className="text-lg font-bold text-gray-800 mb-2">Archive Customer?</h3>
            <p className="text-sm text-gray-500 mb-6">
              <strong>{confirmArchive.firstName} {confirmArchive.lastName}</strong> will be archived and removed from active CRM view. This action can be undone from the database.
            </p>
            <div className="flex gap-3 justify-center">
              <button onClick={() => setConfirmArchive(null)} className="px-4 py-2 text-sm font-medium border border-gray-300 rounded-lg hover:bg-gray-50">Cancel</button>
              <button onClick={() => handleArchive(confirmArchive)} className="px-4 py-2 text-sm font-medium text-white bg-red-600 hover:bg-red-700 rounded-lg">Archive</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
