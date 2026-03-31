import { useEffect, useState } from 'react';
import { ShieldAlert, Clock, FileText, Search } from 'lucide-react';
import api from '../lib/api';
import Select from 'react-select';
import Pagination from '../components/Pagination';

interface AuditLog {
  id: number;
  username: string;
  dealerId: number | null;
  action: string;
  entityType: string;
  entityId: number | null;
  description: string;
  createdAt: string;
}

const ACTION_COLORS: Record<string, string> = {
  CREATE: 'bg-green-100 text-green-700 border-green-200',
  UPDATE: 'bg-blue-100 text-blue-700 border-blue-200',
  DELETE: 'bg-red-100 text-red-700 border-red-200',
  LOGIN: 'bg-purple-100 text-purple-700 border-purple-200',
  FAILED_LOGIN: 'bg-orange-100 text-orange-700 border-orange-200',
  READ: 'bg-gray-100 text-gray-600 border-gray-200',
};

const selectStyles = {
  control: (base: any) => ({
    ...base, minHeight: '38px', borderColor: '#d1d5db', borderRadius: '0.5rem', boxShadow: 'none',
    '&:hover': { borderColor: '#3b82f6' },
  }),
};

export default function AuditLogs() {
  const [logs, setLogs] = useState<AuditLog[]>([]);
  const [loading, setLoading] = useState(true);
  
  // Pagination
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const pageSize = 50;

  // Filters
  const [dealerOptions, setDealerOptions] = useState<any[]>([]);
  const [selectedDealer, setSelectedDealer] = useState<any>(null);
  const [actionType, setActionType] = useState<string>('');
  const [search, setSearch] = useState<string>('');

  useEffect(() => {
    loadDealers();
  }, []);

  useEffect(() => {
    const timer = setTimeout(() => {
      fetchLogs();
    }, 200);
    return () => clearTimeout(timer);
  }, [selectedDealer, actionType, search, page]);

  const loadDealers = async () => {
    try {
      const res = await api.get('/admin/dashboard');
      const dealersData = res.data.dealerRankings || [];
      setDealerOptions(dealersData.map((d: any) => ({
        value: d.dealerId,
        label: d.dealerName
      })));
    } catch { }
  };

  const fetchLogs = async () => {
    setLoading(true);
    try {
      let url = `/audit?page=${page}&size=${pageSize}`;
      if (selectedDealer) url += `&dealerId=${selectedDealer.value}`;
      if (actionType) url += `&actionType=${actionType}`;
      if (search) url += `&search=${encodeURIComponent(search)}`;
      
      const res = await api.get(url);
      setLogs(res.data.content || []);
      setTotalPages(res.data.totalPages || 0);
      setTotalElements(res.data.totalElements || 0);
    } catch {
      // ignore
    } finally {
      setLoading(false);
    }
  };

  const handleFilterChange = (setter: any, val: any) => {
    setter(val);
    setPage(0);
  };

  return (
    <div className="space-y-6 max-w-6xl mx-auto">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-gray-900 flex items-center gap-3">
            <ShieldAlert className="h-8 w-8 text-blue-600" /> System Audit Logs
          </h1>
          <p className="text-sm text-gray-500 mt-1">Track "Who did what and when" across the platform.</p>
        </div>
      </div>

      <div className="bg-white border border-gray-200 rounded-xl p-4 flex flex-wrap gap-4 items-center shadow-sm">
        <div className="flex-1 min-w-[200px]">
          <label className="block text-xs font-bold text-gray-500 uppercase mb-1">Search Logs</label>
          <div className="relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400" />
            <input 
              type="text"
              placeholder="Search by keyword..."
              className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg text-sm focus:ring-blue-500 focus:border-blue-500"
              value={search}
              onChange={(e) => handleFilterChange(setSearch, e.target.value)}
            />
          </div>
        </div>
        <div className="w-64 z-50">
          <label className="block text-xs font-bold text-gray-500 uppercase mb-1">Filter By Dealer</label>
          <Select
            options={dealerOptions}
            isClearable
            styles={selectStyles}
            placeholder="All Dealers"
            onChange={opt => handleFilterChange(setSelectedDealer, opt)}
            value={selectedDealer}
          />
        </div>
        <div className="w-48">
          <label className="block text-xs font-bold text-gray-500 uppercase mb-1">Filter By Action</label>
          <select 
            className="w-full border border-gray-300 rounded-lg text-sm py-2 px-3 focus:ring-blue-500 focus:border-blue-500 transition"
            value={actionType}
            onChange={e => handleFilterChange(setActionType, e.target.value)}
          >
            <option value="">All Actions</option>
            <option value="CREATE">Create</option>
            <option value="UPDATE">Update</option>
            <option value="DELETE">Delete</option>
            <option value="LOGIN">Login</option>
            <option value="FAILED_LOGIN">Failed Login</option>
            <option value="READ">Read / View</option>
          </select>
        </div>
        <div className="ml-auto">
          <button onClick={fetchLogs} className="bg-gray-100 hover:bg-gray-200 text-gray-700 px-4 py-2 mt-5 rounded-lg text-sm font-semibold transition">
            Refresh Logs
          </button>
        </div>
      </div>

      <div className="bg-white border border-gray-200 rounded-xl shadow-sm overflow-hidden text-black">
        {loading ? (
          <div className="p-16 text-center text-gray-400">Loading audit trail...</div>
        ) : logs.length === 0 ? (
          <div className="p-16 flex flex-col items-center justify-center">
             <FileText className="h-10 w-10 text-gray-300 mb-3" />
             <p className="text-gray-500 font-medium">No log entries found.</p>
             <p className="text-gray-400 text-sm">Try adjusting your filters or search terms.</p>
          </div>
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Date & Time</th>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">User</th>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Action</th>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Entity</th>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Description</th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {logs.map((log) => {
                    const bg = ACTION_COLORS[log.action] || 'bg-gray-100 text-gray-700 border-gray-200';
                    return (
                      <tr key={log.id} className="hover:bg-gray-50 transition duration-150 ease-in-out">
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 font-normal">
                          <div className="flex items-center">
                            <Clock className="h-4 w-4 mr-2 text-gray-400 font-normal" />
                            {new Date(log.createdAt).toLocaleString('en-IN', {
                              year: 'numeric', month: 'short', day: 'numeric',
                              hour: '2-digit', minute: '2-digit'
                            })}
                          </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="flex items-center">
                            <div className="flex-shrink-0 h-8 w-8 bg-blue-100 rounded-full flex items-center justify-center">
                              <span className="text-blue-700 font-bold text-sm">{log.username.charAt(0).toUpperCase()}</span>
                            </div>
                            <div className="ml-3">
                              <p className="text-sm font-medium text-gray-900">{log.username}</p>
                              {log.dealerId && (
                                <p className="text-xs text-gray-500">
                                  {dealerOptions.find((d: any) => d.value === log.dealerId)?.label || `Dealer #${log.dealerId}`}
                                </p>
                              )}
                            </div>
                          </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <span className={`px-2.5 py-1 inline-flex text-xs leading-5 font-semibold rounded-full border ${bg}`}>
                            {log.action}
                          </span>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 font-bold">
                          {log.entityType}
                        </td>
                        <td className="px-6 py-4 text-sm text-gray-500 max-w-md truncate font-normal" title={log.description}>
                          {log.description || <span className="text-gray-400 italic">No additional details</span>}
                        </td>
                      </tr>
                    );
                  })}
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
    </div>
  );
}
