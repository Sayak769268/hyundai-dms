import { useEffect, useState, useCallback, useMemo } from 'react';
import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import Select from 'react-select';
import api from '../lib/api';
import { Plus, Search, X, Eye, CheckCircle, HandCoins, AlertTriangle, Filter } from 'lucide-react';
import Pagination from '../components/Pagination';
import { useLocation } from 'react-router-dom';

// ── Status config ──────────────────────────────────────────────
const STATUS_CONFIG: Record<string, { label: string; cls: string }> = {
  PENDING:    { label: 'Pending',     cls: 'bg-yellow-100 text-yellow-800 border-yellow-200' },
  CONFIRMED:  { label: 'Confirmed',   cls: 'bg-blue-100 text-blue-800 border-blue-200' },
  INVOICED:   { label: 'Invoiced',    cls: 'bg-green-100 text-green-800 border-green-200' },
  CANCELLED:  { label: 'Cancelled',   cls: 'bg-red-100 text-red-800 border-red-200' },
};

// ── Zod schemas ────────────────────────────────────────────────
const orderSchema = z.object({
  customerOpt: z.object({ value: z.number(), label: z.string() }, { required_error: 'Customer is required' }),
  vehicleOpt: z.object({ value: z.number(), label: z.string(), price: z.number() }, { required_error: 'Vehicle is required' }),
  discount: z.coerce.number().min(0, 'Discount cannot be negative').optional(),
});
type OrderForm = z.infer<typeof orderSchema>;

// ── Types ──────────────────────────────────────────────────────
interface SalesOrder {
  id: number;
  customerId: number;
  vehicleId: number;
  employeeId?: number;
  price: number;
  discount: number;
  finalAmount: number;
  status: string;
  createdAt: string;
  customerName: string;
  vehicleName: string;
  vehicleVariant?: string;
  dealerId?: number;
  dealerName?: string;
}

interface Customer {
  id: number;
  firstName: string;
  lastName: string;
  phone: string;
}

interface Vehicle {
  id: number;
  modelName: string;
  variant: string;
  basePrice: number;
  stock: number;
}

const selectStyles = {
  control: (base: any) => ({
    ...base, minHeight: '42px', borderColor: '#d1d5db', borderRadius: '0.5rem', boxShadow: 'none',
    '&:hover': { borderColor: '#3b82f6' },
  }),
  menu: (base: any) => ({ ...base, zIndex: 9999 }),
};

