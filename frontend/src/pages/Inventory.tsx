import { useEffect, useState, useCallback } from 'react';
import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import Select from 'react-select';
import api from '../lib/api';
import { Plus, Search, X, Pencil, Trash2, PackagePlus, Filter, AlertTriangle } from 'lucide-react';
import Pagination from '../components/Pagination';

// ── Status config ──────────────────────────────────────────────
const STATUS_CONFIG: Record<string, { label: string; cls: string; dot: string }> = {
  AVAILABLE:    { label: 'Available',    cls: 'bg-green-100 text-green-700',   dot: 'bg-green-500'  },
  LOW_STOCK:    { label: 'Low Stock',    cls: 'bg-yellow-100 text-yellow-700', dot: 'bg-yellow-500' },
  OUT_OF_STOCK: { label: 'Out of Stock', cls: 'bg-red-100 text-red-700',       dot: 'bg-red-500'    },
};

// ── Zod schemas ────────────────────────────────────────────────
const vehicleSchema = z.object({
  modelName: z.object({ value: z.string(), label: z.string() }, { required_error: 'Model is required' }),
  variant: z.object({ value: z.string(), label: z.string() }).nullable().optional(),
  year: z.coerce.number().min(2000, 'Year must be 2000+').max(2030, 'Invalid year'),
  basePrice: z.coerce.number().min(1, 'Price is required'),
  stock: z.coerce.number().min(0, 'Stock cannot be negative'),
});
type VehicleForm = z.infer<typeof vehicleSchema>;

const stockSchema = z.object({
  delta: z.coerce.number().int('Must be a whole number'),
});
type StockForm = z.infer<typeof stockSchema>;

// ── Types ──────────────────────────────────────────────────────
interface Vehicle {
  id: number;
  modelName: string;
  brand?: string;
  variant?: string;
  year: number;
  basePrice: number;
  stock: number;
  stockStatus: string;
  updatedAt?: string;
}
interface Option { value: string; label: string; }

// react-select custom styles
const selectStyles = {
  control: (base: any) => ({
    ...base,
    minHeight: '38px',
    borderColor: '#d1d5db',
    borderRadius: '0.5rem',
    fontSize: '0.875rem',
    boxShadow: 'none',
    '&:hover': { borderColor: '#3b82f6' },
  }),
  menu: (base: any) => ({ ...base, zIndex: 999, fontSize: '0.875rem' }),
};

