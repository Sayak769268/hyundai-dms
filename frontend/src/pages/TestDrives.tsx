import { useEffect, useState, useCallback } from 'react';
import { Car, Plus, X, Clock, CheckCircle2, XCircle } from 'lucide-react';
import api from '../lib/api';
import Pagination from '../components/Pagination';

interface TestDrive {
  id: number;
  customerName: string;
  customerPhone: string;
  vehicleName: string;
  scheduledDate: string;
  status: string;
  notes?: string;
}

interface Customer { id: number; firstName: string; lastName: string; phone: string; status: string; }
interface Vehicle { id: number; modelName: string; variant?: string; stock: number; basePrice: number; }

const STATUS_CONFIG: Record<string, { label: string; cls: string; icon: any }> = {
  SCHEDULED:  { label: 'Scheduled',  cls: 'bg-blue-100 text-blue-700',   icon: Clock },
  COMPLETED:  { label: 'Completed',  cls: 'bg-green-100 text-green-700', icon: CheckCircle2 },
  CANCELLED:  { label: 'Cancelled',  cls: 'bg-red-100 text-red-700',     icon: XCircle },
};

export default function TestDrives() {
  const [drives, setDrives] = useState<TestDrive[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const pageSize = 15;

  const [isOpen, setIsOpen] = useState(false);
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [vehicles, setVehicles] = useState<Vehicle[]>([]);
  const [form, setForm] = useState({ customerId: '', vehicleId: '', scheduledDate: '', notes: '' });
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const fetchDrives = useCallback(async () => {
    setLoading(true);
    try {
      const res = await api.get(`/test-drives?page=${page}&size=${pageSize}`);
      setDrives(res.data.content || []);
      setTotalPages(res.data.totalPages || 0);
      setTotalElements(res.data.totalElements || 0);
    } catch { setDrives([]); }
    finally { setLoading(false); }
  }, [page]);

  useEffect(() => { fetchDrives(); }, [fetchDrives]);

  const openModal = async () => {
    setError('');
    setForm({ customerId: '', vehicleId: '', scheduledDate: '', notes: '' });
    try {
      const [cRes, vRes] = await Promise.all([
        api.get('/customers?page=0&size=200'),
        api.get('/vehicles?page=0&size=200'),
      ]);
      const eligible = (cRes.data.content || []).filter(
        (c: Customer) => c.status === 'NEW' || c.status === 'INTERESTED'
      );
      setCustomers(eligible);
      setVehicles((vRes.data.content || []).filter((v: Vehicle) => v.stock > 0));
    } catch {}
    setIsOpen(true);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!form.customerId || !form.vehicleId || !form.scheduledDate) {
      setError('Please fill in all required fields.');
      return;
    }
    setError('');
    setSubmitting(true);
    try {
      await api.post('/test-drives', {
        customerId: Number(form.customerId),
        vehicleId: Number(form.vehicleId),
        scheduledDate: form.scheduledDate,
        notes: form.notes,
      });
      setIsOpen(false);
      fetchDrives();
    } catch (err: any) {
      setError(err.response?.data?.message || err.response?.data || 'Failed to schedule test drive.');
    } finally {
      setSubmitting(false);
    }
  };

  const updateStatus = async (id: number, status: string) => {
    try {
      await api.patch(`/test-drives/${id}/status`, { status });
      fetchDrives();
    } catch { alert('Failed to update status.'); }
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Test Drives</h1>
          <p className="text-sm text-gray-500 mt-1">Schedule test drives for New or Interested customers</p>
        </div>
        <button onClick={openModal}
          className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2.5 rounded-lg text-sm font-semibold flex items-center gap-2 shadow-sm transition">
          <Plus className="h-4 w-4" /> Schedule Test Drive
        </button>
      </div>

      <div className="bg-white border border-gray-200 rounded-xl shadow-sm overflow-x-auto">
        {loading ? (
          <div className="p-16 text-center text-gray-400">Loading...</div>
        ) : drives.length === 0 ? (
          <div className="p-16 text-center">
            <Car className="h-12 w-12 text-gray-200 mx-auto mb-4" />
            <p className="text-gray-500 font-medium">No test drives scheduled yet.</p>
            <p className="text-sm text-gray-400 mt-1">Click "Schedule Test Drive" to get started.</p>
            <button onClick={openModal} className="mt-4 bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg text-sm font-semibold transition">
              Schedule Now
            </button>
          </div>
        ) : (
          <>
          <table className="min-w-full divide-y divide-gray-100">
            <thead className="bg-gray-50">
              <tr>
                {['Customer', 'Phone', 'Vehicle', 'Date', 'Status', 'Actions'].map(h => (
                  <th key={h} className="px-5 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {drives.map(d => {
                const sc = STATUS_CONFIG[d.status] || STATUS_CONFIG.SCHEDULED;
                const Icon = sc.icon;
                return (
                  <tr key={d.id} className="hover:bg-gray-50 transition">
                    <td className="px-5 py-3 font-semibold text-gray-900 text-sm">{d.customerName}</td>
                    <td className="px-5 py-3 text-sm text-gray-500">{d.customerPhone}</td>
                    <td className="px-5 py-3 text-sm text-gray-600">{d.vehicleName}</td>
                    <td className="px-5 py-3 text-sm text-gray-600">
                      {d.scheduledDate ? new Date(d.scheduledDate).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' }) : '—'}
                    </td>
                    <td className="px-5 py-3">
                      <span className={`inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-semibold ${sc.cls}`}>
                        <Icon className="h-3 w-3" /> {sc.label}
                      </span>
                    </td>
                    <td className="px-5 py-3 whitespace-nowrap">
                      {d.status === 'SCHEDULED' && (
                        <div className="flex gap-2">
                          <button onClick={() => updateStatus(d.id, 'COMPLETED')}
                            className="text-xs text-green-600 font-semibold hover:underline">Complete</button>
                          <button onClick={() => updateStatus(d.id, 'CANCELLED')}
                            className="text-xs text-red-500 font-semibold hover:underline">Cancel</button>
                        </div>
                      )}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
          <Pagination currentPage={page} totalPages={totalPages} totalElements={totalElements} onPageChange={setPage} pageSize={pageSize} />
          </>
        )}
      </div>

      {isOpen && (
        <div className="fixed inset-0 bg-black/50 z-50 flex justify-center items-center p-4">
          <div className="bg-white w-full max-w-md rounded-2xl shadow-2xl flex flex-col max-h-[90vh]">
            <div className="flex justify-between items-center px-6 py-4 border-b flex-shrink-0">
              <h2 className="text-lg font-bold text-gray-900">Schedule Test Drive</h2>
              <button onClick={() => setIsOpen(false)} className="text-gray-400 hover:text-gray-700"><X className="h-5 w-5" /></button>
            </div>
            <form onSubmit={handleSubmit} className="p-6 space-y-4 overflow-y-auto flex-1">
              {error && <div className="bg-red-50 border border-red-200 rounded-lg p-3 text-sm text-red-700">{error}</div>}

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Customer <span className="text-red-500">*</span>
                  <span className="text-xs text-gray-400 font-normal ml-1">(New or Interested only)</span>
                </label>
                <select required value={form.customerId} onChange={e => setForm(f => ({ ...f, customerId: e.target.value }))}
                  className="w-full border border-gray-300 rounded-xl px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
                  <option value="">Select customer...</option>
                  {customers.map(c => (
                    <option key={c.id} value={c.id}>
                      {c.firstName} {c.lastName} ({c.phone}) — {c.status}
                    </option>
                  ))}
                </select>
                {customers.length === 0 && (
                  <p className="text-xs text-amber-600 mt-1">No eligible customers. Only New or Interested customers can be scheduled.</p>
                )}
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Vehicle <span className="text-red-500">*</span></label>
                <select required value={form.vehicleId} onChange={e => setForm(f => ({ ...f, vehicleId: e.target.value }))}
                  className="w-full border border-gray-300 rounded-xl px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
                  <option value="">Select any in-stock vehicle...</option>
                  {vehicles.map(v => (
                    <option key={v.id} value={v.id}>
                      {v.modelName} {v.variant || ''} — ₹{Number(v.basePrice).toLocaleString('en-IN')} (Stock: {v.stock})
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Scheduled Date <span className="text-red-500">*</span></label>
                <input type="date" required
                  min={new Date().toISOString().split('T')[0]}
                  value={form.scheduledDate}
                  onChange={e => setForm(f => ({ ...f, scheduledDate: e.target.value }))}
                  className="w-full border border-gray-300 rounded-xl px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Notes</label>
                <textarea rows={2} value={form.notes}
                  onChange={e => setForm(f => ({ ...f, notes: e.target.value }))}
                  placeholder="Any special requirements..."
                  className="w-full border border-gray-300 rounded-xl px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none" />
              </div>

              <div className="flex justify-end gap-3 pt-2 border-t">
                <button type="button" onClick={() => setIsOpen(false)}
                  className="px-4 py-2 text-sm font-medium text-gray-700 border border-gray-300 rounded-lg hover:bg-gray-50">Cancel</button>
                <button type="submit" disabled={submitting}
                  className="px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700 disabled:opacity-50">
                  {submitting ? 'Scheduling...' : 'Schedule Test Drive'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
