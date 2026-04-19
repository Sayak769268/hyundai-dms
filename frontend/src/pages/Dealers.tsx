import { useEffect, useState, useRef } from 'react';
import { createPortal } from 'react-dom';
import { useNavigate } from 'react-router-dom';
import { 
  Users, 
  Search, 
  PauseCircle, 
  PlayCircle, 
  Eye, 
  Filter, 
  ChevronDown 
} from 'lucide-react';
import api from '../lib/api';
import SortableHeader from '../components/SortableHeader';
import type { SortDir } from '../components/SortableHeader';
import { useGlobalShortcuts } from '../hooks/useKeyboardShortcuts';

interface Dealer {
  dealerId: number;
  dealerName: string;
  location: string;
  totalSales: number;
  totalRevenue: number;
  active?: boolean;
  isActive?: boolean;
}

export default function Dealers() {
  const [dealers, setDealers] = useState<Dealer[]>([]);
  const [loading, setLoading] = useState(true);
  const [filters, setFilters] = useState({
    name: '',
    status: '',
    minSales: '',
    maxSales: '',
    minRevenue: '',
    maxRevenue: ''
  });
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [confirmModal, setConfirmModal] = useState<{isOpen: boolean, dealerId: number | null, dealerName: string}>({ isOpen: false, dealerId: null, dealerName: '' });
  const navigate = useNavigate();

  const searchRef = useRef<HTMLInputElement>(null);
  const [sortField, setSortField] = useState('totalRevenue');
  const [sortDir, setSortDir] = useState<SortDir>('desc');

  const handleSort = (field: string, dir: SortDir) => {
    setSortField(field);
    setSortDir(dir);
  };

  useGlobalShortcuts({ onSearch: () => searchRef.current?.focus() });

  useEffect(() => {
    fetchDealers();
  }, [filters, page]);

  const fetchDealers = async () => {
    try {
      setLoading(true);
      const params = new URLSearchParams();
      if (filters.name) params.append('name', filters.name);
      if (filters.status) params.append('status', filters.status === 'ACTIVE' ? 'true' : 'false');
      if (filters.minSales) params.append('minSales', filters.minSales);
      if (filters.maxSales) params.append('maxSales', filters.maxSales);
      if (filters.minRevenue) params.append('minRevenue', filters.minRevenue);
      if (filters.maxRevenue) params.append('maxRevenue', filters.maxRevenue);
      params.append('page', page.toString());
      params.append('size', '10');
      
      const res = await api.get(`/admin/dealers?${params.toString()}`);
      setDealers(res.data.content || []);
      setTotalPages(res.data.totalPages || 0);
    } catch {
      // Error handling
    } finally {
      setLoading(false);
    }
  };

  const resetFilters = () => {
    setFilters({
      name: '',
      status: '',
      minSales: '',
      maxSales: '',
      minRevenue: '',
      maxRevenue: ''
    });
    setPage(0);
  };

  const attemptToggleStatus = (id: number, currentStatus: boolean, dealerName: string) => {
    if (currentStatus) {
      setConfirmModal({ isOpen: true, dealerId: id, dealerName });
    } else {
      executeToggle(id);
    }
  };

  const executeToggle = async (id: number) => {
    try {
      await api.post(`/admin/dealer/${id}/toggle`);
      setDealers(prev => prev.map(d => d.dealerId === id ? { ...d, active: !(d.active ?? d.isActive), isActive: !(d.active ?? d.isActive) } : d));
      setConfirmModal({ isOpen: false, dealerId: null, dealerName: '' });
    } catch {
      alert("Failed to toggle dealer status");
    }
  };

  // Client-side sorting for computed fields
  const displayDealers = [...dealers].sort((a, b) => {
    if (!sortDir || !sortField) return 0;
    const aVal = (a as any)[sortField];
    const bVal = (b as any)[sortField];
    
    if (typeof aVal === 'string' && typeof bVal === 'string') {
      return sortDir === 'asc' ? aVal.localeCompare(bVal) : bVal.localeCompare(aVal);
    }
    
    return sortDir === 'asc' ? (aVal > bVal ? 1 : -1) : (bVal > aVal ? 1 : -1);
  });

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Manage Dealers</h1>
          <p className="text-sm text-gray-500 mt-1">Global directory of all registered dealerships</p>
        </div>
      </div>

      {/* Filter Row */}
      <div className="bg-white border border-gray-100 rounded-2xl shadow-sm overflow-hidden">
        <div className="px-6 py-4 border-b border-gray-50 bg-gray-50/30 flex items-center gap-2">
            <Filter className="h-4 w-4 text-blue-600" />
            <span className="text-sm font-bold text-gray-700">Filter Controls</span>
        </div>
        
        <div className="p-6 space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-6 gap-4">
            {/* Name Filter */}
            <div className="space-y-1.5">
                <label className="text-[10px] font-bold text-gray-400 uppercase tracking-wider">Dealer Name</label>
                <div className="relative">
                <Search className="h-4 w-4 absolute left-3 top-1/2 -translate-y-1/2 text-gray-300" />
                <input
                    type="text"
                    placeholder="Search name..."
                    value={filters.name}
                    onChange={e => setFilters(f => ({...f, name: e.target.value}))}
                    className="pl-9 pr-3 py-2 w-full border border-gray-200 rounded-xl text-xs focus:ring-4 focus:ring-blue-500/5 focus:border-blue-500 outline-none transition-all"
                />
                </div>
            </div>

            {/* Status Filter */}
            <div className="space-y-1.5">
                <label className="text-[10px] font-bold text-gray-400 uppercase tracking-wider">Status</label>
                <div className="relative">
                    <select
                        value={filters.status}
                        onChange={e => setFilters(f => ({...f, status: e.target.value}))}
                        className="w-full px-3 py-2 border border-gray-200 rounded-xl text-xs focus:ring-4 focus:ring-blue-500/5 focus:border-blue-500 outline-none transition-all appearance-none bg-white pr-8"
                    >
                        <option value="">All Statuses</option>
                        <option value="ACTIVE">Active</option>
                        <option value="INACTIVE">Inactive</option>
                    </select>
                    <ChevronDown className="h-3.5 w-3.5 absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 pointer-events-none" />
                </div>
            </div>

            {/* Sales Range */}
            <div className="space-y-1.5 md:col-span-2">
                <label className="text-[10px] font-bold text-gray-400 uppercase tracking-wider">Total Sales Range</label>
                <div className="flex items-center gap-2">
                    <div className="relative w-full">
                        <input
                            type="number"
                            placeholder="Min Units"
                            value={filters.minSales}
                            onChange={e => setFilters(f => ({...f, minSales: e.target.value}))}
                            className="w-full px-3 py-2 border border-gray-200 rounded-xl text-xs focus:ring-4 focus:ring-blue-500/5 focus:border-blue-500 outline-none transition-all"
                        />
                    </div>
                    <span className="text-gray-300 font-bold">—</span>
                    <div className="relative w-full">
                        <input
                            type="number"
                            placeholder="Max Units"
                            value={filters.maxSales}
                            onChange={e => setFilters(f => ({...f, maxSales: e.target.value}))}
                            className="w-full px-3 py-2 border border-gray-200 rounded-xl text-xs focus:ring-4 focus:ring-blue-500/5 focus:border-blue-500 outline-none transition-all"
                        />
                    </div>
                </div>
            </div>

            {/* Revenue Range */}
            <div className="space-y-1.5 md:col-span-2">
                <label className="text-[10px] font-bold text-gray-400 uppercase tracking-wider">Revenue Range</label>
                <div className="flex items-center gap-2">
                    <div className="relative w-full">
                        <span className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 text-[10px] font-bold">₹</span>
                        <input
                            type="number"
                            placeholder="Min"
                            value={filters.minRevenue}
                            onChange={e => setFilters(f => ({...f, minRevenue: e.target.value}))}
                            className="w-full pl-6 pr-3 py-2 border border-gray-200 rounded-xl text-xs focus:ring-4 focus:ring-blue-500/5 focus:border-blue-500 outline-none transition-all"
                        />
                    </div>
                    <span className="text-gray-300 font-bold">—</span>
                    <div className="relative w-full">
                        <span className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 text-[10px] font-bold">₹</span>
                        <input
                            type="number"
                            placeholder="Max"
                            value={filters.maxRevenue}
                            onChange={e => setFilters(f => ({...f, maxRevenue: e.target.value}))}
                            className="w-full pl-6 pr-3 py-2 border border-gray-200 rounded-xl text-xs focus:ring-4 focus:ring-blue-500/5 focus:border-blue-500 outline-none transition-all"
                        />
                    </div>
                </div>
            </div>
            </div>

            <div className="flex justify-end pt-2 border-t border-gray-50">
            <button
                onClick={resetFilters}
                className="text-xs font-extrabold text-blue-600 hover:text-blue-700 px-4 py-2 hover:bg-blue-50 rounded-xl transition-all flex items-center gap-1.5"
            >
                Clear All Criteria
            </button>
            </div>
        </div>
      </div>

      {/* Table */}
      <div className="bg-white border border-gray-200 rounded-xl shadow-sm overflow-x-auto">
        {loading ? (
          <div className="p-16 text-center text-gray-400">Loading dealers...</div>
        ) : displayDealers.length === 0 ? (
          <div className="p-16 flex justify-center">
            <div className="text-center">
              <Users className="h-10 w-10 text-gray-300 mx-auto mb-3" />
              <p className="text-gray-500 font-medium">No dealers found.</p>
            </div>
          </div>
        ) : (
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <SortableHeader label="Dealer Name" field="dealerName" currentSort={sortField} currentDir={sortDir} onSort={handleSort} />
                <SortableHeader label="Total Sales" field="totalSales" currentSort={sortField} currentDir={sortDir} onSort={handleSort} />
                <SortableHeader label="Revenue" field="totalRevenue" currentSort={sortField} currentDir={sortDir} onSort={handleSort} />
                <SortableHeader label="Status" field="active" currentSort={sortField} currentDir={sortDir} onSort={handleSort} />
                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {displayDealers.map(d => (
                <tr key={d.dealerId} className={`transition ${!(d.active ?? d.isActive) ? 'bg-gray-50/50 opacity-75' : 'hover:bg-gray-50'}`}>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-bold text-gray-900">
                    {d.dealerName}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-bold text-blue-700">
                    {d.totalSales || 0} units
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-bold text-gray-900">
                    ₹{(d.totalRevenue || 0).toLocaleString('en-IN')}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`px-3 py-1 text-xs font-bold rounded-full border ${(d.active ?? d.isActive) ? 'bg-green-100 text-green-700 border-green-200' : 'bg-red-100 text-red-700 border-red-200'}`}>
                      {(d.active ?? d.isActive) ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm">
                    <div className="flex items-center gap-3">
                      <button 
                        onClick={() => navigate(`/?dealerId=${d.dealerId}`)} 
                        className="text-blue-600 hover:text-blue-800 font-medium flex items-center gap-1.5"
                      >
                        <Eye className="h-4 w-4" /> View Dashboard
                      </button>
                      
                      <button 
                        onClick={() => attemptToggleStatus(d.dealerId, !!(d.active ?? d.isActive), d.dealerName)} 
                        className={`font-medium flex items-center gap-1.5 ${(d.active ?? d.isActive) ? 'text-orange-600 hover:text-orange-800' : 'text-green-600 hover:text-green-800'}`}
                      >
                        {(d.active ?? d.isActive) ? <PauseCircle className="h-4 w-4" /> : <PlayCircle className="h-4 w-4" />}
                        {(d.active ?? d.isActive) ? 'Deactivate' : 'Activate'}
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* Pagination Controls */}
      {totalPages > 1 && (
        <div className="flex justify-center items-center gap-4 mt-8">
          <button
            disabled={page === 0}
            onClick={() => setPage(p => p - 1)}
            className="px-4 py-2 bg-white border border-gray-200 rounded-xl text-sm font-bold text-gray-600 disabled:opacity-50 hover:bg-gray-50 transition"
          >
            Previous
          </button>
          <span className="text-sm font-bold text-gray-500">
            Page {page + 1} of {totalPages}
          </span>
          <button
            disabled={page >= totalPages - 1}
            onClick={() => setPage(p => p + 1)}
            className="px-4 py-2 bg-white border border-gray-200 rounded-xl text-sm font-bold text-gray-600 disabled:opacity-50 hover:bg-gray-50 transition"
          >
            Next
          </button>
        </div>
      )}

      {/* Confirmation Modal */}
      {confirmModal.isOpen && createPortal(
        <div className="fixed inset-0 bg-black/60 z-[9999] flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-2xl max-w-sm w-full p-6 text-center transform transition-all scale-100 opacity-100">
             <div className="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4">
               <PauseCircle className="h-8 w-8 text-red-600" />
             </div>
             <h3 className="text-xl font-bold text-gray-900 mb-2">Deactivate Dealer?</h3>
             <p className="text-gray-500 mb-6">Are you sure you want to deactivate <span className="font-semibold text-gray-800">{confirmModal.dealerName}</span>?</p>
             <div className="flex gap-3 w-full">
               <button onClick={() => setConfirmModal({ isOpen: false, dealerId: null, dealerName: '' })} className="flex-1 px-4 py-2.5 bg-gray-100 text-gray-700 font-semibold rounded-lg hover:bg-gray-200 transition">Cancel</button>
               <button onClick={() => confirmModal.dealerId && executeToggle(confirmModal.dealerId)} className="flex-1 px-4 py-2.5 bg-red-600 text-white font-semibold rounded-lg hover:bg-red-700 shadow-md shadow-red-200 transition">Yes, Deactivate</button>
             </div>
          </div>
        </div>,
        document.body
      )}

    </div>
  );
}
