import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  Users, Car, ReceiptText, DollarSign, 
  TrendingUp, TrendingDown, AlertTriangle, 
  Clock, CheckCircle, PackageSearch, 
  CalendarClock, Activity, ArrowRight,
  Target
} from 'lucide-react';
import { 
  BarChart, Bar, XAxis, YAxis, CartesianGrid, 
  Tooltip, ResponsiveContainer, Cell 
} from 'recharts';
import api from '../lib/api';
import Admin from './Admin';

interface DashboardStats {
  totalCustomers: number;
  vehiclesInStock: number;
  salesThisMonth: number;
  revenueThisMonth: number;
  salesGrowthLabel: string;
  revenueGrowthLabel: string;
  salesGrowthPositive: boolean;
  revenueGrowthPositive: boolean;
  lowStockCount: number;
  pendingOrdersCount: number;
  pendingFollowUpsCount: number;
  topSellingModel: string;
  topModelSales: number;
  todayOrders: number;
  todayRevenue: number;
  recentOrders: {
    id: number;
    customerName: string;
    vehicleName: string;
    finalAmount: number;
    status: string;
    createdAt: string;
  }[];
  chartData: { label: string; value: number }[];
}

const colorMap: Record<string, string> = {
  PENDING: 'text-yellow-600 bg-yellow-100',
  CONFIRMED: 'text-blue-600 bg-blue-100',
  INVOICED: 'text-green-600 bg-green-100',
  CANCELLED: 'text-red-600 bg-red-100',
};

