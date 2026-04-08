import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import ProtectedRoute from './components/ProtectedRoute';
import Sidebar from './components/Sidebar';
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import Customers from './pages/Customers';
import CustomerDetail from './pages/CustomerDetail';
import Inventory from './pages/Inventory';
import Sales from './pages/Sales';
import Admin from './pages/Admin';
import Employees from './pages/Employees';
import Dealers from './pages/Dealers';
import AuditLogs from './pages/AuditLogs';

const DashboardLayout = ({ children }: { children?: React.ReactNode }) => (
  <div className="flex h-screen bg-gray-50 overflow-hidden">
    <Sidebar />
    <main className="flex-1 p-8 overflow-y-auto overflow-x-hidden">
      {children}
    </main>
  </div>
);

const Unauthorized = () => <h1 className="text-red-500 text-center m-8 text-2xl font-bold">Unauthorized Access</h1>;

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/unauthorized" element={<Unauthorized />} />
        
        {/* Protected Routes */}
        <Route element={<ProtectedRoute />}>
          <Route path="/" element={<DashboardLayout><Dashboard /></DashboardLayout>} />
          <Route path="/customers" element={<DashboardLayout><Customers /></DashboardLayout>} />
          <Route path="/customers/:id" element={<DashboardLayout><CustomerDetail /></DashboardLayout>} />
          <Route path="/inventory" element={<DashboardLayout><Inventory /></DashboardLayout>} />
          <Route path="/sales" element={<DashboardLayout><Sales /></DashboardLayout>} />
          <Route path="/employees" element={<DashboardLayout><Employees /></DashboardLayout>} />
          <Route path="/dealers" element={<ProtectedRoute allowedRoles={['ROLE_ADMIN']}><DashboardLayout><Dealers /></DashboardLayout></ProtectedRoute>} />
          <Route path="/audit" element={<ProtectedRoute allowedRoles={['ROLE_ADMIN']}><DashboardLayout><AuditLogs /></DashboardLayout></ProtectedRoute>} />
          <Route path="/admin" element={<ProtectedRoute allowedRoles={['ROLE_ADMIN']}><DashboardLayout><Admin /></DashboardLayout></ProtectedRoute>} />
        </Route>

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