export default function Sales() {
  const [orders, setOrders] = useState<SalesOrder[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [fromDate, setFromDate] = useState('');
  const [toDate, setToDate] = useState('');
  const [minAmount, setMinAmount] = useState('');
  const [maxAmount, setMaxAmount] = useState('');
  
  // Pagination
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const pageSize = 15;

  const isAdmin = JSON.parse(localStorage.getItem('user') || '{}').roles?.includes('ROLE_ADMIN');
  const location = useLocation();

  // Read URL params on mount (e.g. from dashboard shortcuts)
  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const s = params.get('status');
    if (s) setStatusFilter(s);
  }, []);

  // Modals
  const [isAddOpen, setIsAddOpen] = useState(false);
  const [viewOrder, setViewOrder] = useState<SalesOrder | null>(null);
  const [errorMsg, setErrorMsg] = useState('');

  // Dropdown data options
  const [customerOptions, setCustomerOptions] = useState<any[]>([]);
  const [vehicleOptions, setVehicleOptions] = useState<any[]>([]);

  // Forms
  const addForm = useForm<OrderForm>({ resolver: zodResolver(orderSchema), defaultValues: { discount: 0 } });
  
  // Live watch for price calculation
  const selVehicle = addForm.watch('vehicleOpt');
  const selDiscount = addForm.watch('discount') || 0;
  const currentPrice = selVehicle?.price || 0;
  const finalAmount = Math.max(0, currentPrice - selDiscount);

  const fetchOrders = useCallback(async () => {
    setLoading(true);
    try {
      let url = `/sales?page=${page}&size=${pageSize}&search=${encodeURIComponent(search)}`;
      if (statusFilter) url += `&status=${statusFilter}`;
      if (fromDate) url += `&fromDate=${fromDate}`;
      if (toDate) url += `&toDate=${toDate}`;
      if (minAmount) url += `&minAmount=${minAmount}`;
      if (maxAmount) url += `&maxAmount=${maxAmount}`;
      const res = await api.get(url);
      setOrders(res.data?.content || []);
      setTotalPages(res.data?.totalPages || 0);
      setTotalElements(res.data?.totalElements || 0);
    } catch { /* Handle */ } finally { setLoading(false); }
  }, [page, search, statusFilter, fromDate, toDate, minAmount, maxAmount]);

  useEffect(() => {
    fetchOrders();
  }, [fetchOrders]);

  const handleFilterChange = (setter: any, val: any) => {
    setter(val);
    setPage(0);
  };

  const loadDropdowns = async () => {
    try {
      // Load active customers
      const cRes = await api.get('/customers?page=0&size=200');
      const activeCustomers = cRes.data.content as Customer[];
      setCustomerOptions(activeCustomers.map(c => ({
        value: c.id, 
        label: `${c.firstName} ${c.lastName} (${c.phone})`
      })));

      // Load available inventory
      const vRes = await api.get('/vehicles?page=0&size=200');
      const allVehicles = vRes.data.content as Vehicle[];
      const available = allVehicles.filter(v => v.stock > 0);
      setVehicleOptions(available.map(v => ({
        value: v.id, 
        label: `${v.modelName} ${v.variant || ''} - ₹${v.basePrice.toLocaleString()} (Stock: ${v.stock})`,
        price: v.basePrice
      })));
    } catch { alert("Failed to load dropdown data"); }
  };

  const openCreateModal = () => {
    addForm.reset({ discount: 0 });
    setErrorMsg('');
    loadDropdowns();
    setIsAddOpen(true);
  };

  const onCreate = async (data: OrderForm) => {
    if (finalAmount < 0) return;
    setErrorMsg('');
    try {
      const payload = {
        customerId: data.customerOpt.value,
        vehicleId: data.vehicleOpt.value,
        discount: data.discount || 0
      };
      const res = await api.post('/sales', payload);
      setOrders(prev => [res.data, ...prev]);
      setTotalElements(prev => prev + 1);
      setIsAddOpen(false);
      fetchOrders();
    } catch (err: any) {
      const status = err.response?.status;
      if (status === 401 || status === 403) {
        setErrorMsg('Session expired. Please sign out and sign back in.');
        return;
      }
      const msg = err.response?.data?.message || err.response?.data || 'An unexpected error occurred';
      setErrorMsg(typeof msg === 'string' ? msg : 'Failed to create order. Please try again.');
    }
  };

  const updateStatus = async (id: number, newStatus: string) => {
    try {
      const res = await api.patch(`/sales/${id}/status`, { status: newStatus });
      setOrders(prev => prev.map(o => o.id === id ? res.data : o));
      if (viewOrder && viewOrder.id === id) setViewOrder(res.data);
    } catch (err: any) {
      alert("Failed to update status: " + (err.response?.data?.message || 'Error'));
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Dealership Sales</h1>
          <p className="text-sm text-gray-500 mt-1">Manage processing and invoices for vehicle sales.</p>
        </div>
        {!isAdmin && (
          <button
            onClick={openCreateModal}
            className="bg-blue-600 hover:bg-blue-700 text-white px-5 py-2.5 rounded-lg text-sm font-semibold flex items-center shadow-sm transition"
          >
            <Plus className="h-4 w-4 mr-2" /> Create Order
          </button>
        )}
      </div>

      {/* Filter / Search */}
      <div className="bg-white border border-gray-200 rounded-xl p-4 flex flex-wrap gap-2 items-center">
        <div className="relative flex-1 max-w-sm">
          <Search className="h-5 w-5 absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
          <input
            type="text"
            placeholder="Search by ID, customer, or model..."
            value={search}
            onChange={e => handleFilterChange(setSearch, e.target.value)}
            className="pl-10 pr-4 py-2 w-full border border-gray-300 rounded-lg text-sm focus:ring-blue-500 focus:border-blue-500 outline-none transition"
          />
        </div>
        <select
          value={statusFilter}
          onChange={e => handleFilterChange(setStatusFilter, e.target.value)}
          className="border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-blue-500 focus:border-blue-500 outline-none"
        >
          <option value="">All Statuses</option>
          <option value="PENDING">Pending</option>
          <option value="CONFIRMED">Confirmed</option>
          <option value="INVOICED">Invoiced</option>
          <option value="CANCELLED">Cancelled</option>
        </select>
        {(search || statusFilter) && (
          <button onClick={() => { setSearch(''); setStatusFilter(''); setFromDate(''); setToDate(''); setMinAmount(''); setMaxAmount(''); setPage(0); }} className="text-xs text-red-500 hover:text-red-700 flex items-center gap-1 border border-red-200 px-2 py-1.5 rounded-lg">
            <X className="h-3 w-3" /> Clear
          </button>
        )}
        <input type="date" value={fromDate} onChange={e => handleFilterChange(setFromDate, e.target.value)}
          className="border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-blue-500 focus:border-blue-500 outline-none" />
        <span className="text-gray-400 text-sm">–</span>
        <input type="date" value={toDate} onChange={e => handleFilterChange(setToDate, e.target.value)}
          className="border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-blue-500 focus:border-blue-500 outline-none" />
        <input type="number" placeholder="Min ₹" value={minAmount} onChange={e => handleFilterChange(setMinAmount, e.target.value)}
          className="border border-gray-300 rounded-lg px-3 py-2 text-sm w-28 focus:ring-blue-500 focus:border-blue-500 outline-none" />
        <input type="number" placeholder="Max ₹" value={maxAmount} onChange={e => handleFilterChange(setMaxAmount, e.target.value)}
          className="border border-gray-300 rounded-lg px-3 py-2 text-sm w-28 focus:ring-blue-500 focus:border-blue-500 outline-none" />
        <p className="ml-auto text-xs text-gray-400 italic">Total: {totalElements} orders</p>
      </div>

      {/* Table */}
      <div className="bg-white border border-gray-200 rounded-xl shadow-sm overflow-x-auto">
        {loading ? (
          <div className="p-16 text-center text-gray-400">Loading orders...</div>
        ) : orders.length === 0 ? (
          <div className="p-16 flex justify-center">
            <div className="text-center">
              <HandCoins className="h-10 w-10 text-gray-300 mx-auto mb-3" />
              <p className="text-gray-500 font-medium">No sales orders found.</p>
              <p className="text-sm text-gray-400">Start by creating a new order.</p>
            </div>
          </div>
        ) : (
          <>
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">
                  {isAdmin ? 'Dealer / Order' : 'Order ID'}
                </th>
                {['Customer', 'Vehicle', 'Amount', 'Date', 'Status', 'Actions'].map(h => (
                  <th key={h} className="px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {orders.map(o => {
                const badge = STATUS_CONFIG[o.status] || STATUS_CONFIG.PENDING;
                return (
                  <tr key={o.id} className="hover:bg-gray-50/50 transition">
                    <td className="px-6 py-4 whitespace-nowrap">
                      {isAdmin ? (
                        <div className="flex flex-col">
                          <span className="text-sm font-bold text-gray-900 border-l-2 border-transparent hover:border-blue-500 pl-1">{o.dealerName || 'Unknown Dealer'}</span>
                          <span className="text-xs font-medium text-gray-400 mt-0.5 ml-1">#ORD-{o.id}</span>
                        </div>
                      ) : (
                        <span className="text-sm font-bold text-gray-700">ORD-{o.id}</span>
                      )}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 font-medium">{o.customerName}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-800">
                      {o.vehicleName} {o.vehicleVariant && <span className="text-gray-400 font-normal ml-1">({o.vehicleVariant})</span>}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-bold text-gray-900">
                      ₹{o.finalAmount.toLocaleString('en-IN')}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 font-normal">
                      {new Date(o.createdAt).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' })}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`px-3 py-1 text-xs font-bold rounded-full border ${badge.cls}`}>
                        {badge.label}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm">
                      <button onClick={() => setViewOrder(o)} className="text-blue-600 hover:text-blue-800 font-medium px-3 py-1.5 rounded-md hover:bg-blue-50 transition border border-transparent hover:border-blue-100 flex items-center gap-1.5">
                        <Eye className="h-4 w-4" /> View
                      </button>
                    </td>
                  </tr>
                );
              })}
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

      {/* ── Create Order Modal ───────────────────────────────── */}
      {isAddOpen && (
        <Modal title="Create New Sales Order" onClose={() => setIsAddOpen(false)}>
          <form onSubmit={addForm.handleSubmit(onCreate)} className="space-y-5">
            {errorMsg && (
              <div className="bg-red-50 border border-red-200 text-red-700 p-3 rounded-lg text-sm font-medium flex gap-2 items-center">
                <AlertTriangle className="h-4 w-4 shrink-0" /> {errorMsg}
              </div>
            )}
            
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-semibold text-gray-700 mb-1">Select Customer</label>
                <Controller
                  name="customerOpt"
                  control={addForm.control}
                  render={({ field }) => (
                    <Select {...field} options={customerOptions} styles={selectStyles} isSearchable placeholder="Search CRM customers..." />
                  )}
                />
                {addForm.formState.errors.customerOpt && <p className="text-red-500 text-xs mt-1">{addForm.formState.errors.customerOpt.message}</p>}
              </div>

              <div>
                <label className="block text-sm font-semibold text-gray-700 mb-1">Select Vehicle (In Stock Only)</label>
                <Controller
                  name="vehicleOpt"
                  control={addForm.control}
                  render={({ field }) => (
                    <Select {...field} options={vehicleOptions} styles={selectStyles} isSearchable placeholder="Search inventory models..." />
                  )}
                />
                {addForm.formState.errors.vehicleOpt && <p className="text-red-500 text-xs mt-1">{addForm.formState.errors.vehicleOpt.message}</p>}
              </div>
            </div>

            <div className="bg-gray-50 rounded-xl p-4 border border-gray-200 space-y-4">
              <div className="flex justify-between items-center text-sm font-medium text-gray-500">
                <span>Vehicle Base Price</span>
                <span className="text-gray-900">₹{currentPrice.toLocaleString('en-IN')}</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-sm font-semibold text-gray-700">Apply Discount (₹)</span>
                <div className="w-1/2">
                  <input
                    type="number"
                    min="0"
                    {...addForm.register('discount')}
                    className="w-full text-right p-2 border border-gray-300 rounded-lg text-sm focus:ring-blue-500 outline-none"
                    placeholder="0"
                  />
                </div>
              </div>
              <div className="pt-3 border-t flex justify-between items-center bg-blue-50 -mx-4 -mb-4 p-4 rounded-b-xl border-blue-100 border-x border-b">
                <span className="font-bold text-gray-800">Final Amount</span>
                <span className={`text-xl font-bold ${finalAmount < 0 ? 'text-red-600' : 'text-blue-700'}`}>
                   ₹{finalAmount.toLocaleString('en-IN')}
                </span>
              </div>
            </div>

            <div className="flex justify-end gap-3 pt-4 border-t border-gray-100">
              <button type="button" onClick={() => setIsAddOpen(false)} className="px-5 py-2.5 text-sm font-semibold text-gray-600 border border-gray-300 rounded-lg hover:bg-gray-50">Cancel</button>
              <button type="submit" disabled={addForm.formState.isSubmitting || finalAmount < 0} className="px-5 py-2.5 text-sm font-semibold text-white bg-blue-600 rounded-lg hover:bg-blue-700 disabled:opacity-50">
                {addForm.formState.isSubmitting ? 'Processing...' : 'Confirm Order'}
              </button>
            </div>
          </form>
        </Modal>
      )}

      {/* ── View Order Modal ───────────────────────────────── */}
      {viewOrder && (
        <Modal title={`Order Details: ORD-${viewOrder.id}`} onClose={() => setViewOrder(null)}>
          <div className="space-y-6">
            <div className="flex justify-between items-start">
              <div>
                <h3 className="text-xl font-bold text-gray-900">₹{viewOrder.finalAmount.toLocaleString('en-IN')}</h3>
                <p className="text-sm text-gray-500">Final Amount • Created on {new Date(viewOrder.createdAt).toLocaleDateString('en-IN')}</p>
              </div>
              <span className={`px-4 py-1.5 text-sm font-bold rounded-full border ${STATUS_CONFIG[viewOrder.status]?.cls || ''}`}>
                {STATUS_CONFIG[viewOrder.status]?.label || viewOrder.status}
              </span>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="bg-gray-50 p-4 rounded-xl border border-gray-100">
                <p className="text-xs text-gray-400 font-bold uppercase tracking-wider mb-1">Customer</p>
                <p className="text-sm font-medium text-gray-900">{viewOrder.customerName}</p>
              </div>
              <div className="bg-gray-50 p-4 rounded-xl border border-gray-100">
                <p className="text-xs text-gray-400 font-bold uppercase tracking-wider mb-1">Vehicle</p>
                <p className="text-sm font-medium text-gray-900">{viewOrder.vehicleName} {viewOrder.vehicleVariant && <span className="opacity-70">({viewOrder.vehicleVariant})</span>}</p>
              </div>
            </div>

            <div className="bg-gray-50 p-4 rounded-xl border border-gray-100 space-y-2">
              <div className="flex justify-between text-sm"><span className="text-gray-500">Base Price</span><span className="font-medium text-gray-900">₹{viewOrder.price.toLocaleString('en-IN')}</span></div>
              <div className="flex justify-between text-sm"><span className="text-red-500">Discount Applied</span><span className="font-medium text-red-600">- ₹{viewOrder.discount.toLocaleString('en-IN')}</span></div>
              <hr className="border-gray-200 my-2" />
              <div className="flex justify-between font-bold"><span className="text-gray-900">Paid Amount</span><span className="text-blue-700">₹{viewOrder.finalAmount.toLocaleString('en-IN')}</span></div>
            </div>

            {/* Order Status Flow Pipeline */}
            <div className="pt-4 border-t border-gray-100">
              <h4 className="text-sm font-bold text-gray-800 mb-3">Update Progress</h4>
              {viewOrder.status === 'CANCELLED' ? (
                <div className="bg-red-50 text-red-800 p-4 rounded-lg text-sm font-medium text-center border border-red-200">
                  This order was permanently cancelled and stock has been released.
                </div>
              ) : viewOrder.status === 'INVOICED' ? (
                <div className="bg-green-50 text-green-800 p-4 rounded-lg text-sm font-medium text-center flex items-center justify-center gap-2 border border-green-200">
                  <CheckCircle className="h-5 w-5" /> Order Successfully Completed!
                </div>
              ) : (
                <div className="flex gap-3">
                  {viewOrder.status === 'PENDING' && (
                    <button onClick={() => updateStatus(viewOrder.id, 'CONFIRMED')} className="flex-1 bg-blue-600 hover:bg-blue-700 text-white font-semibold py-2.5 rounded-lg text-sm transition shadow-sm">
                      Mark as Confirmed
                    </button>
                  )}
                  {viewOrder.status === 'CONFIRMED' && (
                    <button onClick={() => updateStatus(viewOrder.id, 'INVOICED')} className="flex-1 bg-green-600 hover:bg-green-700 text-white font-semibold py-2.5 rounded-lg text-sm transition shadow-sm">
                      Mark as Invoiced
                    </button>
                  )}
                  <button onClick={() => { if(confirm('Are you sure you want to cancel? This will return the stock.')) updateStatus(viewOrder.id, 'CANCELLED'); }} className="px-5 py-2.5 bg-white border border-red-200 text-red-600 hover:bg-red-50 font-semibold rounded-lg text-sm transition">
                    Cancel Order
                  </button>
                </div>
              )}
            </div>
          </div>
        </Modal>
      )}

    </div>
  );
}

// ── Helpers ─────────────────────────────────────────────────────────
function Modal({ title, onClose, children }: { title: string; onClose: () => void; children: React.ReactNode }) {
  return (
    <div className="fixed inset-0 bg-black/50 z-50 flex justify-center items-center p-4">
      <div className="bg-white w-full max-w-lg rounded-2xl shadow-2xl animate-in fade-in zoom-in-95 duration-200 flex flex-col max-h-[90vh]">
        <div className="flex justify-between items-center px-6 py-4 border-b border-gray-100 flex-shrink-0">
          <h2 className="text-lg font-bold text-gray-900">{title}</h2>
          <button onClick={onClose} className="p-2 text-gray-400 hover:text-gray-700 hover:bg-gray-100 rounded-lg transition"><X className="h-5 w-5" /></button>
        </div>
        <div className="p-6 overflow-y-auto flex-1">{children}</div>
      </div>
    </div>
  );
}