export default function Dashboard() {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [isImpersonating, setIsImpersonating] = useState(false);
  const navigate = useNavigate();

  const storedUser = JSON.parse(localStorage.getItem('user') || '{}');
  const isAdmin = storedUser.roles?.includes('ROLE_ADMIN');

  useEffect(() => {
    fetchDashboard();
  }, [window.location.search]);

  const fetchDashboard = async () => {
    try {
      const searchParams = new URLSearchParams(window.location.search);
      const dealerId = searchParams.get('dealerId');
      setIsImpersonating(!!dealerId);
      
      const url = dealerId ? `/dashboard/stats?dealerId=${dealerId}` : '/dashboard/stats';
      const res = await api.get(url);
      setStats(res.data);
    } catch { /* Handle error */ } finally { setLoading(false); }
  };

  if (isAdmin && !isImpersonating) {
    return <Admin />;
  }

  if (loading || !stats) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  const mainCards = [
    { 
      name: 'Total CRM Customers', 
      value: stats.totalCustomers, 
      sub: 'Active in CRM',
      icon: Users, 
      color: 'bg-blue-600', 
      btn: true,
      path: '/customers'
    },
    { 
      name: 'Vehicles in Stock', 
      value: stats.vehiclesInStock, 
      sub: stats.lowStockCount > 0 ? `${stats.lowStockCount} items need attention` : 'Stock is healthy',
      subColor: stats.lowStockCount > 0 ? 'text-red-600 font-bold' : 'text-gray-500',
      icon: Car, 
      color: 'bg-indigo-600',
      btn: true,
      path: '/inventory'
    },
    { 
      name: 'Sales This Month', 
      value: stats.salesThisMonth, 
      sub: stats.salesGrowthLabel,
      subColor: stats.salesGrowthPositive ? 'text-green-600' : 'text-red-600',
      icon: ReceiptText, 
      color: 'bg-green-600',
      btn: true,
      path: '/sales'
    },
    { 
      name: 'Revenue (Month)', 
      value: `₹${(stats.revenueThisMonth / 10000000).toFixed(2)}Cr`, 
      sub: stats.revenueGrowthLabel,
      subColor: stats.revenueGrowthPositive ? 'text-green-600' : 'text-red-600',
      icon: DollarSign, 
      color: 'bg-purple-600',
      btn: false
    },
  ];

  return (
    <div className="space-y-6 pb-12">
      {/* Header with quick snapshot */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight">Business Snapshot</h1>
          <p className="text-gray-500 mt-1">Ready for a great day? Here is your real-time Hyundai DMS overview.</p>
        </div>
        <div className="flex items-center gap-3 bg-white p-3 rounded-2xl shadow-sm border border-gray-100">
           <div className="p-2 bg-blue-50 rounded-lg"><Activity className="h-5 w-5 text-blue-600" /></div>
           <div className="pr-4 border-r border-gray-100">
             <p className="text-[10px] uppercase font-bold text-gray-400">Today's Orders</p>
             <p className="font-bold text-gray-900">{stats.todayOrders}</p>
           </div>
           <div className="px-2">
             <p className="text-[10px] uppercase font-bold text-gray-400">Today's Rev</p>
             <p className="font-bold text-gray-900">₹{stats.todayRevenue.toLocaleString('en-IN')}</p>
           </div>
        </div>
      </div>
      
      {/* Primary Metrics Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {mainCards.map((stat) => (
          <div 
            key={stat.name} 
            onClick={() => stat.btn && navigate(stat.path!)}
            className={`bg-white rounded-2xl shadow-sm border border-gray-100 p-6 flex flex-col justify-between group transition-all duration-300 ${stat.btn ? 'hover:shadow-md hover:-translate-y-1 cursor-pointer' : ''}`}
          >
            <div className="flex justify-between items-start mb-4">
              <div className={`p-3 rounded-xl flex-shrink-0 text-white shadow-lg ${stat.color} group-hover:scale-110 transition-transform`}>
                <stat.icon className="h-6 w-6" />
              </div>
              {stat.btn && <ArrowRight className="h-4 w-4 text-gray-200 group-hover:text-gray-400 transition-colors" />}
            </div>
            <div>
              <p className="text-sm font-semibold text-gray-400 uppercase tracking-wider mb-1">{stat.name}</p>
              <h3 className="text-3xl font-extrabold text-gray-900 mb-1">{stat.value.toLocaleString()}</h3>
              <div className="flex items-center gap-1.5">
                {(stat.name.includes("Month") && stats.salesGrowthPositive) && <TrendingUp className="h-3.5 w-3.5 text-green-600" />}
                {(stat.name.includes("Month") && !stats.salesGrowthPositive) && <TrendingDown className="h-3.5 w-3.5 text-red-600" />}
                <p className={`text-xs font-bold ${stat.subColor || 'text-gray-500'}`}>{stat.sub}</p>
              </div>
            </div>
          </div>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Column: Charts and Activity */}
        <div className="lg:col-span-2 space-y-6">
          
          {/* Main Chart */}
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
            <div className="flex justify-between items-center mb-6">
              <h2 className="text-lg font-bold text-gray-900 flex items-center gap-2">
                <ReceiptText className="h-5 w-5 text-blue-500" /> Sales Trend (Last 6 Months)
              </h2>
            </div>
            <div className="h-[280px] w-full">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={stats.chartData} barSize={40}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                  <XAxis dataKey="label" axisLine={false} tickLine={false} tick={{fill: '#94a3b8', fontSize: 12}} dy={10} />
                  <YAxis axisLine={false} tickLine={false} tick={{fill: '#94a3b8', fontSize: 12}} />
                  <Tooltip 
                    cursor={{fill: '#f8fafc'}}
                    contentStyle={{borderRadius: '12px', border: 'none', boxShadow: '0 4px 12px rgba(0,0,0,0.1)'}}
                  />
                  <Bar dataKey="value" radius={[6, 6, 0, 0]}>
                    {stats.chartData.map((_entry, index) => (
                      <Cell key={`cell-${index}`} fill={index === stats.chartData.length - 1 ? '#2563eb' : '#cbd5e1'} />
                    ))}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>

          {/* Recent Orders Table */}
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
            <div className="p-6 border-b border-gray-100 flex justify-between items-center bg-gray-50/50">
              <h2 className="text-lg font-bold text-gray-900 flex items-center gap-2">
                <Clock className="h-5 w-5 text-indigo-500" /> Recent Sales Orders
              </h2>
              <button 
                onClick={() => navigate('/sales')}
                className="text-xs font-bold text-blue-600 hover:text-blue-800 transition"
              >
                View All Orders
              </button>
            </div>
            <div className="overflow-x-auto">
              <table className="w-full text-left">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-[10px] uppercase font-bold text-gray-400">Order ID</th>
                    <th className="px-6 py-3 text-[10px] uppercase font-bold text-gray-400">Customer</th>
                    <th className="px-6 py-3 text-[10px] uppercase font-bold text-gray-400">Vehicle</th>
                    <th className="px-6 py-3 text-[10px] uppercase font-bold text-gray-400">Status</th>
                    <th className="px-6 py-3 text-[10px] uppercase font-bold text-gray-400 text-right">Amount</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-50">
                  {stats.recentOrders.length === 0 ? (
                    <tr><td colSpan={5} className="p-10 text-center text-gray-400">No orders yet.</td></tr>
                  ) : (
                    stats.recentOrders.map((o) => (
                      <tr key={o.id} className="hover:bg-blue-50/30 transition">
                        <td className="px-6 py-4 text-sm font-bold text-gray-700">ORD-{o.id}</td>
                        <td className="px-6 py-4 text-sm font-medium text-gray-900">{o.customerName}</td>
                        <td className="px-6 py-4 text-sm text-gray-500">{o.vehicleName}</td>
                        <td className="px-6 py-4">
                          <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold uppercase ${colorMap[o.status]}`}>
                            {o.status}
                          </span>
                        </td>
                        <td className="px-6 py-4 text-sm font-bold text-gray-900 text-right">
                          ₹{o.finalAmount.toLocaleString('en-IN')}
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </div>

        {/* Right Column: Alerts and Actions */}
        <div className="space-y-6">
          
          {/* Critical Alerts */}
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
            <h2 className="text-lg font-bold text-gray-900 mb-4 flex items-center gap-2">
              <AlertTriangle className="h-5 w-5 text-red-500" /> Critical Attention
            </h2>
            <div className="space-y-4">
              {stats.lowStockCount > 0 ? (
                <div onClick={() => navigate('/inventory')} className="flex items-start gap-4 p-4 rounded-xl bg-red-50 border border-red-100 cursor-pointer hover:bg-red-100 transition group">
                  <div className="p-2 bg-red-200 rounded-lg text-red-600 group-hover:bg-red-600 group-hover:text-white transition-colors"><PackageSearch className="h-5 w-5" /></div>
                  <div>
                    <h4 className="text-sm font-bold text-red-900">Inventory Shortage</h4>
                    <p className="text-xs text-red-700 mt-0.5">{stats.lowStockCount} vehicles are out or low on stock.</p>
                  </div>
                </div>
              ) : (
                <div className="flex items-start gap-4 p-4 rounded-xl bg-green-50 border border-green-100">
                  <div className="p-2 bg-green-200 rounded-lg text-green-600"><CheckCircle className="h-5 w-5" /></div>
                  <div>
                    <h4 className="text-sm font-bold text-green-900">Stock Healthy</h4>
                    <p className="text-xs text-green-700 mt-0.5">All inventory items meet minimum stock levels.</p>
                  </div>
                </div>
              )}

              {stats.pendingOrdersCount > 0 && (
                <div onClick={() => navigate('/sales')} className="flex items-start gap-4 p-4 rounded-xl bg-yellow-50 border border-yellow-100 cursor-pointer hover:bg-yellow-100 transition group">
                  <div className="p-2 bg-yellow-200 rounded-lg text-yellow-600 group-hover:bg-yellow-600 group-hover:text-white transition-colors"><Clock className="h-5 w-5" /></div>
                  <div>
                    <h4 className="text-sm font-bold text-yellow-900">Pending Orders</h4>
                    <p className="text-xs text-yellow-700 mt-0.5">{stats.pendingOrdersCount} orders waiting for confirmation.</p>
                  </div>
                </div>
              )}
            </div>
          </div>

          {/* Top Model Showcase */}
          <div className="bg-gradient-to-br from-blue-600 to-indigo-800 rounded-2xl shadow-lg p-6 text-white overflow-hidden relative">
            <Target className="absolute -bottom-6 -right-6 h-32 w-32 opacity-15 rotate-12" />
            <span className="bg-white/20 text-white text-[10px] font-extrabold uppercase px-2 py-1 rounded-md mb-2 inline-block">Bestseller</span>
            <h3 className="text-2xl font-extrabold mb-1">{stats.topSellingModel}</h3>
            <p className="text-blue-100 text-sm opacity-80 mb-4">Most popular model this period.</p>
            <div className="bg-white/10 p-4 rounded-xl border border-white/10 backdrop-blur-sm">
               <p className="text-2xl font-bold">{stats.topModelSales} <span className="text-sm font-medium opacity-70">Sales Total</span></p>
            </div>
          </div>

          {/* Pending Actions CRM */}
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
            <h2 className="text-lg font-bold text-gray-900 mb-4 flex items-center gap-2">
              <CalendarClock className="h-5 w-5 text-purple-500" /> Pending CRM Actions
            </h2>
            {stats.pendingFollowUpsCount > 0 || stats.totalCustomers > 0 ? (
               <div onClick={() => navigate('/customers')} className="p-4 bg-gray-50 border border-gray-100 rounded-xl hover:border-blue-300 transition-colors cursor-pointer">
                  <p className="text-sm font-bold text-gray-700">Customer Pipeline</p>
                  <p className="text-xs text-gray-500 mt-1">Review your {stats.totalCustomers} active customer records and follow up scheduled today.</p>
               </div>
            ) : (
               <p className="text-sm text-gray-400 text-center py-4">No CRM actions needed.</p>
            )}
          </div>

        </div>
      </div>
    </div>
  );
}
