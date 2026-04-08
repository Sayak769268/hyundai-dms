import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Users, Search, Activity, PauseCircle, PlayCircle, Eye } from 'lucide-react';
import api from '../lib/api';

interface Dealer {
  dealerId: number;
  dealerName: string;
  location: string;
  totalSales: number;
  totalRevenue: number;
  isActive: boolean;
}

export default function Dealers() {
  const [dealers, setDealers] = useState<Dealer[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    fetchDealers();
  }, []);

  const fetchDealers = async () => {
    try {
      setLoading(true);
      const res = await api.get('/admin/dashboard');
      setDealers(res.data.dealerRankings || []);
    } catch {
      // Error handling
    } finally {
      setLoading(false);
    }
  };

  const handleToggleStatus = async (id: number) => {
    try {
      await api.post(`/admin/dealer/${id}/toggle`);
      // Update local state instead of full refetch for better UX
      setDealers(prev => prev.map(d => d.dealerId === id ? { ...d, isActive: !d.isActive } : d));
    } catch {
      alert("Failed to toggle dealer status");
    }
  };

  const filteredDealers = dealers.filter(d => 
    d.dealerName.toLowerCase().includes(search.toLowerCase()) || 
    d.location.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Manage Dealers</h1>
          <p className="text-sm text-gray-500 mt-1">Global directory of all registered dealerships</p>
        </div>
      </div>

      {/* Filter / Search */}
      <div className="bg-white border border-gray-200 rounded-xl p-4 flex items-center">
        <div className="relative flex-1 max-w-sm">
          <Search className="h-5 w-5 absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
          <input
            type="text"
            placeholder="Search by name or location..."
            value={search}
            onChange={e => setSearch(e.target.value)}
            className="pl-10 pr-4 py-2 w-full border border-gray-300 rounded-lg text-sm focus:ring-blue-500 focus:border-blue-500 outline-none transition"
          />
        </div>
      </div>

      {/* Table */}
      <div className="bg-white border border-gray-200 rounded-xl shadow-sm overflow-x-auto">
        {loading ? (
          <div className="p-16 text-center text-gray-400">Loading dealers...</div>
        ) : filteredDealers.length === 0 ? (
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
                {['Dealer Name', 'Total Sales', 'Revenue', 'Status', 'Actions'].map(h => (
                  <th key={h} className="px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {filteredDealers.map(d => (
                <tr key={d.dealerId} className={`transition ${!d.isActive ? 'bg-gray-50/50 opacity-75' : 'hover:bg-gray-50'}`}>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-bold text-gray-900">
                    {d.dealerName}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-bold text-blue-700">
                    {d.totalSales} units
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-bold text-gray-900">
                    ₹{d.totalRevenue.toLocaleString('en-IN')}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`px-3 py-1 text-xs font-bold rounded-full border ${d.isActive ? 'bg-green-100 text-green-700 border-green-200' : 'bg-red-100 text-red-700 border-red-200'}`}>
                      {d.isActive ? 'Active' : 'Inactive'}
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
                        onClick={() => handleToggleStatus(d.dealerId)} 
                        className={`font-medium flex items-center gap-1.5 ${d.isActive ? 'text-orange-600 hover:text-orange-800' : 'text-green-600 hover:text-green-800'}`}
                      >
                        {d.isActive ? <PauseCircle className="h-4 w-4" /> : <PlayCircle className="h-4 w-4" />}
                        {d.isActive ? 'Deactivate' : 'Activate'}
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

    </div>
  );
}
