import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../lib/api';
import { ArrowLeft, User, Phone, Mail, MapPin, Calendar, StickyNote, Clock } from 'lucide-react';

const STATUS_COLORS: Record<string, string> = {
  NEW: 'bg-blue-100 text-blue-700',
  INTERESTED: 'bg-yellow-100 text-yellow-700',
  BOOKED: 'bg-green-100 text-green-700',
  LOST: 'bg-red-100 text-red-700',
};

interface Customer {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  address?: string;
  notes?: string;
  status: string;
  assignedEmployeeName?: string;
  nextFollowUpDate?: string;
  createdAt?: string;
}

export default function CustomerDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [customer, setCustomer] = useState<Customer | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!id) return;
    api.get(`/customers/${id}`)
      .then(res => setCustomer(res.data))
      .catch(() => navigate('/customers'))
      .finally(() => setLoading(false));
  }, [id, navigate]);

  if (loading) {
    return <div className="p-16 text-center text-gray-400 text-sm">Loading customer details...</div>;
  }

  if (!customer) return null;

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <button
        onClick={() => navigate('/customers')}
        className="flex items-center gap-2 text-sm text-gray-500 hover:text-gray-800 transition font-medium"
      >
        <ArrowLeft className="h-4 w-4" />
        Back to CRM
      </button>

      {/* Header Card */}
      <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-6">
        <div className="flex items-start justify-between">
          <div className="flex items-center gap-4">
            <div className="h-14 w-14 rounded-full bg-blue-100 flex items-center justify-center text-blue-700 font-bold text-xl">
              {customer.firstName[0]}{customer.lastName[0]}
            </div>
            <div>
              <h1 className="text-2xl font-bold text-gray-900">{customer.firstName} {customer.lastName}</h1>
              <p className="text-sm text-gray-500 mt-0.5">Customer ID #{customer.id}</p>
            </div>
          </div>
          <span className={`inline-flex items-center px-3 py-1 rounded-full text-sm font-semibold ${STATUS_COLORS[customer.status] ?? 'bg-gray-100 text-gray-600'}`}>
            {customer.status}
          </span>
        </div>
      </div>

      {/* Details Grid */}
      <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-6 space-y-4">
        <h2 className="text-sm font-semibold text-gray-500 uppercase tracking-wider">Contact Information</h2>
        <div className="grid grid-cols-1 gap-4">
          <InfoRow icon={<Mail className="h-4 w-4"/>} label="Email" value={customer.email} />
          <InfoRow icon={<Phone className="h-4 w-4"/>} label="Phone" value={customer.phone} />
          <InfoRow icon={<MapPin className="h-4 w-4"/>} label="Address" value={customer.address ?? '—'} />
        </div>
      </div>

      <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-6 space-y-4">
        <h2 className="text-sm font-semibold text-gray-500 uppercase tracking-wider">CRM Details</h2>
        <div className="grid grid-cols-1 gap-4">
          <InfoRow icon={<User className="h-4 w-4"/>} label="Assigned To" value={customer.assignedEmployeeName ?? 'Not assigned'} />
          <InfoRow icon={<Calendar className="h-4 w-4"/>} label="Next Follow-up"
            value={customer.nextFollowUpDate ? new Date(customer.nextFollowUpDate).toLocaleDateString('en-IN', { dateStyle: 'long' }) : 'Not scheduled'} />
          <InfoRow icon={<Clock className="h-4 w-4"/>} label="Created On"
            value={customer.createdAt ? new Date(customer.createdAt).toLocaleDateString('en-IN', { dateStyle: 'long' }) : '—'} />
        </div>
      </div>

      {customer.notes && (
        <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-6">
          <div className="flex items-center gap-2 mb-3">
            <StickyNote className="h-4 w-4 text-gray-400" />
            <h2 className="text-sm font-semibold text-gray-500 uppercase tracking-wider">Notes</h2>
          </div>
          <p className="text-sm text-gray-700 whitespace-pre-line leading-relaxed">{customer.notes}</p>
        </div>
      )}
    </div>
  );
}

function InfoRow({ icon, label, value }: { icon: React.ReactNode; label: string; value: string }) {
  return (
    <div className="flex items-start gap-3">
      <div className="text-gray-400 mt-0.5">{icon}</div>
      <div className="flex-1 flex justify-between items-start">
        <span className="text-sm text-gray-500 font-medium w-32 shrink-0">{label}</span>
        <span className="text-sm text-gray-800 text-right">{value}</span>
      </div>
    </div>
  );
}
