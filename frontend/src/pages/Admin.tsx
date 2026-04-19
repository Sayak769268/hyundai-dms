import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  Building2, Users, Receipt, TrendingUp, TrendingDown, 
  AlertTriangle, Navigation, Crown, Database, CheckCircle2
} from 'lucide-react';
import api from '../lib/api';

interface DealerRank {
  dealerId: number;
  dealerName: string;
  totalSales: number;
  totalRevenue: number;
}

interface AdminDashboard {
  totalDealers: number;
  totalUsers: number;
  globalSalesThisMonth: number;
  globalRevenueThisMonth: number;
  topDealer: DealerRank | null;
  worstDealer: DealerRank | null;
  dealerRankings: DealerRank[];
  globalAlerts: string[];
}

export default function Admin() {
  const [data, setData] = useState<AdminDashboard | null>(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    fetchAdminData();
  }, []);

  const fetchAdminData = async () => {
    try {
      const res = await api.get('/admin/dashboard');
      setData(res.data);
    } catch (error) {
      console.error('Failed to fetch admin dashboard', error);
    } finally {
      setLoading(false);
    }
  };

  const handleDealerClick = (dealerId: number) => {
    navigate(`/?dealerId=${dealerId}`);
  };

  if (loading || !data) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6 pb-12">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight">Global Administration</h1>
          <p className="text-gray-500 mt-1 text-sm">Monitor all dealers, sales, and system alerts system-wide.</p>
        </div>
      </div>

      {/* Primary Global Metrics */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 flex flex-col justify-between">
          <div className="flex items-start justify-between mb-4">
            <div className="p-3 bg-blue-600 rounded-xl text-white shadow-lg shadow-blue-200">
              <Building2 className="h-6 w-6" />
            </div>
          </div>
          <div>
            <p className="text-sm font-semibold text-gray-400 uppercase tracking-wider mb-1">Total Dealers</p>
            <h3 className="text-3xl font-extrabold text-gray-900">{data.totalDealers}</h3>
            <p className="text-xs font-bold text-gray-500 mt-1">Active Branches</p>
          </div>
        </div>

        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 flex flex-col justify-between">
          <div className="flex items-start justify-between mb-4">
            <div className="p-3 bg-indigo-600 rounded-xl text-white shadow-lg shadow-indigo-200">
              <Users className="h-6 w-6" />
            </div>
          </div>
          <div>
            <p className="text-sm font-semibold text-gray-400 uppercase tracking-wider mb-1">Total System Users</p>
            <h3 className="text-3xl font-extrabold text-gray-900">{data.totalUsers}</h3>
            <p className="text-xs font-bold text-gray-500 mt-1">Employees & Dealers</p>
          </div>
        </div>

        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 flex flex-col justify-between">
          <div className="flex items-start justify-between mb-4">
            <div className="p-3 bg-green-600 rounded-xl text-white shadow-lg shadow-green-200">
              <Receipt className="h-6 w-6" />
            </div>
          </div>
          <div>
            <p className="text-sm font-semibold text-gray-400 uppercase tracking-wider mb-1">Global Sales</p>
            <h3 className="text-3xl font-extrabold text-gray-900">{data.globalSalesThisMonth}</h3>
            <p className="text-xs font-bold text-gray-500 mt-1">Vehicles Sold This Month</p>
          </div>
        </div>

        <div className="bg-gradient-to-br from-gray-900 to-black rounded-2xl shadow-lg border border-gray-800 p-6 flex flex-col justify-between text-white relative overflow-hidden">
          <Database className="absolute -bottom-4 -right-4 h-32 w-32 opacity-10" />
          <div className="relative z-10">
            <p className="text-sm font-semibold text-gray-400 uppercase tracking-wider mb-1">Global Revenue</p>
            <h3 className="text-3xl font-extrabold text-white mb-1">
              ₹{(data.globalRevenueThisMonth / 10000000).toFixed(2)}Cr
            </h3>
            <p className="text-xs font-bold text-gray-400 mt-1">Total System Revenue</p>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        
        {/* Dealer Rankings */}
        <div className="lg:col-span-2 space-y-6">
          <div className="flex gap-6">
            {/* Top Dealer Card */}
            {data.topDealer && (
              <div className="flex-1 bg-gradient-to-br from-yellow-50 to-orange-50 rounded-2xl shadow-sm border border-yellow-200 p-6">
                <div className="flex items-center gap-2 mb-4">
                  <Crown className="h-6 w-6 text-yellow-500" />
                  <h2 className="text-lg font-bold text-yellow-900 tracking-tight">Top Performing Dealer</h2>
                </div>
                <h3 className="text-2xl font-extrabold text-gray-900 mb-2">{data.topDealer.dealerName}</h3>
                <div className="flex justify-between items-end border-t border-yellow-200/50 pt-4">
                   <div>
                     <p className="text-[10px] text-gray-400 font-bold uppercase tracking-wider">Revenue</p>
                     <p className="text-lg font-bold text-green-600">₹{(data.topDealer.totalRevenue / 100000).toFixed(2)}L</p>
                   </div>
                   <div className="text-right">
                     <p className="text-[10px] text-gray-400 font-bold uppercase tracking-wider">Units Sold</p>
                     <p className="text-lg font-bold text-blue-600">{data.topDealer.totalSales}</p>
                   </div>
                </div>
              </div>
            )}
            
            {/* Needs Improvement Card */}
            {data.worstDealer && data.worstDealer.dealerId !== data.topDealer?.dealerId && (
              <div className="flex-1 bg-gradient-to-br from-red-50 to-rose-50 rounded-2xl shadow-sm border border-red-200 p-6">
                <div className="flex items-center gap-2 mb-4">
                  <TrendingDown className="h-6 w-6 text-red-500" />
                  <h2 className="text-lg font-bold text-red-900 tracking-tight">Needs Improvement</h2>
                </div>
                <h3 className="text-2xl font-extrabold text-gray-900 mb-2">{data.worstDealer.dealerName}</h3>
                <div className="flex justify-between items-end border-t border-red-200/50 pt-4">
                   <div>
                     <p className="text-[10px] text-gray-400 font-bold uppercase tracking-wider">Revenue</p>
                     <p className="text-lg font-bold text-red-600">₹{(data.worstDealer.totalRevenue / 100000).toFixed(2)}L</p>
                   </div>
                   <div className="text-right">
                     <p className="text-[10px] text-gray-400 font-bold uppercase tracking-wider">Units Sold</p>
                     <p className="text-lg font-bold text-gray-700">{data.worstDealer.totalSales}</p>
                   </div>
                </div>
              </div>
            )}
          </div>

          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-x-auto">
             <div className="p-6 border-b border-gray-100 flex justify-between items-center bg-gray-50/50">
                <h2 className="text-lg font-bold text-gray-900 flex items-center gap-2">
                  <TrendingUp className="h-5 w-5 text-blue-500" /> Dealership Leaderboard
                </h2>
                <div className="text-[10px] font-bold text-gray-400 bg-gray-100 px-3 py-1 rounded-full uppercase tracking-widest">
                   Live Rankings
                </div>
             </div>
             <div className="overflow-x-auto">
               <table className="w-full text-left">
                 <thead className="bg-gray-50 border-b border-gray-100">
                   <tr>
                     <th className="px-6 py-4 text-xs font-bold text-gray-500 uppercase tracking-wider">Rank</th>
                     <th className="px-6 py-4 text-xs font-bold text-gray-500 uppercase tracking-wider">Dealership</th>
                     <th className="px-6 py-4 text-xs font-bold text-gray-500 uppercase tracking-wider">Total Sales</th>
                     <th className="px-6 py-4 text-xs font-bold text-gray-500 uppercase tracking-wider">Revenue</th>
                     <th className="px-6 py-4 text-xs font-bold text-gray-500 uppercase tracking-wider">Action</th>
                   </tr>
                 </thead>
                 <tbody className="divide-y divide-gray-50">
                   {data.dealerRankings.map((dealer, idx) => (
                     <tr 
                       key={dealer.dealerId} 
                       className="hover:bg-blue-50/50 transition-colors cursor-pointer group"
                       onClick={() => handleDealerClick(dealer.dealerId)}
                     >
                       <td className="px-6 py-4">
                         {idx === 0 ? (
                            <span className="inline-flex items-center justify-center h-8 w-8 rounded-full bg-yellow-100 text-yellow-700 font-extrabold text-sm shadow-sm ring-1 ring-yellow-200">1</span>
                         ) : idx === 1 ? (
                            <span className="inline-flex items-center justify-center h-8 w-8 rounded-full bg-slate-100 text-slate-700 font-extrabold text-sm shadow-sm ring-1 ring-slate-200">2</span>
                         ) : idx === 2 ? (
                            <span className="inline-flex items-center justify-center h-8 w-8 rounded-full bg-orange-50 text-orange-800 font-extrabold text-sm shadow-sm ring-1 ring-orange-100">3</span>
                         ) : (
                            <span className="inline-flex items-center justify-center h-8 w-8 rounded-full bg-gray-50 text-gray-500 font-bold text-sm">{idx + 1}</span>
                         )}
                       </td>
                       <td className="px-6 py-4 font-bold text-gray-900 group-hover:text-blue-600 transition-colors">{dealer.dealerName}</td>
                       <td className="px-6 py-4 text-gray-600 font-medium">{dealer.totalSales} Units</td>
                       <td className="px-6 py-4 font-bold text-green-600">₹{(dealer.totalRevenue / 100000).toLocaleString('en-IN')}L</td>
                       <td className="px-6 py-4">
                         <button
                           onClick={(e) => { e.stopPropagation(); handleDealerClick(dealer.dealerId); }}
                           className="flex items-center gap-1.5 text-blue-600 border border-blue-100 font-bold text-xs bg-blue-50 hover:bg-blue-600 hover:text-white px-4 py-2 rounded-xl transition-all shadow-sm"
                         >
                           View Data <Navigation className="h-3 w-3" />
                         </button>
                       </td>
                     </tr>
                   ))}
                 </tbody>
               </table>
             </div>
          </div>
        </div>

        {/* Global Alerts System */}
        <div className="space-y-6">
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
            <h2 className="text-lg font-bold text-gray-900 mb-4 flex items-center gap-2">
              <AlertTriangle className="h-5 w-5 text-red-500" /> Global Priority Alerts
            </h2>
            
            {data.globalAlerts.length === 0 ? (
               <div className="p-6 bg-gray-50 border border-dashed border-gray-200 rounded-xl text-center">
                 <CheckCircle2 className="h-8 w-8 text-green-500 mx-auto mb-2 opacity-50" />
                 <p className="text-sm font-bold text-gray-400">All systems optimal.</p>
                 <p className="text-xs text-gray-400">No critical alerts to display.</p>
               </div>
            ) : (
                <div className="space-y-4">
                  {data.globalAlerts.map((alert, i) => {
                    const [summary, details] = alert.split('|');
                    return (
                      <div key={i} className="flex flex-col gap-3 p-4 rounded-xl bg-red-50 border border-red-100 shadow-sm border-l-4 border-l-red-500">
                        <div className="flex items-center gap-2">
                          <AlertTriangle className="h-5 w-5 text-red-500 flex-shrink-0" />
                          <p className="text-sm text-red-900 font-bold">{summary}</p>
                        </div>
                        {details && (
                          <div className="pl-7 space-y-1">
                            {details.split(', ').map((item, idx) => (
                              <div key={idx} className="text-xs text-red-700 flex items-center gap-1.5">
                                <span className="h-1 w-1 bg-red-400 rounded-full" />
                                {item}
                              </div>
                            ))}
                          </div>
                        )}
                      </div>
                    );
                  })}
                </div>
            )}
          </div>
        </div>

      </div>
    </div>
  );
}