export default function Inventory() {
  const [vehicles, setVehicles]         = useState<Vehicle[]>([]);
  const [loading, setLoading]           = useState(true);
  const [search, setSearch]             = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  
  // Pagination
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const pageSize = 15;

  const [isAddOpen, setIsAddOpen]       = useState(false);
  const [editTarget, setEditTarget]     = useState<Vehicle | null>(null);
  const [stockTarget, setStockTarget]   = useState<Vehicle | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<Vehicle | null>(null);
  const [stockError, setStockError]     = useState('');

  // ── Model / Variant options ────────────────────────────────
  const [modelOptions, setModelOptions]     = useState<Option[]>([]);
  const [variantOptions, setVariantOptions] = useState<Option[]>([]);
  const [editVariantOpts, setEditVariantOpts] = useState<Option[]>([]);

  // ── Auth ───────────────────────────────────────────────────
  const storedUser = JSON.parse(localStorage.getItem('user') || '{}');
  const userRoles: string[] = storedUser?.roles ?? [];
  const isAdmin = userRoles.includes('ROLE_ADMIN');
  const canEdit = !isAdmin;

  // ── Forms ──────────────────────────────────────────────────
  const addForm = useForm<VehicleForm>({
    resolver: zodResolver(vehicleSchema),
    defaultValues: { stock: 0, year: new Date().getFullYear() },
  });
  const editForm = useForm<VehicleForm>({ resolver: zodResolver(vehicleSchema) });
  const stockForm = useForm<StockForm>({ resolver: zodResolver(stockSchema), defaultValues: { delta: 1 } });

  // ── Load models on mount ────────────────────────────────────
  useEffect(() => {
    api.get('/models').then(res => {
      setModelOptions(res.data.map((m: any) => ({ value: m.name, label: m.name, id: m.id })));
    }).catch(() => {});
  }, []);

  const loadVariants = async (modelName: string, setter: (opts: Option[]) => void) => {
    const modelData = await api.get('/models');
    const found = modelData.data.find((m: any) => m.name === modelName);
    if (found) {
      const vRes = await api.get(`/models/${found.id}/variants`);
      setter(vRes.data.map((v: any) => ({ value: v.name, label: v.name })));
    } else {
      setter([]);
    }
  };

  // ── Fetch inventory ─────────────────────────────────────────
  const fetchVehicles = useCallback(async () => {
    setLoading(true);
    try {
      let url = `/vehicles?search=${encodeURIComponent(search)}&page=${page}&size=${pageSize}`;
      if (statusFilter) url += `&status=${statusFilter}`;
      
      const res = await api.get(url);
      setVehicles(res.data?.content ?? []);
      setTotalPages(res.data?.totalPages ?? 0);
      setTotalElements(res.data?.totalElements ?? 0);
    } catch (err) { console.error(err); }
    finally { setLoading(false); }
  }, [search, statusFilter, page]);

  useEffect(() => {
    const t = setTimeout(fetchVehicles, 200);
    return () => clearTimeout(t);
  }, [fetchVehicles]);

  const handleFilterChange = (setter: any, val: any) => {
    setter(val);
    setPage(0);
  };

  // ── Handlers ───────────────────────────────────────────────
  const onAdd = async (data: VehicleForm) => {
    try {
      const payload = {
        modelName: data.modelName.value,
        brand: 'Hyundai',
        variant: data.variant?.value ?? null,
        year: data.year,
        basePrice: data.basePrice,
        stock: data.stock,
      };
      const res = await api.post('/vehicles', payload);
      setVehicles(prev => [res.data, ...prev]);
      setIsAddOpen(false);
      addForm.reset({ stock: 0, year: new Date().getFullYear() });
      setVariantOptions([]);
    } catch { alert('Failed to add vehicle.'); }
  };

  const openEdit = async (v: Vehicle) => {
    setEditTarget(v);
    const modelOpt = { value: v.modelName, label: v.modelName };
    editForm.reset({
      modelName: modelOpt,
      variant: v.variant ? { value: v.variant, label: v.variant } : null,
      year: v.year,
      basePrice: v.basePrice,
      stock: v.stock,
    });
    await loadVariants(v.modelName, setEditVariantOpts);
  };

  const onEdit = async (data: VehicleForm) => {
    if (!editTarget) return;
    try {
      const payload = {
        modelName: data.modelName.value,
        brand: 'Hyundai',
        variant: data.variant?.value ?? null,
        year: data.year,
        basePrice: data.basePrice,
      };
      const res = await api.put(`/vehicles/${editTarget.id}`, payload);
      setVehicles(prev => prev.map(v => v.id === editTarget.id ? res.data : v));
      setEditTarget(null);
    } catch { alert('Failed to update vehicle.'); }
  };

  const onStockUpdate = async (data: StockForm) => {
    if (!stockTarget) return;
    setStockError('');
    try {
      const res = await api.patch(`/vehicles/${stockTarget.id}/stock`, { delta: data.delta });
      setVehicles(prev => prev.map(v => v.id === stockTarget.id ? res.data : v));
      setStockTarget(null);
      stockForm.reset({ delta: 1 });
    } catch (err: any) {
      const msg = err.response?.data?.message ?? err.response?.data ?? 'Failed to update stock.';
      setStockError(typeof msg === 'string' ? msg : 'Stock cannot go negative.');
    }
  };

  const onDelete = async () => {
    if (!deleteTarget) return;
    try {
      await api.delete(`/vehicles/${deleteTarget.id}`);
      setVehicles(prev => prev.filter(v => v.id !== deleteTarget.id));
      setDeleteTarget(null);
    } catch { alert('Failed to delete vehicle.'); }
  };

  const lowStockCount = vehicles.filter(v => v.stockStatus === 'LOW_STOCK').length;
  const outCount      = vehicles.filter(v => v.stockStatus === 'OUT_OF_STOCK').length;

  return (
    <div className="space-y-6 relative">
      {/* Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Dealership Inventory</h1>
          <p className="text-sm text-gray-500 mt-1">{totalElements} vehicles in stock</p>
        </div>
        {canEdit && (
          <button
            onClick={() => setIsAddOpen(true)}
            className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2.5 rounded-lg text-sm font-semibold flex items-center gap-2 transition-colors shadow-sm"
          >
            <Plus className="h-4 w-4" /> Add Vehicle
          </button>
        )}
      </div>

      {/* Stock Alert Banner */}
      {(lowStockCount > 0 || outCount > 0) && (
        <div className="flex items-center gap-3 bg-amber-50 border border-amber-200 rounded-xl px-4 py-3">
          <AlertTriangle className="h-5 w-5 text-amber-500 shrink-0" />
          <p className="text-sm text-amber-800">
            {outCount > 0 && <span className="font-semibold">{outCount} model(s) out of stock. </span>}
            {lowStockCount > 0 && <span>{lowStockCount} model(s) running low. Consider restocking.</span>}
          </p>
        </div>
      )}

      {/* Filters */}
      <div className="bg-white border border-gray-200 rounded-xl p-4 flex flex-wrap gap-3 items-center">
        <div className="relative flex-1 min-w-[200px]">
          <Search className="h-4 w-4 absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
          <input
            type="text"
            placeholder="Search model or variant..."
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
            <option value="AVAILABLE">🟢 Available</option>
            <option value="LOW_STOCK">🟡 Low Stock</option>
            <option value="OUT_OF_STOCK">🔴 Out of Stock</option>
          </select>
        </div>
      </div>

      {/* Table */}
      <div className="bg-white shadow-sm border border-gray-200 rounded-xl overflow-hidden">
        {loading ? (
          <div className="p-16 text-center text-gray-400 text-sm">Loading inventory...</div>
        ) : vehicles.length === 0 ? (
          <div className="p-16 text-center text-gray-400 text-sm">No vehicles found. Add your first one!</div>
        ) : (
          <>
          <table className="min-w-full divide-y divide-gray-100">
            <thead className="bg-gray-50">
              <tr>
                {['Model', 'Variant', 'Year', 'Price (₹)', 'Stock', 'Status', 'Last Updated', 'Actions'].map(h => (
                  <th key={h} className="px-5 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-100">
              {vehicles.map(v => {
                const sc = STATUS_CONFIG[v.stockStatus] ?? STATUS_CONFIG['AVAILABLE'];
                return (
                  <tr key={v.id} className="hover:bg-gray-50 transition-colors">
                    <td className="px-5 py-3 font-semibold text-gray-900 text-sm">
                      <div>{v.modelName}</div>
                      <div className="text-xs text-gray-400 font-normal">{v.brand ?? 'Hyundai'}</div>
                    </td>
                    <td className="px-5 py-3 text-sm text-gray-600">{v.variant ?? '—'}</td>
                    <td className="px-5 py-3 text-sm text-gray-600">{v.year}</td>
                    <td className="px-5 py-3 text-sm font-medium text-gray-800">
                      ₹{Number(v.basePrice).toLocaleString('en-IN')}
                    </td>
                    <td className="px-5 py-3">
                      <div className="flex items-center gap-1.5">
                        {v.stockStatus === 'LOW_STOCK' && <AlertTriangle className="h-3.5 w-3.5 text-yellow-500" />}
                        <span className={`font-bold text-sm ${v.stock === 0 ? 'text-red-600' : v.stock < 3 ? 'text-yellow-600' : 'text-gray-800'}`}>
                          {v.stock ?? 0}
                        </span>
                      </div>
                    </td>
                    <td className="px-5 py-3">
                      <span className={`inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-semibold ${sc.cls}`}>
                        <span className={`w-1.5 h-1.5 rounded-full ${sc.dot}`} />
                        {sc.label}
                      </span>
                    </td>
                    <td className="px-5 py-3 text-xs text-gray-400 font-normal">
                      {v.updatedAt ? new Date(v.updatedAt).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' }) : '—'}
                    </td>
                    <td className="px-5 py-3 whitespace-nowrap">
                      {canEdit ? (
                        <div className="flex items-center gap-2">
                          <button onClick={() => { setStockError(''); setStockTarget(v); }}
                            className="p-1.5 text-gray-400 hover:text-green-600 hover:bg-green-50 rounded-md transition-colors" title="Update Stock">
                            <PackagePlus className="h-4 w-4" />
                          </button>
                          <button onClick={() => openEdit(v)}
                            className="p-1.5 text-gray-400 hover:text-amber-600 hover:bg-amber-50 rounded-md transition-colors" title="Edit">
                            <Pencil className="h-4 w-4" />
                          </button>
                          <button onClick={() => setDeleteTarget(v)}
                            className="p-1.5 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-md transition-colors" title="Remove">
                            <Trash2 className="h-4 w-4" />
                          </button>
                        </div>
                      ) : <span className="text-xs text-gray-400">View only</span>}
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

      {/* ── Add Vehicle Modal ─────────────────────────────── */}
      {isAddOpen && (
        <Modal title="Add Vehicle to Inventory" onClose={() => { setIsAddOpen(false); setVariantOptions([]); }}>
          <form onSubmit={addForm.handleSubmit(onAdd)} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Model *</label>
              <Controller
                name="modelName"
                control={addForm.control}
                render={({ field }) => (
                  <Select
                    {...field}
                    options={modelOptions}
                    styles={selectStyles}
                    placeholder="Search model (e.g. Creta)..."
                    isSearchable
                    onChange={opt => {
                      field.onChange(opt);
                      addForm.setValue('variant', null);
                      setVariantOptions([]);
                      if (opt) loadVariants(opt.value, setVariantOptions);
                    }}
                  />
                )}
              />
              {addForm.formState.errors.modelName && (
                <p className="text-xs text-red-500 mt-1">{addForm.formState.errors.modelName.message}</p>
              )}
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Variant</label>
              <Controller
                name="variant"
                control={addForm.control}
                render={({ field }) => (
                  <Select
                    {...field}
                    options={variantOptions}
                    styles={selectStyles}
                    placeholder={variantOptions.length === 0 ? 'Select model first...' : 'Select variant...'}
                    isSearchable
                    isClearable
                    isDisabled={variantOptions.length === 0}
                  />
                )}
              />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <Field label="Year *" error={addForm.formState.errors.year?.message}>
                <input type="number" {...addForm.register('year')} className="input-field" />
              </Field>
              <Field label="Price (₹) *" error={addForm.formState.errors.basePrice?.message}>
                <input type="number" {...addForm.register('basePrice')} className="input-field" placeholder="1500000" />
              </Field>
            </div>
            <Field label="Initial Stock *" error={addForm.formState.errors.stock?.message}>
              <input type="number" {...addForm.register('stock')} className="input-field" placeholder="5" />
            </Field>
            <ModalFooter onCancel={() => setIsAddOpen(false)} submitLabel="Add to Inventory" isSubmitting={addForm.formState.isSubmitting} />
          </form>
        </Modal>
      )}

      {/* ── Edit Modal ─────────────────────────────────────── */}
      {editTarget && (
        <Modal title={`Edit — ${editTarget.modelName}`} onClose={() => setEditTarget(null)}>
          <form onSubmit={editForm.handleSubmit(onEdit)} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Model *</label>
              <Controller
                name="modelName"
                control={editForm.control}
                render={({ field }) => (
                  <Select
                    {...field}
                    options={modelOptions}
                    styles={selectStyles}
                    isSearchable
                    onChange={opt => {
                      field.onChange(opt);
                      editForm.setValue('variant', null);
                      if (opt) loadVariants(opt.value, setEditVariantOpts);
                    }}
                  />
                )}
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Variant</label>
              <Controller
                name="variant"
                control={editForm.control}
                render={({ field }) => (
                  <Select {...field} options={editVariantOpts} styles={selectStyles} isSearchable isClearable placeholder="Select variant..." />
                )}
              />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <Field label="Year *" error={editForm.formState.errors.year?.message}>
                <input type="number" {...editForm.register('year')} className="input-field" />
              </Field>
              <Field label="Price (₹) *" error={editForm.formState.errors.basePrice?.message}>
                <input type="number" {...editForm.register('basePrice')} className="input-field" />
              </Field>
            </div>
            <ModalFooter onCancel={() => setEditTarget(null)} submitLabel="Save Changes" isSubmitting={editForm.formState.isSubmitting} />
          </form>
        </Modal>
      )}

      {/* ── Update Stock Modal ─────────────────────────────── */}
      {stockTarget && (
        <Modal title={`Update Stock — ${stockTarget.modelName}${stockTarget.variant ? ` ${stockTarget.variant}` : ''}`} onClose={() => setStockTarget(null)}>
          <div className="mb-4 p-3 bg-gray-50 rounded-lg flex justify-between items-center">
            <span className="text-sm text-gray-600">Current Stock</span>
            <span className="font-bold text-lg text-gray-900">{stockTarget.stock ?? 0} units</span>
          </div>
          <form onSubmit={stockForm.handleSubmit(onStockUpdate)} className="space-y-4">
            <Field label="Adjustment" error={stockForm.formState.errors.delta?.message}>
              <input
                type="number"
                {...stockForm.register('delta')}
                className="input-field text-center text-lg font-semibold"
                placeholder="+5 to add, -1 to reduce"
              />
              <p className="text-xs text-gray-400 mt-1">Positive adds stock. Negative reduces it (e.g. -1 when sold). Cannot go below 0.</p>
            </Field>
            {stockError && (
              <div className="bg-red-50 border border-red-200 rounded-lg px-3 py-2 text-sm text-red-700">{stockError}</div>
            )}
            <ModalFooter onCancel={() => setStockTarget(null)} submitLabel="Update Stock" isSubmitting={stockForm.formState.isSubmitting} />
          </form>
        </Modal>
      )}

      {/* ── Delete Confirm ─────────────────────────────────── */}
      {deleteTarget && (
        <div className="fixed inset-0 bg-black/50 z-50 flex justify-center items-center p-4">
          <div className="bg-white rounded-2xl shadow-2xl p-6 max-w-sm w-full text-center">
            <div className="w-12 h-12 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <Trash2 className="h-6 w-6 text-red-600" />
            </div>
            <h3 className="text-lg font-bold text-gray-800 mb-2">Remove Vehicle?</h3>
            <p className="text-sm text-gray-500 mb-6">
              <strong>{deleteTarget.modelName} {deleteTarget.variant}</strong> will be permanently removed from inventory.
            </p>
            <div className="flex gap-3 justify-center">
              <button onClick={() => setDeleteTarget(null)} className="px-4 py-2 text-sm font-medium border border-gray-300 rounded-lg hover:bg-gray-50">Cancel</button>
              <button onClick={onDelete} className="px-4 py-2 text-sm font-medium text-white bg-red-600 hover:bg-red-700 rounded-lg">Remove</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

// ── Helpers ────────────────────────────────────────────────────
function Modal({ title, onClose, children }: { title: string; onClose: () => void; children: React.ReactNode }) {
  return (
    <div className="fixed inset-0 bg-black/50 z-50 flex justify-center items-center p-4">
      <div className="bg-white w-full max-w-lg rounded-2xl shadow-2xl">
        <div className="flex justify-between items-center px-6 py-4 border-b">
          <h2 className="text-lg font-bold text-gray-800">{title}</h2>
          <button onClick={onClose} className="text-gray-400 hover:text-gray-700 transition"><X className="h-5 w-5" /></button>
        </div>
        <div className="p-6 max-h-[80vh] overflow-y-auto">{children}</div>
      </div>
    </div>
  );
}

function Field({ label, error, children }: { label: string; error?: string; children: React.ReactNode }) {
  return (
    <div>
      <label className="block text-sm font-medium text-gray-700 mb-1">{label}</label>
      {children}
      {error && <p className="text-xs text-red-500 mt-1">{error}</p>}
    </div>
  );
}

function ModalFooter({ onCancel, submitLabel, isSubmitting }: { onCancel: () => void; submitLabel: string; isSubmitting: boolean }) {
  return (
    <div className="flex justify-end gap-3 pt-2 border-t">
      <button type="button" onClick={onCancel} className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition">Cancel</button>
      <button type="submit" disabled={isSubmitting} className="px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700 transition disabled:opacity-50">
        {isSubmitting ? 'Saving...' : submitLabel}
      </button>
    </div>
  );
}
