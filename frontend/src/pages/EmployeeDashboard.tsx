import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Users, ReceiptText, Clock, CheckCircle, TrendingUp, ArrowRight, Car } from 'lucide-react';
import api from '../lib/api';

interface EmpStats {
  myCustomers: number;
  myOrders: number;
  pendingOrders: number;
  invoicedOrders: number;
  recentOrders: {
    id: number;
    customerName: string;
    vehicleName: string;
    finalAmount: number;
    status: string;
    createdAt: string;
  }[];
}

const STATUS_COLORS: Record<string, string> = {
  PENDING:   'bg-yellow-100 text-yellow-700',
  CONFIRMED: 'bg-blue-100 text-blue-700',
  INVOICED:  'bg-green-100 text-green-700',
  CANCELLED: 'bg-red-100 text-red-700',
};

export default function EmployeeDashboard() {
  const [stats, setStats] = useState<EmpStats | null>(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem('user') || '{}');

  useEffect(() => {
    fetchStats();
  }, []);

  const fetchStats = async () => {
    try {
      const [custRes, salesRes] = await Promise.all([
        api.get('/customers?page=0&size=1'),
        api.get('/sales?page=0&size=5'),
      ]);

      setStats({
        myCustomers: custRes.data.totalElements || 0,
        myOrders: salesRes.data.totalElements || 0,
        pendingOrders: (salesRes.data.content || []).filter((o: any) => o.status === 'PENDING').length,
        invoicedOrders: (salesRes.data.content || []).filter((o: any) => o.status === 'INVOICED').length,
        recentOrders: salesRes.data.content || [],
      });
    } catch {
      setStats({ myCustomers: 0, myOrders: 0, pendingOrders: 0, invoicedOrders: 0, recentOrders: [] });
    } finally {
      setLoading(false);
    }
  };

  if (loading) return (
    <div className="flex items-center justify-center min-h-[400px]">
      <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-blue-600" />
    </div>
  );

  return (
    <div className="space-y-6 pb-12">
      <div>
        <h1 className="text-3xl font-extrabold text-gray-900">My Dashboard</h1>
        <p className="text-gray-500 mt-1 text-sm">Welcome back, {user.username}. Here's your activity overview.</p>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        {[
          { label: 'My Customers', value: stats?.myCustomers, icon: Users, color: 'bg-blue-600', path: '/customers' },
          { label: 'My Orders', value: stats?.myOrders, icon: ReceiptText, color: 'bg-green-600', path: '/sales' },
          { label: 'Pending Orders', value: stats?.pendingOrders, icon: Clock, color: 'bg-yellow-500', path: '/sales?status=PENDING' },
          { label: 'Invoiced', value: stats?.invoicedOrders, icon: CheckCircle, color: 'bg-indigo-600', path: '/sales?status=INVOICED' },
        ].map(({ label, value, icon: Icon, color, path }) => (
          <div key={label} onClick={() => navigate(path)}
            className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6 flex flex-col gap-4 cursor-pointer hover:shadow-md hover:-translate-y-0.5 transition-all">
            <div className={`w-11 h-11 rounded-xl ${color} flex items-center justify-center`}>
              <Icon className="h-5 w-5 text-white" />
            </div>
            <div>
              <p className="text-xs font-bold text-gray-400 uppercase tracking-wider">{label}</p>
              <p className="text-3xl font-extrabold text-gray-900 mt-0.5">{value ?? 0}</p>
            </div>
          </div>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Recent Orders */}
        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
          <div className="px-6 py-4 border-b border-gray-100 flex justify-between items-center">
            <h2 className="font-bold text-gray-900 flex items-center gap-2">
              <TrendingUp className="h-4 w-4 text-blue-500" /> Recent Orders
            </h2>
            <button onClick={() => navigate('/sales')} className="text-xs text-blue-600 font-semibold hover:underline flex items-center gap-1">
              View All <ArrowRight className="h-3 w-3" />
            </button>
          </div>
          {stats?.recentOrders.length === 0 ? (
            <div className="p-10 text-center text-gray-400 text-sm">No orders yet.</div>
          ) : (
            <div className="divide-y divide-gray-50">
              {stats?.recentOrders.map(o => (
                <div key={o.id} className="px-6 py-3 flex items-center justify-between hover:bg-gray-50 transition">
                  <div>
                    <p className="text-sm font-semibold text-gray-900">{o.customerName}</p>
                    <p className="text-xs text-gray-400">{o.vehicleName}</p>
                  </div>
                  <div className="text-right">
                    <p className="text-sm font-bold text-gray-900">₹{o.finalAmount.toLocaleString('en-IN')}</p>
                    <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${STATUS_COLORS[o.status]}`}>{o.status}</span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Quick Actions */}
        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6">
          <h2 className="font-bold text-gray-900 mb-4">Quick Actions</h2>
          <div className="space-y-3">
            {[
              { label: 'View My Customers', desc: 'See your assigned customer list', icon: Users, path: '/customers', color: 'bg-blue-50 text-blue-600' },
              { label: 'View Inventory', desc: 'Check available vehicles', icon: Car, path: '/inventory', color: 'bg-indigo-50 text-indigo-600' },
              { label: 'Schedule Test Drive', desc: 'Book a test drive for a customer', icon: Clock, path: '/test-drives', color: 'bg-purple-50 text-purple-600' },
              { label: 'My Sales Orders', desc: 'Track your order pipeline', icon: ReceiptText, path: '/sales', color: 'bg-green-50 text-green-600' },
            ].map(({ label, desc, icon: Icon, path, color }) => (
              <button key={label} onClick={() => navigate(path)}
                className="w-full flex items-center gap-4 p-3 rounded-xl hover:bg-gray-50 transition text-left border border-gray-100">
                <div className={`w-9 h-9 rounded-lg ${color} flex items-center justify-center flex-shrink-0`}>
                  <Icon className="h-4 w-4" />
                </div>
                <div>
                  <p className="text-sm font-semibold text-gray-900">{label}</p>
                  <p className="text-xs text-gray-400">{desc}</p>
                </div>
                <ArrowRight className="h-4 w-4 text-gray-300 ml-auto" />
              </button>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
